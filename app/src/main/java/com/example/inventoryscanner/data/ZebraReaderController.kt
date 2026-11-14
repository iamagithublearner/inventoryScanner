package com.example.inventoryscanner.data

import android.content.Context
import com.zebra.rfid.api3.ENUM_TRANSPORT
import com.zebra.rfid.api3.OperationFailureException
import com.zebra.rfid.api3.ReaderDevice
import com.zebra.rfid.api3.Readers
import com.zebra.rfid.api3.RFIDReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ZebraReaderOption(val id: String, val name: String, val serialNumber: String)

/** Owns the Zebra SDK reader session; all SDK calls run off the main thread. */
class ZebraReaderController(context: Context) {
    private val appContext = context.applicationContext
    private val lock = Any()
    private val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var readers: Readers? = null
    private var readerDevices = emptyMap<String, ReaderDevice>()
    private var connectedReader: RFIDReader? = null

    suspend fun discover(): List<ZebraReaderOption> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val sdkReaders = readers ?: Readers(appContext, ENUM_TRANSPORT.BLUETOOTH).also { readers = it }
            val devices = sdkReaders.GetAvailableRFIDReaderList().orEmpty()
                .filter { it.name.contains("RFD8500", ignoreCase = true) || it.modelNumber.contains("RFD8500", ignoreCase = true) }
            readerDevices = devices.mapIndexed { index, device ->
                val id = device.address.ifBlank { device.serialNumber.ifBlank { "reader-$index" } }
                id to device
            }.toMap()
            readerDevices.map { (id, device) ->
                ZebraReaderOption(id, device.name.ifBlank { "Zebra RFD8500" }, device.serialNumber)
            }
        }
    }

    suspend fun connect(id: String): String = withContext(Dispatchers.IO) {
        synchronized(lock) {
            try {
                val device = readerDevices[id] ?: return@synchronized "Reader is no longer available. Scan for paired readers again."
                val reader = device.rfidReader
                if (!reader.isConnected) reader.connect()
                connectedReader = reader
                "Connected to ${device.name.ifBlank { "Zebra RFD8500" }}"
            } catch (error: OperationFailureException) {
                error.vendorMessage?.takeIf(String::isNotBlank)
                    ?: error.statusDescription?.takeIf(String::isNotBlank)
                    ?: "Could not connect to the reader. Check Bluetooth pairing and reader power."
            } catch (error: Exception) {
                error.message?.takeIf(String::isNotBlank)
                    ?: "Could not connect to the reader. Check Bluetooth pairing and reader power."
            }
        }
    }

    suspend fun disconnect(): String = withContext(Dispatchers.IO) {
        synchronized(lock) {
            try {
                connectedReader?.disconnect()
                connectedReader = null
                "Reader disconnected"
            } catch (error: Exception) {
                error.message?.takeIf(String::isNotBlank) ?: "Could not disconnect the reader."
            }
        }
    }

    fun close() {
        cleanupScope.launch {
            synchronized(lock) {
                runCatching { connectedReader?.disconnect() }
                connectedReader = null
                runCatching { readers?.Dispose() }
                readers = null
                readerDevices = emptyMap()
            }
            cleanupScope.cancel()
        }
    }
}
