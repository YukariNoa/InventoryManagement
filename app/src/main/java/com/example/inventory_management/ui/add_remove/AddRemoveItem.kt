package com.example.inventory_management.ui.add_remove

import android.content.ContentValues.TAG
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableRow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.inventory_management.R
import com.example.inventory_management.databinding.FragmentAddRemoveItemBinding
import com.example.inventory_management.ui.inventory.Item
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast

class AddRemoveItem : Fragment() {

    private var _binding: FragmentAddRemoveItemBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var viewModel: AddRemoveItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[AddRemoveItemViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val AddRemoveItemViewModel =
        //    ViewModelProvider(this).get(AddRemoveItemViewModel::class.java)

        _binding = FragmentAddRemoveItemBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val textView: TextView = binding.textAddRemoveItem
        viewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        viewModel.itemList.observe(viewLifecycleOwner) { items ->
            populateTableLayout(items)
        }

        // Fetch data
        viewModel.fetchInventoryData()

        binding.AddItem.setOnClickListener {
            val itemName = binding.NewItemName.text.toString().trim()
            val itemQty = binding.NewItemQuantity.text.toString().toIntOrNull() ?: 0
            val db = FirebaseFirestore.getInstance()

            if (itemName.isEmpty()) {
                return@setOnClickListener
            }

            // Step 1: Check if the item name already exists
            db.collection("Inventory")
                .whereEqualTo("ItemName", itemName)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Item with the same name already exists
                        Log.w("Firestore", "Item with name $itemName already exists.")
                        Toast.makeText(requireContext(), "Item already exists!", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    } else {
                        // Step 2: Proceed to add new item with new ID
                        db.collection("Inventory")
                            .get()
                            .addOnSuccessListener { result ->
                                val newId = (result.size() + 1).toString()
                                val newItem = hashMapOf(
                                    "ItemName" to itemName,
                                    "ItemQty" to itemQty
                                )
                                db.collection("Inventory").document(newId)
                                    .set(newItem)
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "Item added successfully")
                                        viewModel.fetchInventoryData()
                                        binding.NewItemName.text.clear()
                                        binding.NewItemQuantity.text.clear()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("Firestore", "Error writing document", e)
                                    }
                            }
                            .addOnFailureListener { e ->
                                Log.w("Firestore", "Error fetching inventory size", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Error checking for duplicates", e)
                }
        }


        return root
    }

    private fun removeItem(item: Item) {
        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("Inventory")

        // Step 1: Delete the selected item
        collectionRef.document(item.itemId.toString())
            .delete()
            .addOnSuccessListener {
                // Step 2: Fetch all remaining items
                collectionRef.get()
                    .addOnSuccessListener { result ->
                        val remainingItems = mutableListOf<Item>()

                        for (document in result) {
                            val itemID = document.id.toIntOrNull() ?: -1
                            val itemName = document.getString("ItemName")
                            val itemQty = document.getLong("ItemQty")?.toInt()
                            if (itemName != null && itemQty != null) {
                                remainingItems.add(Item(itemID, itemName, itemQty)) // Temp ID
                                remainingItems.sortedBy { it.itemId }
                                Log.w(TAG, "Remaining Item: $itemID, $itemName, $itemQty")
                            }
                        }

                        // Step 3: Delete all remaining items
                        val deleteBatch = db.batch()
                        for (doc in result.documents) {
                            deleteBatch.delete(doc.reference)
                        }

                        deleteBatch.commit().addOnSuccessListener {
                            // Step 4: Re-add items with new sequential IDs
                            val reAddBatch = db.batch()
                            remainingItems.forEachIndexed { index, remainingItem ->
                                var ID: Int
                                if (remainingItem.itemId == index + 1)
                                {
                                    ID = remainingItem.itemId
                                    Log.w(TAG, "Changed ID from ${remainingItem.itemId} to $ID")
                                }
                                else
                                {
                                    ID = index + 1
                                    Log.w(TAG, "Changed ID from ${remainingItem.itemId} to $ID")
                                }
                                //val newId = (index + 1).toString()
                                val docRef = collectionRef.document(ID.toString())
                                val itemData = mapOf(
                                    "ItemName" to remainingItem.itemName,
                                    "ItemQty" to remainingItem.itemQty
                                )
                                reAddBatch.set(docRef, itemData)
                            }

                            reAddBatch.commit().addOnSuccessListener {
                                viewModel.fetchInventoryData()
                            }.addOnFailureListener { e ->
                                Log.w("Firestore", "Error re-adding items", e)
                            }

                        }.addOnFailureListener { e ->
                            Log.w("Firestore", "Error deleting old items", e)
                        }

                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error fetching items", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error deleting item", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun populateTableLayout(items: List<Item>) {
        val tableLayout = binding.itemTable
        tableLayout.removeAllViews() // Clear old rows

        val borderDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.cell_border)

        // --- Header Row ---
        val headerRow = TableRow(requireContext())
        val headers = listOf("Item ID", "Item Name", "Action")
        headers.forEachIndexed { index, text ->
            val tv = TextView(requireContext()).apply {
                this.text = text
                setTypeface(null, Typeface.BOLD)
                setPadding(16, 16, 16, 16)
                textSize = 20f
                gravity = if (index == 2) Gravity.CENTER else Gravity.START
                layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                background = borderDrawable
            }
            headerRow.addView(tv)
        }
        tableLayout.addView(headerRow)

        // --- Data Rows ---
        for (item in items) {
            val row = TableRow(requireContext())

            val colParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

            // Column 1: Item ID
            val idView = TextView(requireContext()).apply {
                text = item.itemId.toString()
                setPadding(16, 16, 16, 16)
                textSize = 18f
                gravity = Gravity.START
                layoutParams = colParams
                background = borderDrawable
            }

            // Column 2: Item Name
            val nameView = TextView(requireContext()).apply {
                text = item.itemName
                setPadding(16, 16, 16, 16)
                textSize = 18f
                gravity = Gravity.START
                layoutParams = colParams
                background = borderDrawable
            }

            // Column 3: Remove Button
            val removeButton = Button(requireContext()).apply {
                textSize = 14f
                text = getString(R.string.remove_button)
                layoutParams = colParams
                background = borderDrawable
                setOnClickListener {
                    // Call a function to remove the item
                    removeItem(item)
                }
            }

            // Add columns to row
            row.addView(idView)
            row.addView(nameView)
            row.addView(removeButton)

            tableLayout.addView(row)
        }
    }


}