# Implementation Plan - Notifications and Expense Limits

Add class schedule reminders and expense limit alerts to the StudentDaily app.

## User Review Required

> [!IMPORTANT]
> - This update requires a Room migration from version 2 to 3. I will implement a proper migration to preserve your existing data.
> - The app will request `POST_NOTIFICATIONS` permission on Android 13+ and `SCHEDULE_EXACT_ALARM` for precise class reminders.

## Proposed Changes

### Data Layer

#### [NEW] [NotificationSettingsEntity.kt](file:///C:/Users/gokul/AndroidStudioProjects/studentdaily/app/src/main/java/com/example/studentdaily/data/model/NotificationSettingsEntity.kt)
- Store user preferences: `classRemindersEnabled`, `reminderMinutesBefore`, `notifyClassStart`, `notifyClassEnd`, `dailyLimit`, `weeklyLimit`, `monthlyLimit`, etc.

#### [NEW] [ExpenseAlertStateEntity.kt](file:///C:/Users/gokul/AndroidStudioProjects/studentdaily/app/src/main/java/com/example/studentdaily/data/model/ExpenseAlertStateEntity.kt)
- Track triggered thresholds: `type` (DAILY/WEEKLY/MONTHLY), `threshold` (50, 75, 90, 100, OVER), `periodKey` (date/week_year/month_year).

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/gokul/AndroidStudioProjects/studentdaily/app/src/main/java/com/example/studentdaily/data/AppDatabase.kt)
- Register new entities and DAOs.
- Implement Migration from 2 to 3.
- Remove `fallbackToDestructiveMigration(true)`.

### Notification System

#### [NEW] [NotificationHelper.kt](file:///C:/Users/gokul/AndroidStudioProjects/studentdaily/app/src/main/java/com/example/studentdaily/NotificationHelper.kt)
- Manage Notification Channels: `CLASS_REMINDERS`, `EXPENSE_ALERTS`.
- Helper methods to show notifications.

#### [NEW] [ClassReminderManager.kt](file:///C:/Users/gokul/AndroidStudioProjects/studentdaily/app/src/main/java/com/example/studentdaily/ClassReminderManager.kt)
- Logic to schedule/cancel `AlarmManager` intents for class reminders.
- Handles recurring weekly alarms.

#### [NEW] [AlarmReceiver.kt](file:///C:/Users/gokul/AndroidStudioProjects/studentdaily/app/src/main/java/com/example/studentdaily/AlarmReceiver.kt)
- Receives alarms and triggers notifications via `NotificationHelper`.

#### [NEW] [BootReceiver.kt](file:///C:/Users/gokul/AndroidStudioProjects/studentdaily/app/src/main/java/com/example/studentdaily/BootReceiver.kt)
- Reschedules all alarms on device reboot.

### ViewModels

#### [MODIFY] [AttendanceViewModel.kt](file:///C:/Users/gokul/AndroidStudioProjects/studentdaily/app/src/main/java/com/example/studentdaily/ui/viewmodel/AttendanceViewModel.kt)
- Call `ClassReminderManager` on class CRUD operations.

#### [MODIFY] [ExpenseViewModel.kt](file:///C:/Users/gokul/AndroidStudioProjects/studentdaily/app/src/main/java/com/example/studentdaily/ui/viewmodel/ExpenseViewModel.kt)
- Implement threshold check logic after adding/editing expenses.
- Persist triggered states in `ExpenseAlertStateEntity`.

#### [NEW] [SettingsViewModel.kt](file:///C:/Users/gokul/AndroidStudioProjects/studentdaily/app/src/main/java/com/example/studentdaily/ui/viewmodel/SettingsViewModel.kt)
- Manage notification and limit preferences.
- Trigger rescheduling of all class alarms when interval settings change.

### UI

#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/gokul/AndroidStudioProjects/studentdaily/app/src/main/java/com/example/studentdaily/ui/screen/SettingsScreen.kt)
- Add sections for "Class Notifications" and "Expense Limits".
- Implement input validation and Material 3 toggles/pickers.

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/gokul/AndroidStudioProjects/studentdaily/app/src/main/java/com/example/studentdaily/ui/screen/HomeScreen.kt)
- Add "Next Class" and "Expense Status" summary cards.

## Verification Plan

### Automated Tests
- Build verification: `app:assembleDebug`.

### Manual Verification
1. **Class Notifications**:
   - Add a class starting in 35 minutes with a 30-min reminder.
   - Wait 5 minutes to verify notification.
   - Edit class time and verify alarm updates.
   - Reboot emulator (if possible) or trigger BootReceiver manually to check rescheduling.
2. **Expense Alerts**:
   - Set a daily limit of ₹100.
   - Add a ₹51 expense (verify 50% alert).
   - Add another ₹10 expense (verify no repeated 50% alert).
   - Add a ₹30 expense (verify 75% alert).
3. **Data Integrity**:
   - Verify all pre-existing classes and expenses are still present after migration.
