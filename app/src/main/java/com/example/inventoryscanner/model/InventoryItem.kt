package com.example.inventoryscanner.model

data class InventoryItem(
    val name: String,
    val tags: String,
    val barcode: String,
    val rfidId: String,
    val description: String
)
