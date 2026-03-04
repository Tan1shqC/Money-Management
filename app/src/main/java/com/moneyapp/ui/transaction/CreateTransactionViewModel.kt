package com.moneyapp.ui.transaction

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneyapp.data.Transaction
import com.moneyapp.data.TransactionDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CreateTransactionState(
    val amount: Long = 0L,
    val sourceType: String = "UPI",
    val state: String = "SUCCESS",
    val externalTxnId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class CreateTransactionViewModel(
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTransactionState())
    val state: StateFlow<CreateTransactionState> = _state

    fun setAmount(paise: Long) {
        _state.value = _state.value.copy(amount = paise)
    }

    fun setSourceType(type: String) {
        _state.value = _state.value.copy(sourceType = type)
    }

    fun setState(s: String) {
        _state.value = _state.value.copy(state = s)
    }

    fun setExternalTxnId(id: String) {
        _state.value = _state.value.copy(externalTxnId = id)
    }

    fun createTransaction() {
        Log.i("Creating Transaction", "")
        val state = _state.value
        if (state.amount <= 0L) {
            _state.value = _state.value.copy(error = "Amount must be > 0")
            return
        }

        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true, error = null)
                val txn = Transaction(
                    amount = state.amount,
                    sourceType = state.sourceType,
                    state = state.state,
                    externalTxnId = state.externalTxnId.ifBlank { null },
                    groupId = ""
                )
                transactionDao.insert(txn)
                _state.value = _state.value.copy(
                    isLoading = false,
                    success = true
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create transaction"
                )
            }
        }
    }

    fun resetSuccess() {
        _state.value = _state.value.copy(success = false)
    }
}
