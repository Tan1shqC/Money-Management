package com.moneyapp.ui.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val SOURCE_TYPES = listOf("UPI", "CARD", "BANK_SYNC", "MANUAL")
val TRANSACTION_STATES = listOf("LAUNCHED", "SUCCESS", "FAILED", "PENDING", "SYNCED")

@Composable
fun CreateTransactionScreen(
    viewModel: CreateTransactionViewModel,
    onTransactionCreated: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var sourceExpanded by remember { mutableStateOf(false) }
    var stateExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Transaction", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

        // Amount
        OutlinedTextField(
            value = if (state.amount == 0L) "" else "${state.amount / 100}",
            onValueChange = { input ->
                val paise = input.toLongOrNull()?.times(100) ?: 0L
                viewModel.setAmount(paise)
            },
            label = { Text("Amount (₹)") },
            placeholder = { Text("e.g., 1000") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Source Type
        OutlinedButton(
            onClick = { sourceExpanded = !sourceExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(state.sourceType)
        }

        DropdownMenu(
            expanded = sourceExpanded,
            onDismissRequest = { sourceExpanded = false }
        ) {
            SOURCE_TYPES.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type) },
                    onClick = {
                        viewModel.setSourceType(type)
                        sourceExpanded = false
                    }
                )
            }
        }

        // Transaction State
        OutlinedButton(
            onClick = { stateExpanded = !stateExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(state.state)
        }

        DropdownMenu(
            expanded = stateExpanded,
            onDismissRequest = { stateExpanded = false }
        ) {
            TRANSACTION_STATES.forEach { s ->
                DropdownMenuItem(
                    text = { Text(s) },
                    onClick = {
                        viewModel.setState(s)
                        stateExpanded = false
                    }
                )
            }
        }

        // External Txn ID
        OutlinedTextField(
            value = state.externalTxnId,
            onValueChange = { viewModel.setExternalTxnId(it) },
            label = { Text("External Txn ID (UTR)") },
            placeholder = { Text("Optional") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Error
        if (state.error != null) {
            Text(
                state.error!!,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        // Success
        if (state.success) {
            Text(
                "Transaction created!",
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
            viewModel.resetSuccess()
            onTransactionCreated()
        }

        // Create Button
        Button(
            onClick = { viewModel.createTransaction() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            Text(if (state.isLoading) "Creating..." else "Create Transaction")
        }
    }
}
