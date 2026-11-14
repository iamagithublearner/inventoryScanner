package com.example.inventoryscanner.ui.screens

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.pm.PackageManager
import android.content.Context
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.inventoryscanner.data.ZebraReaderController
import com.example.inventoryscanner.data.ZebraReaderOption
import kotlinx.coroutines.launch
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
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.provider.Settings
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.inventoryscanner.model.InventoryItem

@Composable
fun InventoryScreen(items: Flow<List<InventoryItem>>, onSave: (InventoryItem) -> Unit) {
    val inventoryItems by items.collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }
    var adding by remember { mutableStateOf(false) }
    var readerSetup by remember { mutableStateOf(false) }
    val ink = Color(0xFF17211D)
    val muted = Color(0xFF75817B)
    val green = Color(0xFF286B50)
    val bg = Color(0xFFF6F7F3)

    if (readerSetup) {
        ReaderSetupScreen(onBack = { readerSetup = false })
        return
    }

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
            Spacer(Modifier.height(18.dp))
            ReaderConnectionCard(onClick = { readerSetup = true }, green = green)
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
private fun ReaderConnectionCard(onClick: () -> Unit, green: Color) {
    Card(onClick = onClick, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0E9))) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("BT", color = green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("Connect RFID reader", color = Color(0xFF17211D), fontWeight = FontWeight.SemiBold)
                Text("Zebra RFD8500 · Bluetooth", color = Color(0xFF75817B), fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
            }
            Text("›", color = green, fontSize = 24.sp)
        }
    }
}

