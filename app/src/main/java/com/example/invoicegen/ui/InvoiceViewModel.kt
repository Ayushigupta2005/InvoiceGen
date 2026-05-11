package com.example.invoicegen.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.invoicegen.data.CompanySettings
import com.example.invoicegen.data.SettingsRepository
import com.example.invoicegen.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InvoiceViewModel(private val repository: SettingsRepository) : ViewModel() {

    val companySettings = repository.companySettings
        .map { it ?: CompanySettings() }
        .onEach { settings ->
            if (_header.value.invoiceNo.isEmpty()) {
                _header.value = _header.value.copy(invoiceNo = settings.nextInvoiceNumber.toString())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CompanySettings()
        )

    private val _header = MutableStateFlow(InvoiceHeader())
    val header: StateFlow<InvoiceHeader> = _header.asStateFlow()

    private val _items = MutableStateFlow(listOf(InvoiceItem()))
    val items: StateFlow<List<InvoiceItem>> = _items.asStateFlow()

    private val _transport = MutableStateFlow(TransportDetails())
    val transport: StateFlow<TransportDetails> = _transport.asStateFlow()

    private val _summary = MutableStateFlow(InvoiceSummary())
    val summary: StateFlow<InvoiceSummary> = _summary.asStateFlow()

    fun updateHeader(newHeader: InvoiceHeader) {
        _header.value = newHeader
    }

    fun incrementNextInvoiceNumber() {
        viewModelScope.launch {
            val currentSettings = companySettings.value
            val currentInvoiceNo = _header.value.invoiceNo.toIntOrNull() ?: currentSettings.nextInvoiceNumber
            val nextNo = currentInvoiceNo + 1
            repository.updateSettings(currentSettings.copy(nextInvoiceNumber = nextNo))
            // Update the UI header to the next invoice number
            _header.value = _header.value.copy(invoiceNo = nextNo.toString())
        }
    }

    fun addItem() {
        _items.value = _items.value + InvoiceItem()
    }

    fun removeItem(index: Int) {
        if (_items.value.size > 1) {
            val newList = _items.value.toMutableList()
            newList.removeAt(index)
            _items.value = newList
        }
    }

    fun updateItem(index: Int, newItem: InvoiceItem) {
        val newList = _items.value.toMutableList()
        newList[index] = newItem
        _items.value = newList
    }

    fun updateTransport(newTransport: TransportDetails) {
        _transport.value = newTransport
    }

    fun updateSummary(newSummary: InvoiceSummary) {
        _summary.value = newSummary
    }

    fun saveSettings(settings: CompanySettings) {
        viewModelScope.launch {
            repository.updateSettings(settings)
        }
    }

    val totalAmount: Double
        get() = _items.value.sumOf { it.amount }

    val grandTotal: Double
        get() {
            val total = totalAmount
            val adv = _summary.value.advance.toDoubleOrNull() ?: 0.0
            val ins = _summary.value.insurance.toDoubleOrNull() ?: 0.0
            return total + adv + ins
        }
}
