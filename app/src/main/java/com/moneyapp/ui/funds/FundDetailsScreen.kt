package com.moneyapp.ui.funds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.moneyapp.data.Fund
import com.moneyapp.repo.EventWithRemaining
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundDetailsScreen(
    viewModel: FundsViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.detailsState.collectAsState()
    val fund = state.fund ?: return

    var startDateStr by remember { mutableStateOf(formatDate(state.startDate)) }
    var endDateStr by remember { mutableStateOf(formatDate(state.endDate)) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(fund.name) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fund Header with Balance
            FundSummaryCard(fund, state.events, viewModel)

            // Date Range Filters
            Text("Date Range", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = startDateStr,
                    onValueChange = { startDateStr = it },
                    label = { Text("From") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = endDateStr,
                    onValueChange = { endDateStr = it },
                    label = { Text("To") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
            Button(
                onClick = {
                    // Parse dates - simplified, expects MM/dd/yyyy format
                    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                    try {
                        val startDate = sdf.parse(startDateStr)?.time ?: state.startDate
                        val endDate = sdf.parse(endDateStr)?.time ?: state.endDate
                        viewModel.setDateRange(startDate, endDate)
                    } catch (e: Exception) {
                        // Ignore parse errors for now
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filter")
            }

            // Events List
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (state.events.isEmpty()) {
                Text(
                    "No events in this period.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                Text(
                    "Events (${state.events.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.events) { event ->
                        EventAnalyticsCard(event)
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
}

@Composable
fun FundSummaryCard(
    fund: Fund,
    events: List<EventWithRemaining>,
    viewModel: FundsViewModel
) {
    val totalSpend = viewModel.calculateTotalSpend(events)
    val remainingBalance = viewModel.calculateFundBalance(fund.currentBalance, events)

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Monthly Allocation", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${fund.monthlyAllocation / 100}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total Spend", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${totalSpend / 100}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                }
            }

            Text(
                "separator",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Current Balance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "₹${remainingBalance / 100}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = if (remainingBalance >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EventAnalyticsCard(event: EventWithRemaining) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        event.event.description,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        "State: ${event.event.state}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₹${event.event.amount / 100}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        formatDateShort(event.event.createdAt),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatDate(ms: Long): String {
    val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return sdf.format(Date(ms))
}

private fun formatDateShort(ms: Long): String {
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    return sdf.format(Date(ms))
}