@Composable
private fun ReaderSetupScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val readerController = remember(context) { ZebraReaderController(context) }
    var readerOptions by remember { mutableStateOf(emptyList<ZebraReaderOption>()) }
    var selectedReaderId by remember { mutableStateOf<String?>(null) }
    var isConnected by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var usbStatus by remember { mutableStateOf<String?>(null) }
    var usbReady by remember { mutableStateOf(false) }
    val usbManager = remember(context) { context.getSystemService(Context.USB_SERVICE) as UsbManager }
    val usbPermissionAction = remember(context) { "${context.packageName}.USB_PERMISSION" }

    DisposableEffect(readerController) {
        onDispose { readerController.close() }
    }

    DisposableEffect(usbManager, usbPermissionAction) {
        val usbPermissionReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != usbPermissionAction) return
                val device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                usbReady = granted
                usbStatus = if (granted) {
                    "USB access granted${device?.productName?.let { " for $it" }.orEmpty()}. Configure the RFD8500 for USB HID to send tag IDs."
                } else "USB permission was denied."
            }
        }
        ContextCompat.registerReceiver(
            context,
            usbPermissionReceiver,
            IntentFilter(usbPermissionAction),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose { context.unregisterReceiver(usbPermissionReceiver) }
    }

    fun discoverReaders() {
        scope.launch {
            isLoading = true
            status = "Searching for paired RFD8500 readers…"
            try {
                readerOptions = readerController.discover()
                selectedReaderId = readerOptions.firstOrNull()?.id
                status = if (readerOptions.isEmpty()) {
                    "No paired RFD8500 found. Pair it in Bluetooth settings, then scan again."
                } else "Found ${readerOptions.size} paired RFD8500 reader${if (readerOptions.size == 1) "" else "s"}."
            } catch (error: Exception) {
                status = error.message ?: "Reader discovery failed. Check Bluetooth permission and try again."
            } finally {
                isLoading = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        if (results.values.all { it }) discoverReaders()
        else status = "Bluetooth permissions are required to discover the RFD8500."
    }

    fun scanForReaders() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else emptyArray()
        val missing = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) discoverReaders() else permissionLauncher.launch(missing.toTypedArray())
    }

    fun connectUsbReader() {
        val device = usbManager.deviceList.values.firstOrNull { it.vendorId == ZEBRA_USB_VENDOR_ID }
        if (device == null) {
            usbReady = false
            usbStatus = "No Zebra USB device found. Connect the RFD8500 to the TC21 with a USB host/OTG adapter."
        } else if (usbManager.hasPermission(device)) {
            usbReady = true
            usbStatus = "USB access is ready${device.productName?.let { " for $it" }.orEmpty()}. Configure the RFD8500 for USB HID to send tag IDs."
        } else {
            val permissionIntent = PendingIntent.getBroadcast(
                context,
                USB_PERMISSION_REQUEST_CODE,
                Intent(usbPermissionAction).setPackage(context.packageName),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            usbManager.requestPermission(device, permissionIntent)
            usbStatus = "Waiting for USB permission…"
        }
    }

    val ink = Color(0xFF17211D)
    val muted = Color(0xFF75817B)
    val green = Color(0xFF286B50)
    val bg = Color(0xFFF6F7F3)
    Scaffold(containerColor = bg) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)
        ) {
            Spacer(Modifier.height(18.dp))
            TextButton(onClick = onBack) { Text("‹  Inventory", color = green) }
            Spacer(Modifier.height(14.dp))
            Text("RFID READER", color = green, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Text("Connect your RFD8500", color = ink, fontSize = 27.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 7.dp))
            Text("Pair the reader with this TC21 over Bluetooth.", color = muted, fontSize = 14.sp, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(22.dp))
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("Bluetooth", color = ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text("Supported for the RFD8500. Pair it in Android settings, then return here to connect through Zebra's RFID SDK.", color = muted, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                    Button(
                        onClick = { context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = green)
                    ) { Text("Open Bluetooth settings") }
                    Button(
                        onClick = { scanForReaders() },
                        enabled = !isLoading && !isConnected,
                        modifier = Modifier.fillMaxWidth().padding(top = 9.dp).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = green)
                    ) { Text(if (isLoading) "Searching…" else "Find paired RFD8500") }
                }
            }
            if (readerOptions.isNotEmpty() && !isConnected) {
                Spacer(Modifier.height(12.dp))
                readerOptions.forEach { option ->
                    Card(
                        onClick = { selectedReaderId = option.id },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (selectedReaderId == option.id) Color(0xFFE8F0E9) else Color.White)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(option.name, color = ink, fontWeight = FontWeight.SemiBold)
                            if (option.serialNumber.isNotBlank()) Text("Serial ${option.serialNumber}", color = muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Button(
                    onClick = {
                        val id = selectedReaderId ?: return@Button
                        scope.launch {
                            isLoading = true
                            status = "Connecting to reader…"
                            status = readerController.connect(id)
                            isConnected = status?.startsWith("Connected to") == true
                            isLoading = false
                        }
                    },
                    enabled = selectedReaderId != null && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = green)
                ) { Text("Connect") }
            }
            if (isConnected) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            status = readerController.disconnect()
                            isConnected = false
                            isLoading = false
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A3932))
                ) { Text("Disconnect reader") }
            }
            status?.let { Text(it, color = if (it.startsWith("Connected to")) green else muted, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp)) }
            Spacer(Modifier.height(12.dp))
            Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFEDEFEA))) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("USB HID", color = ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text("The RFD8500 can send RFID reads over USB HID. In this mode, tag IDs arrive as keyboard input; focus the RFID ID field before pressing the reader trigger. Configure USB HID on the reader first.", color = muted, fontSize = 14.sp, modifier = Modifier.padding(top = 8.dp))
                    Button(
                        onClick = { connectUsbReader() },
                        modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = green)
                    ) { Text(if (usbReady) "Check USB reader" else "Connect USB reader") }
                    usbStatus?.let { Text(it, color = if (usbReady) green else muted, fontSize = 12.sp, modifier = Modifier.padding(top = 9.dp)) }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Discovery and Bluetooth connection use Zebra's RFID SDK. RFID tag capture and Find mode are the next integration steps.", color = muted, fontSize = 12.sp)
            Spacer(Modifier.height(28.dp))
        }
    }
}

private const val ZEBRA_USB_VENDOR_ID = 0x05E0
private const val USB_PERMISSION_REQUEST_CODE = 8500

@Composable
private fun SmallPill(label: String, green: Color) {
    Text(label, color = green, fontSize = 11.sp, modifier = Modifier.background(Color(0xFFEAF1EB), RoundedCornerShape(20.dp)).padding(horizontal = 10.dp, vertical = 6.dp))
}
