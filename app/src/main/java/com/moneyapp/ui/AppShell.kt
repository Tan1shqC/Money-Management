package com.moneyapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneyapp.data.MoneyDatabase
import com.moneyapp.repo.ReconciliationRepository
import com.moneyapp.ui.event.CreateEventScreen
import com.moneyapp.ui.event.CreateEventViewModel
import com.moneyapp.ui.funds.FundsListScreen
import com.moneyapp.ui.funds.FundDetailsScreen
import com.moneyapp.ui.funds.FundsViewModel
import com.moneyapp.ui.reconciliation.ReconciliationScreen
import com.moneyapp.ui.reconciliation.ReconciliationViewModel
import com.moneyapp.ui.transaction.CreateTransactionScreen
import com.moneyapp.ui.transaction.CreateTransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(database: MoneyDatabase) {
    var currentPage by remember { mutableStateOf(AppPage.RECONCILIATION) }
    var sidebarOpen by remember { mutableStateOf(false) }

    // ViewModels
    val reconciliationRepo = ReconciliationRepository(
        database.eventDao(),
        database.transactionDao(),
        database.linkingGroupDao()
    )
    val reconciliationVM = viewModel { ReconciliationViewModel(reconciliationRepo) }
    val createEventVM = viewModel {
        CreateEventViewModel(database.eventDao(), database.fundDao())
    }
    val createTransactionVM = viewModel {
        CreateTransactionViewModel(database.transactionDao())
    }
    val fundsVM = viewModel {
        FundsViewModel(database.fundDao(), database.eventDao())
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Money Manager") },
                navigationIcon = {
                    IconButton(onClick = { sidebarOpen = !sidebarOpen }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sidebar
            if (sidebarOpen) {
                Sidebar(
                    currentPage = currentPage,
                    onPageSelect = { page ->
                        currentPage = page
                        sidebarOpen = false
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Divider(modifier = Modifier.fillMaxHeight(1f).width(1.dp))
            }

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(enabled = sidebarOpen) {
                        sidebarOpen = false
                    }
            ) {
                when (currentPage) {
                    AppPage.RECONCILIATION -> ReconciliationScreen(reconciliationVM)
                    AppPage.CREATE_EVENT -> CreateEventScreen(createEventVM) {
                        currentPage = AppPage.RECONCILIATION
                    }
                    AppPage.CREATE_TRANSACTION -> CreateTransactionScreen(createTransactionVM) {
                        currentPage = AppPage.RECONCILIATION
                    }
                    AppPage.FUNDS -> FundsListScreen(fundsVM) {
                        currentPage = AppPage.FUND_DETAILS
                    }
                    AppPage.FUND_DETAILS -> FundDetailsScreen(fundsVM) {
                        currentPage = AppPage.FUNDS
                    }
                }
            }
        }
    }
}

@Composable
fun Sidebar(
    currentPage: AppPage,
    onPageSelect: (AppPage) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Menu",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        SidebarItem(
            label = "Reconciliation",
            isSelected = currentPage == AppPage.RECONCILIATION,
            onClick = { onPageSelect(AppPage.RECONCILIATION) }
        )

        SidebarItem(
            label = "Create Event",
            isSelected = currentPage == AppPage.CREATE_EVENT,
            onClick = { onPageSelect(AppPage.CREATE_EVENT) }
        )

        SidebarItem(
            label = "Create Transaction",
            isSelected = currentPage == AppPage.CREATE_TRANSACTION,
            onClick = { onPageSelect(AppPage.CREATE_TRANSACTION) }
        )

        SidebarItem(
            label = "Funds",
            isSelected = currentPage == AppPage.FUNDS || currentPage == AppPage.FUND_DETAILS,
            onClick = { onPageSelect(AppPage.FUNDS) }
        )
    }
}

@Composable
fun SidebarItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}
