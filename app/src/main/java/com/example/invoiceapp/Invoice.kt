package com.example.invoiceapp

import java.util.Date
import java.util.Dictionary

data class Invoice(
    val id: Int,
    val invoiceNumber: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val storeName: String,
    val amount: Int,
    val details: String
)
