package com.moneyapp.ui.reconciliation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyapp.data.Event
import com.moneyapp.data.Transaction
import com.moneyapp.repo.ReconciliationRepository
import com.moneyapp.repo.EventWithRemaining
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ReconciliationMode {
    VIEW, EDIT
}

enum class SortAxis {
    BY_EVENT_DATE, BY_TRANSACTION_DATE
}

data class ReconciliationState(
    val eventsWithRemaining: List<EventWithRemaining> = emptyList(),
    val transactions: List<Transaction> = emptyList(),
    val mode: ReconciliationMode = ReconciliationMode.VIEW,
    val sortAxis: SortAxis = SortAxis.BY_EVENT_DATE,
    val selectedEvent: EventWithRemaining? = null,
    val selectedTransaction: Transaction? = null,
    val filterFundId: String? = null,
    val filterEventState: String? = null,
    val filterLinked: Boolean? = null,
    val dateRangeStart: Long? = null,
    val dateRangeEnd: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReconciliationViewModel(
    private val repo: ReconciliationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReconciliationState())
    val state: StateFlow<ReconciliationState> = _state

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val events = repo.getEventsWithRemaining()
                val txns = repo.getAllTransactions()
                val filtered = applyFilters(events, txns)
                _state.value = _state.value.copy(
                    eventsWithRemaining = filtered.first,
                    transactions = filtered.second,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = e.message ?: "Unknown error",
                    isLoading = false
                )
            }
        }
    }

    fun toggleMode() {
        _state.value = _state.value.copy(
            mode = if (_state.value.mode == ReconciliationMode.VIEW) 
                ReconciliationMode.EDIT else ReconciliationMode.VIEW
        )
    }

    fun setSortAxis(axis: SortAxis) {
        _state.value = _state.value.copy(sortAxis = axis)
        sortData()
    }

    fun selectEvent(event: EventWithRemaining) {
        if (_state.value.mode == ReconciliationMode.EDIT) {
            _state.value = _state.value.copy(selectedEvent = event)
        }
    }

    fun selectTransaction(transaction: Transaction) {
        if (_state.value.mode == ReconciliationMode.EDIT) {
            _state.value = _state.value.copy(selectedTransaction = transaction)
        }
    }

    fun linkSelectedItems() {
        val event = _state.value.selectedEvent ?: return
        val txn = _state.value.selectedTransaction ?: return

        viewModelScope.launch {
            try {
                val group = repo.createLinkingGroup()
                repo.linkEventToGroup(event.event, group.id)
                repo.linkTransactionToGroup(txn, group.id)

                // Check if fully linked
                if (repo.isGroupFullyLinked(group.id)) {
                    repo.resolveEvent(event.event)
                }

                // Refresh and clear selection
                _state.value = _state.value.copy(
                    selectedEvent = null,
                    selectedTransaction = null
                )
                loadData()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun unlinkItem(isEvent: Boolean) {
        viewModelScope.launch {
            try {
                if (isEvent) {
                    val event = _state.value.selectedEvent?.event ?: return@launch
                    repo.unlinkEvent(event)
                    _state.value = _state.value.copy(selectedEvent = null)
                } else {
                    val txn = _state.value.selectedTransaction ?: return@launch
                    repo.unlinkTransaction(txn)
                    _state.value = _state.value.copy(selectedTransaction = null)
                }
                loadData()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun setFilterFund(fundId: String?) {
        _state.value = _state.value.copy(filterFundId = fundId)
        loadData()
    }

    fun setFilterEventState(state: String?) {
        _state.value = _state.value.copy(filterEventState = state)
        loadData()
    }

    fun setFilterLinked(linked: Boolean?) {
        _state.value = _state.value.copy(filterLinked = linked)
        loadData()
    }

    fun setDateRange(start: Long?, end: Long?) {
        _state.value = _state.value.copy(
            dateRangeStart = start,
            dateRangeEnd = end
        )
        loadData()
    }

    private fun applyFilters(
        events: List<EventWithRemaining>,
        txns: List<Transaction>
    ): Pair<List<EventWithRemaining>, List<Transaction>> {
        val state = _state.value
        var filteredEvents = events

        // Fund filter
        state.filterFundId?.let {
            filteredEvents = filteredEvents.filter { e -> e.event.fundId == it }
        }

        // Event state filter
        state.filterEventState?.let {
            filteredEvents = filteredEvents.filter { e -> e.event.state == it }
        }

        // Linked filter
        state.filterLinked?.let { linked ->
            filteredEvents = if (linked) {
                filteredEvents.filter { e -> e.event.groupId.isNotEmpty() }
            } else {
                filteredEvents.filter { e -> e.event.groupId.isEmpty() }
            }
        }

        // Date range
        state.dateRangeStart?.let { start ->
            state.dateRangeEnd?.let { end ->
                filteredEvents = filteredEvents.filter { e -> e.event.createdAt in start..end }
            }
        }

        var filteredTxns = txns
        state.filterLinked?.let { linked ->
            filteredTxns = if (linked) {
                filteredTxns.filter { it.groupId.isNotEmpty() }
            } else {
                filteredTxns.filter { it.groupId.isEmpty() }
            }
        }

        return Pair(filteredEvents, filteredTxns)
    }

    private fun sortData() {
        val events = _state.value.eventsWithRemaining
        val txns = _state.value.transactions

        val sortedEvents = when (_state.value.sortAxis) {
            SortAxis.BY_EVENT_DATE -> events.sortedByDescending { it.event.createdAt }
            SortAxis.BY_TRANSACTION_DATE -> events
        }

        val sortedTxns = when (_state.value.sortAxis) {
            SortAxis.BY_EVENT_DATE -> txns
            SortAxis.BY_TRANSACTION_DATE -> txns.sortedByDescending { it.occurredAt }
        }

        _state.value = _state.value.copy(
            eventsWithRemaining = sortedEvents,
            transactions = sortedTxns
        )
    }
}
