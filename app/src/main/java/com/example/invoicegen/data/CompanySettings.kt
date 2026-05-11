package com.example.invoicegen.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_settings")
data class CompanySettings(
    @PrimaryKey val id: Int = 1,
    val companyName: String = "",
    val subtitle: String = "",
    val address: String = "",
    val gstin: String = "",
    val mobileNumbers: String = "",
    val iciciAccountNo: String = "",
    val iciciIfsc: String = "",
    val pnbAccountNo: String = "",
    val pnbIfsc: String = "",
    val termsAndConditions: String = "",
    val nextInvoiceNumber: Int = 1,
    val logoPath: String = ""
)