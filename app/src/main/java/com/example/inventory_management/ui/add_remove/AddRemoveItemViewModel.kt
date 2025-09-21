package com.example.inventory_management.ui.add_remove

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.inventory_management.ui.inventory.Item
import com.google.firebase.firestore.FirebaseFirestore

class AddRemoveItemViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is slideshow Fragment really?"
    }

    private val _items = MutableLiveData<List<Item>>()

    init {
        fetchInventoryData()
    }

    private val _itemList = MutableLiveData<List<Item>>()
    val itemList: LiveData<List<Item>> = _itemList
    fun fetchInventoryData() {
        // Placeholder for future implementation to fetch inventory data
        val db = FirebaseFirestore.getInstance()
        db.collection("Inventory")
            .get()
            .addOnSuccessListener { result ->
                val items = mutableListOf<Item>()
                for (document in result) {
                    val ItemID = document.id.toIntOrNull()
                    val ItemName = document.getString("ItemName")
                    val ItemQuantity = document.getLong("ItemQty")?.toInt()
                    // You can now use itemName, itemQuantity, and itemLocation as needed
                    if (ItemID != null && ItemName != null && ItemQuantity != null) {
                        items.add(Item(ItemID, ItemName, ItemQuantity))
                        Log.w(TAG, "ITEM ADDED: $ItemID, $ItemName, $ItemQuantity")
                    } else {
                        Log.w("Firestore", "Skipping document ${document.id} due to missing or invalid data")
                    }
                }
                val sortedList = items.sortedBy { it.itemId }
                _itemList.value = sortedList
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    val text: LiveData<String> = _text
}