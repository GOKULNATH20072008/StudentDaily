package com.example.studentdaily.ui.screen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studentdaily.data.model.ExpenseCategory
import com.example.studentdaily.data.model.ExpenseEntity
import com.example.studentdaily.ui.components.EditExpenseDialog
import com.example.studentdaily.ui.components.ExpenseItem
import com.example.studentdaily.ui.viewmodel.ExpenseViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: ExpenseViewModel,
    onAddExpense: () -> Unit
) {
    val allExpenses by viewModel.allExpenses.collectAsState(initial = null)
    val weeklyTotalByCategory by viewModel.weeklyTotalByCategory.collectAsState(initial = null)
    
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    var editingExpense by remember { mutableStateOf<ExpenseEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses") },
                actions = {
                    IconButton(onClick = onAddExpense) {
                        Icon(Icons.Default.Add, contentDescription = "Add Expense")
                    }
                },
                windowInsets = TopAppBarDefaults.windowInsets
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Weekly Breakdown
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Weekly Breakdown", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                
                if (weeklyTotalByCategory == null) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    val totalSpend = weeklyTotalByCategory!!.values.sum()
                    ExpenseCategory.entries.forEach { category ->
                        val amount = weeklyTotalByCategory!![category] ?: 0.0
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category.name, style = MaterialTheme.typography.labelMedium)
                                Text(String.format(Locale.getDefault(), "₹%.2f", amount), fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { if (totalSpend > 0) (amount / totalSpend).toFloat() else 0f },
                                modifier = Modifier.fillMaxWidth().height(4.dp),
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            // Section 2: Expense History
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                
                // Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") }
                    )
                    ExpenseCategory.entries.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.name) }
                        )
                    }
                }
            }

            if (allExpenses == null) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                val filteredExpenses = if (selectedCategory == null) {
                    allExpenses!!
                } else {
                    allExpenses!!.filter { it.category == selectedCategory }
                }

                if (filteredExpenses.isEmpty()) {
                    item {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("No expenses found.", color = MaterialTheme.colorScheme.outline)
                            Button(onClick = onAddExpense) {
                                Text("Log your first expense")
                            }
                        }
                    }
                } else {
                    items(filteredExpenses, key = { "expense_${it.id}" }) { expense ->
                        ExpenseItem(
                            expense = expense,
                            onEdit = { editingExpense = expense }
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    editingExpense?.let { entity ->
        EditExpenseDialog(
            expenseEntity = entity,
            onDismiss = { editingExpense = null },
            onUpdate = { viewModel.updateExpense(it) },
            onDelete = { viewModel.deleteExpense(it) }
        )
    }
}
