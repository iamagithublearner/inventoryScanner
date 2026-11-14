package com.example.inventoryscanner.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.inventoryscanner.model.InventoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Stores each inventory field in its own column for straightforward CSV/JSON export later. */
class InventoryRepository(context: Context) {
    private val database = InventoryDb(context.applicationContext)

    suspend fun load(): List<InventoryItem> = withContext(Dispatchers.IO) {
        val rows = mutableListOf<InventoryItem>()
        database.readableDatabase.query(
            "inventory_items",
            arrayOf("name", "tags", "barcode", "rfid_id", "description"),
            null, null, null, null, "id DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                rows += InventoryItem(
                    name = cursor.getString(0),
                    tags = cursor.getString(1),
                    barcode = cursor.getString(2),
                    rfidId = cursor.getString(3),
                    description = cursor.getString(4)
                )
            }
        }
        rows
    }

    suspend fun add(item: InventoryItem) = withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
            put("name", item.name)
            put("tags", item.tags)
            put("barcode", item.barcode)
            put("rfid_id", item.rfidId)
            put("description", item.description)
        }
        database.writableDatabase.insertOrThrow("inventory_items", null, values)
        Unit
    }

    private class InventoryDb(context: Context) : SQLiteOpenHelper(context, "inventory.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE inventory_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    tags TEXT NOT NULL,
                    barcode TEXT NOT NULL,
                    rfid_id TEXT NOT NULL,
                    description TEXT NOT NULL
                )""".trimIndent()
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    }
}
