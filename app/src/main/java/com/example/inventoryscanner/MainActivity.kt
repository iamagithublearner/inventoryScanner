package com.example.inventoryscanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.inventoryscanner.ui.theme.InventoryScannerTheme
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

private data class InventoryItem(
    val name: String,
    val tags: String,
    val barcode: String,
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
                Text("${items.size}", color = green, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.background(Color(0xFFE4EEE7), CircleShape).padding(horizontal = 9.dp, vertical = 4.dp))
            }
            Spacer(Modifier.height(12.dp))
            val filtered = items.filter { item ->
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

@Composable
private fun AddItemScreen(onBack: () -> Unit, onSave: (InventoryItem) -> Unit, ink: Color, muted: Color, green: Color, bg: Color) {
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
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 22.dp)) {
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
        }
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
private fun BarcodeCameraScreen(onBarcode: (String) -> Unit, onClose: () -> Unit, onError: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val scanner = remember {
        BarcodeScanning.getClient(BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build())
    }
    DisposableEffect(Unit) {
        onDispose { scanner.close(); executor.shutdown() }
    }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(factory = { ctx ->
            val previewView = PreviewView(ctx)
            val providerFuture = ProcessCameraProvider.getInstance(ctx)
            providerFuture.addListener({
                try {
                    val provider = providerFuture.get()
                    val preview = Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                    val analysis = ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                    analysis.setAnalyzer(executor) { proxy ->
                        val mediaImage = proxy.image
                        if (mediaImage == null) proxy.close() else {
                            val image = InputImage.fromMediaImage(mediaImage, proxy.imageInfo.rotationDegrees)
                            scanner.process(image)
                                .addOnSuccessListener { codes -> codes.firstOrNull { !it.rawValue.isNullOrBlank() }?.rawValue?.let(onBarcode) }
                                .addOnFailureListener { onError(it.localizedMessage ?: "Barcode scan failed.") }
                                .addOnCompleteListener { proxy.close() }
                        }
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                } catch (error: Exception) { onError(error.localizedMessage ?: "Unable to open the camera.") }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }, modifier = Modifier.fillMaxSize())
        TextButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart).padding(20.dp)) { Text("Cancel", color = Color.White) }
        Text("Point the camera at a barcode", color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp))
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
