package com.example.inventoryscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.inventoryscanner.data.InventoryRepository
import com.example.inventoryscanner.ui.screens.InventoryScreen
import com.example.inventoryscanner.ui.theme.InventoryScannerTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = InventoryRepository(applicationContext)
        val inventoryItems = MutableStateFlow(emptyList<com.example.inventoryscanner.model.InventoryItem>())
        lifecycleScope.launch { inventoryItems.value = repository.load() }
        setContent {
            InventoryScannerTheme {
                InventoryScreen(
                    items = inventoryItems,
                    onSave = { item -> lifecycleScope.launch {
                        repository.add(item)
                        inventoryItems.value = repository.load()
                    } }
                )
            }
        }
    }
}
