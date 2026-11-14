package com.example.inventoryscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.inventoryscanner.ui.screens.InventoryScreen
import com.example.inventoryscanner.ui.theme.InventoryScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InventoryScannerTheme {
                InventoryScreen()
            }
        }
    }
}
