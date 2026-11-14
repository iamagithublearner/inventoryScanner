package com.example.inventoryscanner.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventoryscanner.model.InventoryItem

@Composable
fun InventoryScreen(items: Flow<List<InventoryItem>>, onSave: (InventoryItem) -> Unit) {
    val inventoryItems by items.collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }
    var adding by remember { mutableStateOf(false) }
    val ink = Color(0xFF17211D)
    val muted = Color(0xFF75817B)
    val green = Color(0xFF286B50)
    val bg = Color(0xFFF6F7F3)

    if (adding) {
        AddItemScreen(
            onBack = { adding = false },
            onSave = { item -> onSave(item); adding = false },
            ink = ink, muted = muted, green = green, bg = bg
        )
        return
    }

    Scaffold(containerColor = bg, floatingActionButton = {
        FloatingActionButton(onClick = { adding = true }, containerColor = green, contentColor = Color.White) {
            Icon(Icons.Default.Add, contentDescription = "Add item")
        }
    }) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(28.dp))
            Text("FIELDNOTE", color = green, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Spacer(Modifier.height(10.dp))
            Text("Your inventory", color = ink, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text("Keep track of what you own and where it is.", color = muted, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search items, tags or RFID") }, leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, "Clear search") } },
                shape = RoundedCornerShape(16.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White, focusedContainerColor = Color.White,
                    unfocusedBorderColor = Color(0xFFE4E8E2), focusedBorderColor = green
                )
            )
            Spacer(Modifier.height(28.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("ITEMS", color = muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
                Spacer(Modifier.width(8.dp))
                Text("${inventoryItems.size}", color = green, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Color(0xFFE4EEE7), CircleShape).padding(horizontal = 9.dp, vertical = 4.dp))
            }
            Spacer(Modifier.height(12.dp))
            val filtered = inventoryItems.filter { item ->
                listOf(item.name, item.tags, item.barcode, item.rfidId, item.description).any { it.contains(query, ignoreCase = true) }
            }
            if (filtered.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(top = 58.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(64.dp).background(Color(0xFFE7EEE8), CircleShape), contentAlignment = Alignment.Center) {
                        Text("＋", color = green, fontSize = 30.sp)
                    }
                    Spacer(Modifier.height(18.dp))
                    Text(if (query.isBlank()) "Nothing in your inventory yet" else "No matching items", color = ink, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
                    Text(if (query.isBlank()) "Tap + to scan and add your first item." else "Try a different name, tag or RFID ID.", color = muted, fontSize = 14.sp, modifier = Modifier.padding(top = 7.dp))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filtered) { item ->
                        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Text(item.name, color = ink, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                if (item.description.isNotBlank()) Text(item.description, color = muted, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
                                Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (item.tags.isNotBlank()) SmallPill(item.tags, green)
                                    if (item.barcode.isNotBlank()) SmallPill("BARCODE · ${item.barcode}", green)
                                    if (item.rfidId.isNotBlank()) SmallPill("RFID · ${item.rfidId}", green)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallPill(label: String, green: Color) {
    Text(label, color = green, fontSize = 11.sp, modifier = Modifier.background(Color(0xFFEAF1EB), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 6.dp))
}
