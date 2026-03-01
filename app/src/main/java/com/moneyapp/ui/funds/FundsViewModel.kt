package com.moneyapp.ui.funds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyapp.data.Fund
import com.moneyapp.data.FundDao
import com.moneyapp.data.EventDao
import com.moneyapp.repo.EventWithRemaining
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class FundsListState(
    val funds: List<Fund> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFund: Fund? = null
)

data class FundDetailsState(
    val fund: Fund? = null,
    val events: List<EventWithRemaining> = emptyList(),
    val startDate: Long = getStartOfMonth(),
    val endDate: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CreateFundState(
    val name: String = "",
    val monthlyAllocation: Long = 0L,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class FundsViewModel(
    private val fundDao: FundDao,
    private val eventDao: EventDao
) : ViewModel() {

    private val _listState = MutableStateFlow(FundsListState())
    val listState: StateFlow<FundsListState> = _listState

    private val _detailsState = MutableStateFlow(FundDetailsState())
    val detailsState: StateFlow<FundDetailsState> = _detailsState

    private val _createState = MutableStateFlow(CreateFundState())
    val createState: StateFlow<CreateFundState> = _createState

    init {
        loadFunds()
    }

    fun loadFunds() {
        viewModelScope.launch {
            try {
                _listState.value = _listState.value.copy(isLoading = true, error = null)
                val funds = fundDao.getActive().sortedBy { it.name }
                _listState.value = _listState.value.copy(
                    funds = funds,
                    isLoading = false
                )
            } catch (e: Exception) {
                _listState.value = _listState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun selectFund(fund: Fund) {
        _listState.value = _listState.value.copy(selectedFund = fund)
        loadFundDetails(fund)
    }

    fun loadFundDetails(fund: Fund, startDate: Long = getStartOfMonth(), endDate: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            try {
                _detailsState.value = _detailsState.value.copy(isLoading = true, error = null)
                val allEvents = eventDao.getAll()
                
                // Filter events for this fund and date range
                val filteredEvents = allEvents
                    .filter { it.fundId == fund.id && it.createdAt in startDate..endDate }
                    .map { event ->
                        val linkedAmount = calculateLinkedAmount(event.id)
                        val remaining = event.amount - linkedAmount
                        EventWithRemaining(event, remaining, linkedAmount)
                    }
                    .sortedByDescending { it.event.createdAt }

                _detailsState.value = _detailsState.value.copy(
                    fund = fund,
                    events = filteredEvents,
                    startDate = startDate,
                    endDate = endDate,
                    isLoading = false
                )
            } catch (e: Exception) {
                _detailsState.value = _detailsState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }

    fun setDateRange(startDate: Long, endDate: Long) {
        val fund = _detailsState.value.fund ?: return
        loadFundDetails(fund, startDate, endDate)
    }

    private suspend fun calculateLinkedAmount(eventId: String): Long {
        // Simplified: just return 0 for now, could expand to check LinkingGroup
        return 0L
    }

    // Fund creation functions
    fun setCreateFundName(name: String) {
        _createState.value = _createState.value.copy(name = name)
    }

    fun setCreateFundAllocation(paise: Long) {
        _createState.value = _createState.value.copy(monthlyAllocation = paise)
    }

    fun createFund() {
        val state = _createState.value
        if (state.name.isBlank()) {
            _createState.value = _createState.value.copy(error = "Fund name required")
            return
        }
        if (state.monthlyAllocation <= 0L) {
            _createState.value = _createState.value.copy(error = "Allocation must be > 0")
            return
        }

        viewModelScope.launch {
            try {
                _createState.value = _createState.value.copy(isLoading = true, error = null)
                val fund = Fund(
                    name = state.name,
                    monthlyAllocation = state.monthlyAllocation,
                    currentBalance = state.monthlyAllocation
                )
                fundDao.insert(fund)
                _createState.value = _createState.value.copy(
                    isLoading = false,
                    success = true,
                    name = "",
                    monthlyAllocation = 0L
                )
                loadFunds()
            } catch (e: Exception) {
                _createState.value = _createState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create fund"
                )
            }
        }
    }

    fun resetCreateSuccess() {
        _createState.value = _createState.value.copy(success = false)
    }

    fun clearSelection() {
        _listState.value = _listState.value.copy(selectedFund = null)
    }

    // Calculate total spend and balance for fund analytics
    fun calculateFundBalance(fundBalance: Long, events: List<EventWithRemaining>): Long {
        val totalSpend = events.sumOf { it.event.amount }
        return fundBalance - totalSpend
    }

    fun calculateTotalSpend(events: List<EventWithRemaining>): Long {
        return events.sumOf { it.event.amount }
    }
}

private fun getStartOfMonth(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}
