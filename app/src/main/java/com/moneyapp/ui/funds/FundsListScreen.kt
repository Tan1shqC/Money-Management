package com.moneyapp.ui.funds

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.AlertDialog
import com.moneyapp.data.Fund
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FundsListScreen(
    viewModel: FundsViewModel,
    onFundSelected: (Fund) -> Unit
) {
    val state by viewModel.listState.collectAsState()
    val createState by viewModel.createState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Fund")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Funds",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (state.funds.isEmpty()) {
                Text(
                    "No funds created yet. Tap + to add one.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.funds) { fund ->
                        FundCard(
                            fund = fund,
                            onClick = {
                                viewModel.selectFund(fund)
                                onFundSelected(fund)
                            }
                        )
                    }
                }
            }

            if (state.error != null) {
                Text(
                    state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateFundDialog(
            state = createState,
            onNameChange = { viewModel.setCreateFundName(it) },
            onAllocationChange = { viewModel.setCreateFundAllocation(it) },
            onCreateClick = {
                viewModel.createFund()
                if (createState.success) {
                    showCreateDialog = false
                }
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (createState.success) {
        viewModel.resetCreateSuccess()
    }
}

@Composable
fun FundCard(
    fund: Fund,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        fund.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        "Monthly: ₹${fund.monthlyAllocation / 100}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "₹${fund.currentBalance / 100}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (fund.currentBalance >= 0) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun CreateFundDialog(
    state: CreateFundState,
    onNameChange: (String) -> Unit,
    onAllocationChange: (Long) -> Unit,
    onCreateClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Fund") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text("Fund Name") },
                    placeholder = { Text("e.g., Office Travel") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = if (state.monthlyAllocation == 0L) "" else "${state.monthlyAllocation / 100}",
                    onValueChange = { input ->
                        val paise = input.toLongOrNull()?.times(100) ?: 0L
                        onAllocationChange(paise)
                    },
                    label = { Text("Monthly Allocation (₹)") },
                    placeholder = { Text("e.g., 5000") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (state.error != null) {
                    Text(
                        state.error!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCreateClick,
                enabled = !state.isLoading
            ) {
                Text(if (state.isLoading) "Creating..." else "Create")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
