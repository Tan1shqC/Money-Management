package com.moneyapp.ui.event

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyapp.data.Event
import com.moneyapp.data.EventDao
import com.moneyapp.data.Fund
import com.moneyapp.data.FundDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CreateEventState(
    val description: String = "",
    val amountPaise: Long = 0L,
    val selectedFund: Fund? = null,
    val funds: List<Fund> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class CreateEventViewModel(
    private val eventDao: EventDao,
    private val fundDao: FundDao
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEventState())
    val state: StateFlow<CreateEventState> = _state

    init {
        loadFunds()
    }

    private fun loadFunds() {
        viewModelScope.launch {
            try {
                val funds = fundDao.getActive()
                _state.value = _state.value.copy(funds = funds)
                if (funds.isNotEmpty()) {
                    _state.value = _state.value.copy(selectedFund = funds[0])
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
            }
        }
    }

    fun setDescription(desc: String) {
        _state.value = _state.value.copy(description = desc)
    }

    fun setAmount(paise: Long) {
        _state.value = _state.value.copy(amountPaise = paise)
    }

    fun setFund(fund: Fund) {
        _state.value = _state.value.copy(selectedFund = fund)
    }

    fun createEvent() {
        val state = _state.value
        if (state.description.isBlank()) {
            _state.value = _state.value.copy(error = "Description required")
            return
        }
        if (state.amountPaise <= 0) {
            _state.value = _state.value.copy(error = "Amount must be > 0")
            return
        }
        if (state.selectedFund == null) {
            _state.value = _state.value.copy(error = "Fund required")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val event = Event(
                    amount = state.amountPaise,
                    description = state.description,
                    fundId = state.selectedFund.id,
                    state = "OPEN",
                    groupId = ""
                )
                eventDao.insert(event)
                _state.value = _state.value.copy(
                    isLoading = false,
                    success = true,
                    description = "",
                    amountPaise = 0L
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create event"
                )
            }
        }
    }

    fun resetSuccess() {
        _state.value = _state.value.copy(success = false)
    }
}
