package com.moneyapp.ui.event

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
import com.moneyapp.data.Fund

@Composable
fun CreateEventScreen(
    viewModel: CreateEventViewModel,
    onEventCreated: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Event", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)

        // Description
        OutlinedTextField(
            value = state.description,
            onValueChange = { viewModel.setDescription(it) },
            label = { Text("Description") },
            placeholder = { Text("e.g., Dinner at cafe") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Amount
        OutlinedTextField(
            value = if (state.amountPaise == 0L) "" else "${state.amountPaise / 100}",
            onValueChange = { input ->
                val paise = input.toLongOrNull()?.times(100) ?: 0L
                viewModel.setAmount(paise)
            },
            label = { Text("Amount (₹)") },
            placeholder = { Text("e.g., 500") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Fund Selector
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(state.selectedFund?.name ?: "Select Fund")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            state.funds.forEach { fund ->
                DropdownMenuItem(
                    text = { Text(fund.name) },
                    onClick = {
                        viewModel.setFund(fund)
                        expanded = false
                    }
                )
            }
        }

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
                "Event created!",
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
            viewModel.resetSuccess()
            onEventCreated()
        }

        // Create Button
        Button(
            onClick = { viewModel.createEvent() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoading
        ) {
            Text(if (state.isLoading) "Creating..." else "Create Event")
        }
    }
}
