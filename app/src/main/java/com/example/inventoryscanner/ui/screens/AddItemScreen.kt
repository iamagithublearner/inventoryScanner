package com.example.inventoryscanner.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.inventoryscanner.model.InventoryItem

@Composable
fun AddItemScreen(
    onBack: () -> Unit,
    onSave: (InventoryItem) -> Unit,
    ink: Color,
    muted: Color,
    green: Color,
    bg: Color
) {
    var name by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var barcode by remember { mutableStateOf("") }
    var rfid by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var scanError by remember { mutableStateOf<String?>(null) }
    var scanning by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val cameraPermission = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) { scanning = true; scanError = null } else scanError = "Camera permission is needed to scan a barcode."
    }
    fun launchScanner() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            scanning = true; scanError = null
        } else cameraPermission.launch(Manifest.permission.CAMERA)
    }
    if (scanning) {
        BarcodeCameraScreen(
            onBarcode = { barcode = it; scanning = false },
            onClose = { scanning = false },
            onError = { scanError = it; scanning = false }
        )
        return
    }
    Scaffold(containerColor = bg, bottomBar = {
        Surface(color = bg, shadowElevation = 8.dp) {
            Button(onClick = { if (name.isNotBlank()) onSave(InventoryItem(name.trim(), tags.trim(), barcode.trim(), rfid.trim(), description.trim())) },
                enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp).height(54.dp),
                shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = green)) {
                Text("Save item", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
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
            Spacer(Modifier.height(16.dp))
            IconButton(onClick = onBack, modifier = Modifier.size(44.dp).background(Color.White, CircleShape)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = ink) }
            Spacer(Modifier.height(18.dp))
            Text("ADD TO INVENTORY", color = green, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Text("Scan your item", color = ink, fontSize = 27.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
            Text("Start with a barcode, then add the details.", color = muted, fontSize = 14.sp, modifier = Modifier.padding(top = 5.dp))
            Spacer(Modifier.height(22.dp))
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0E9))) {
                Column(Modifier.fillMaxWidth().padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("▥", color = green, fontSize = 38.sp)
                    Text(if (barcode.isBlank()) "Barcode scanner ready" else "Barcode scanned", color = ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 5.dp))
                    Text(if (barcode.isBlank()) "Use your camera to scan the item's barcode." else barcode, color = muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    OutlinedButton(onClick = { launchScanner() }, modifier = Modifier.padding(top = 12.dp), shape = RoundedCornerShape(12.dp)) {
                        Text(if (barcode.isBlank()) "Scan barcode" else "Scan again", color = green)
                    }
                    scanError?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
                }
            }
            Spacer(Modifier.height(22.dp))
            Text("ITEM DETAILS", color = muted, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
            Spacer(Modifier.height(10.dp))
            FormField("Name", name, { name = it }, "e.g. Camera body", true)
            FormField("Tags", tags, { tags = it }, "e.g. electronics, studio", false)
            FormField("Barcode", barcode, { barcode = it }, "Scanned barcode value", false)
            FormField("RFID ID", rfid, { rfid = it }, "Scan or enter tag ID", false)
            FormField("Description", description, { description = it }, "Notes about this item", false, singleLine = false)
            Spacer(Modifier.height(24.dp))
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
