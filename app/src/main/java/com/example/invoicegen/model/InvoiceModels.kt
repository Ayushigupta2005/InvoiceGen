package com.example.invoicegen.model

data class InvoiceItem(
    val particulars: String = "",
    val qtyBags: String = "",
    val weightPerBag: String = "",
    val ratePerQtl: String = ""
) {
    val netWeightQtl: Double
        get() = try {
            val bags = qtyBags.toDoubleOrNull() ?: 0.0
            val weight = weightPerBag.toDoubleOrNull() ?: 0.0
            (bags * weight) / 100.0
        } catch (e: Exception) { 0.0 }

    val amount: Double
        get() = try {
            val rate = ratePerQtl.toDoubleOrNull() ?: 0.0
            netWeightQtl * rate
        } catch (e: Exception) { 0.0 }
}

data class InvoiceHeader(
    val invoiceNo: String = "",
    val date: String = "",
    val nameOfParty: String = "",
    val broker: String = "",
    val address: String = "",
    val truckNo: String = "",
    val mobileNo: String = "",
    val transport: String = "",
    val transhipment: String = "",
    val gstin: String = "",
    val stateCode: String = ""
)

data class TransportDetails(
    val driverName: String = "",
    val licenseNo: String = "",
    val mobileNo: String = "",
    val truckOwner: String = "",
    val engineNo: String = "",
    val chassisNo: String = ""
)

data class InvoiceSummary(
    val advance: String = "0",
    val insurance: String = "0"
)
