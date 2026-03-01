package com.moneyapp.ui.reconciliation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyapp.repo.EventWithRemaining
import com.moneyapp.data.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReconciliationScreen(viewModel: ReconciliationViewModel) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Reconciliation") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Controls
            ControlsBar(
                mode = state.mode,
                sortAxis = state.sortAxis,
                onToggleMode = { viewModel.toggleMode() },
                onChangeSortAxis = { viewModel.setSortAxis(it) },
                onLink = { viewModel.linkSelectedItems() },
                canLink = state.selectedEvent != null && state.selectedTransaction != null
            )

            Divider()

            // Two-pane view
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Events Pane
                EventsPane(
                    events = state.eventsWithRemaining,
                    selected = state.selectedEvent,
                    enabled = state.mode == ReconciliationMode.EDIT,
                    onSelect = { viewModel.selectEvent(it) },
                    modifier = Modifier.weight(1f)
                )

                Divider(
                    modifier = Modifier
                        .fillMaxSize(1f)
                        .padding(horizontal = 4.dp),
                    thickness = 1.dp
                )

                // Transactions Pane
                TransactionsPane(
                    transactions = state.transactions,
                    selected = state.selectedTransaction,
                    enabled = state.mode == ReconciliationMode.EDIT,
                    onSelect = { viewModel.selectTransaction(it) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ControlsBar(
    mode: ReconciliationMode,
    sortAxis: SortAxis,
    onToggleMode: () -> Unit,
    onChangeSortAxis: (SortAxis) -> Unit,
    onLink: () -> Unit,
    canLink: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onToggleMode,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (mode == ReconciliationMode.VIEW) "Switch to Edit" else "Switch to View")
            }

            Button(
                onClick = { onChangeSortAxis(SortAxis.BY_EVENT_DATE) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sortAxis == SortAxis.BY_EVENT_DATE)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("By Event Date")
            }

            Button(
                onClick = { onChangeSortAxis(SortAxis.BY_TRANSACTION_DATE) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (sortAxis == SortAxis.BY_TRANSACTION_DATE)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("By Txn Date")
            }
        }

        if (canLink) {
            Button(
                onClick = onLink,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Link Selected")
            }
        }
    }
}

@Composable
fun EventsPane(
    events: List<EventWithRemaining>,
    selected: EventWithRemaining?,
    enabled: Boolean,
    onSelect: (EventWithRemaining) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Events (${events.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(events) { event ->
                EventCard(
                    event = event,
                    isSelected = selected?.event?.id == event.event.id,
                    enabled = enabled,
                    onClick = { if (enabled) onSelect(event) }
                )
            }
        }
    }
}

@Composable
fun EventCard(
    event: EventWithRemaining,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        event.event.state == "RESOLVED" -> MaterialTheme.colorScheme.surface
        event.remaining == 0L -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .then(
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    event.event.description,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "₹${event.event.amount / 100}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                "Remaining: ₹${event.remaining / 100} | State: ${event.event.state}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                formatDate(event.event.createdAt),
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TransactionsPane(
    transactions: List<Transaction>,
    selected: Transaction?,
    enabled: Boolean,
    onSelect: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            "Transactions (${transactions.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.padding(8.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(transactions) { txn ->
                TransactionCard(
                    transaction = txn,
                    isSelected = selected?.id == txn.id,
                    enabled = enabled,
                    onClick = { if (enabled) onSelect(txn) }
                )
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        transaction.groupId.isEmpty() -> MaterialTheme.colorScheme.error.copy(alpha = 0.05f)
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .then(
                if (enabled) Modifier.clickable(onClick = onClick) else Modifier
            )
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    transaction.sourceType,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "₹${transaction.amount / 100}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                "State: ${transaction.state} | ${if (transaction.groupId.isEmpty()) "Unlinked" else "Linked"}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                formatDate(transaction.occurredAt),
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(ms: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(ms))
}
