package com.example.studentdaily.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.studentdaily.data.dao.*
import com.example.studentdaily.data.model.*

@Database(
    entities = [
        ClassEntity::class,
        AttendanceEntity::class,
        MenuEntity::class,
        ExpenseEntity::class,
        NotificationSettingsEntity::class,
        ExpenseAlertStateEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun classDao(): ClassDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun menuDao(): MenuDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun settingsDao(): SettingsDao
    abstract fun expenseAlertDao(): ExpenseAlertDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `notification_settings` (`id` INTEGER NOT NULL, `classRemindersEnabled` INTEGER NOT NULL, `reminderMinutesBefore` INTEGER NOT NULL, `notifyClassStart` INTEGER NOT NULL, `notifyClassEnd` INTEGER NOT NULL, `dailyLimitEnabled` INTEGER NOT NULL, `dailyLimit` REAL NOT NULL, `weeklyLimitEnabled` INTEGER NOT NULL, `weeklyLimit` REAL NOT NULL, `monthlyLimitEnabled` INTEGER NOT NULL, `monthlyLimit` REAL NOT NULL, `threshold50` INTEGER NOT NULL, `threshold75` INTEGER NOT NULL, `threshold90` INTEGER NOT NULL, `threshold100` INTEGER NOT NULL, `thresholdOver` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `expense_alert_state` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `type` TEXT NOT NULL, `threshold` TEXT NOT NULL, `periodKey` TEXT NOT NULL)")
                // Initialize settings with default values
                db.execSQL("INSERT OR IGNORE INTO `notification_settings` (`id`, `classRemindersEnabled`, `reminderMinutesBefore`, `notifyClassStart`, `notifyClassEnd`, `dailyLimitEnabled`, `dailyLimit`, `weeklyLimitEnabled`, `weeklyLimit`, `monthlyLimitEnabled`, `monthlyLimit`, `threshold50`, `threshold75`, `threshold90`, `threshold100`, `thresholdOver`) VALUES (1, 1, 30, 0, 0, 0, 0.0, 0, 0.0, 0, 0.0, 1, 1, 1, 1, 1)")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `notification_settings` ADD COLUMN `soundEnabled` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `notification_settings` ADD COLUMN `vibrationEnabled` INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP INDEX IF EXISTS `index_menu_dayOfWeek_mealType`")
            }
        }

        private val CALLBACK = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL("INSERT OR IGNORE INTO `notification_settings` (`id`, `classRemindersEnabled`, `reminderMinutesBefore`, `notifyClassStart`, `notifyClassEnd`, `dailyLimitEnabled`, `dailyLimit`, `weeklyLimitEnabled`, `weeklyLimit`, `monthlyLimitEnabled`, `monthlyLimit`, `threshold50`, `threshold75`, `threshold90`, `threshold100`, `thresholdOver`, `soundEnabled`, `vibrationEnabled`) VALUES (1, 1, 30, 0, 0, 0, 0.0, 0, 0.0, 0, 0.0, 1, 1, 1, 1, 1, 1, 1)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_daily_db"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                .addCallback(CALLBACK)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
