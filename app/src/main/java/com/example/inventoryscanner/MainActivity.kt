package com.example.inventoryscanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventoryscanner.ui.theme.InventoryScannerTheme

private data class InventoryItem(
    val name: String,
    val tags: String,
    val rfidId: String,
    val description: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { InventoryScannerTheme { InventoryApp() } }
    }
}

@Composable
private fun InventoryApp() {
    var items by remember { mutableStateOf(emptyList<InventoryItem>()) }
    var query by remember { mutableStateOf("") }
    var adding by remember { mutableStateOf(false) }
    val ink = Color(0xFF17211D)
    val muted = Color(0xFF75817B)
    val green = Color(0xFF286B50)
    val bg = Color(0xFFF6F7F3)

    if (adding) {
        AddItemScreen(
            onBack = { adding = false },
            onSave = { item -> items = listOf(item) + items; adding = false },
            ink = ink, muted = muted, green = green, bg = bg
        )
        return
    }

    Scaffold(containerColor = bg, floatingActionButton = {
        FloatingActionButton(onClick = { adding = true }, containerColor = green, contentColor = Color.White) {
            Icon(Icons.Default.Add, contentDescription = "Add item")
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 22.dp)) {
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
                Text("${items.size}", color = green, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Color(0xFFE4EEE7), CircleShape).padding(horizontal = 9.dp, vertical = 4.dp))
            }
            Spacer(Modifier.height(12.dp))
            val filtered = items.filter { item ->
                listOf(item.name, item.tags, item.rfidId, item.description).any { it.contains(query, ignoreCase = true) }
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

@Composable
private fun AddItemScreen(onBack: () -> Unit, onSave: (InventoryItem) -> Unit, ink: Color, muted: Color, green: Color, bg: Color) {
    var name by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var rfid by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var scanMessage by remember { mutableStateOf("Barcode scanner ready") }
    Scaffold(containerColor = bg, bottomBar = {
        Surface(color = bg, shadowElevation = 8.dp) {
            Button(onClick = { if (name.isNotBlank()) onSave(InventoryItem(name.trim(), tags.trim(), rfid.trim(), description.trim())) },
                enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp).height(54.dp),
                shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = green)) {
                Text("Save item", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(16.dp))
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp).background(Color.White, CircleShape)) { Icon(Icons.Default.ArrowBack, "Back", tint = ink) }
            Spacer(Modifier.height(18.dp))
            Text("ADD TO INVENTORY", color = green, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Text("Scan your item", color = ink, fontSize = 27.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
            Text("Start with a barcode, then add the details.", color = muted, fontSize = 14.sp, modifier = Modifier.padding(top = 5.dp))
            Spacer(Modifier.height(22.dp))
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0E9))) {
                Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("▥", color = green, fontSize = 38.sp)
                    Text(scanMessage, color = ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 5.dp))
                    Text("Camera scanning will be connected in the next build step.", color = muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    OutlinedButton(onClick = { scanMessage = "Scanner integration coming next" }, modifier = Modifier.padding(top = 12.dp), shape = RoundedCornerShape(12.dp)) {
                        Text("Scan barcode", color = green)
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            Text("ITEM DETAILS", color = muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(10.dp))
            FormField("Name", name, { name = it }, "e.g. Camera body", true)
            FormField("Tags", tags, { tags = it }, "e.g. electronics, studio", false)
            FormField("RFID ID", rfid, { rfid = it }, "Scan or enter tag ID", false)
            FormField("Description", description, { description = it }, "Notes about this item", false, singleLine = false)
        }
    }
}

@Composable
private fun FormField(label: String, value: String, onValueChange: (String) -> Unit, hint: String, required: Boolean, singleLine: Boolean = true) {
    OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        label = { Text(if (required) "$label *" else label) }, placeholder = { Text(hint) }, singleLine = singleLine,
        minLines = if (singleLine) 1 else 3, shape = RoundedCornerShape(14.dp), colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White, focusedContainerColor = Color.White,
            unfocusedBorderColor = Color(0xFFE4E8E2), focusedBorderColor = Color(0xFF286B50)
        ))
}
