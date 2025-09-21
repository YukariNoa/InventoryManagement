package com.example.inventory_management.ui.inventory

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.inventory_management.databinding.FragmentInventoryBinding
import android.widget.TableRow
import android.graphics.Typeface
import android.widget.Button
import android.widget.LinearLayout
import android.view.Gravity
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels


import com.example.inventory_management.R


import com.google.firebase.firestore.FirebaseFirestore
data class Item(val itemId: Int, val itemName: String, val itemQty: Int)
class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val inventoryViewModel =
        //   ViewModelProvider(this).get(InventoryViewModel::class.java)

        val viewModel: InventoryViewModel by viewModels()

        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        val root: View = binding.root



        val textView: TextView = binding.textInventory
        viewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }



        /*_binding = FragmentInventoryBinding.inflate(inflater, container, false)

        // Initialize your adapter with an empty list
        adapter = ItemAdapter(emptyList())

        // Setup RecyclerView: layout manager + adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // Observe the LiveData from ViewModel that holds your items
        inventoryViewModel.items.observe(viewLifecycleOwner) { itemList ->
            // Update adapter's data when items change
            adapter.updateData(itemList)
        }*/

        // Observe LiveData and update TableLayout
        viewModel.itemList.observe(viewLifecycleOwner) { items ->
            populateTableLayout(items)
        }

        // Fetch data
        viewModel.fetchInventoryData()

        binding.AddItem.setOnClickListener {
            val tableLayout = binding.inventoryTable
            val db = FirebaseFirestore.getInstance()

            // Skip header row (start from index 1)
            for (i in 1 until tableLayout.childCount) {
                val row = tableLayout.getChildAt(i) as TableRow

                // Get item ID from first column
                val itemIdView = row.getChildAt(0) as TextView
                val itemId = itemIdView.text.toString()

                // Quantity layout is in third column (index 2)
                val quantityLayout = row.getChildAt(2) as LinearLayout
                val qtyTextView = quantityLayout.getChildAt(1) as TextView
                val updatedQty = qtyTextView.text.toString().toInt()

                // 🔥 Update Firestore document
                db.collection("Inventory")
                    .document(itemId)
                    .update("ItemQty", updatedQty)
                    .addOnSuccessListener {
                        Log.d(TAG, "Successfully updated $itemId with quantity $updatedQty")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Failed to update $itemId", e)
                    }
            }
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun populateTableLayout(items: List<Item>) {
        val tableLayout = binding.inventoryTable
        tableLayout.removeAllViews() // Clear old rows

        val borderDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.cell_border)

        // Get screen width in pixels
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        // Set dynamic button size based on screen width
        val buttonWidth = if (screenWidth > 1200) { // Tablet
            100 // Larger button size for tablet
        } else {
            60 // Smaller button size for mobile
        }

        // Set dynamic text size based on screen width
        val textSizeBase = if (screenWidth > 1200) { // Tablet
            18f // Larger text for tablet
        } else {
            14f // Smaller text for phones
        }

        // --- Header Row ---
        val headerRow = TableRow(requireContext())
        val headers = listOf("Item ID", "Item Name", "Quantity")
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

            // LayoutParams for the three main columns (weights for equal width)
            val colParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

            // Item ID
            val idView = TextView(requireContext()).apply {
                text = item.itemId.toString()
                setPadding(16, 16, 16, 16)
                textSize = textSizeBase // Use dynamic text size
                gravity = Gravity.START
                layoutParams = colParams
                background = borderDrawable
            }

            // Item Name
            val nameView = TextView(requireContext()).apply {
                text = item.itemName
                setPadding(16, 16, 16, 16)
                textSize = textSizeBase // Use dynamic text size
                gravity = Gravity.START
                layoutParams = colParams
                background = borderDrawable
            }

            // Quantity LayoutParams for LinearLayout in the Quantity column
            val quantityLayoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)

            // Quantity TextView LayoutParams with weight
            val qtyParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                gravity = Gravity.CENTER
                marginStart = 4
                marginEnd = 4
            }

            // Quantity TextView
            val qtyView = TextView(requireContext()).apply {
                text = item.itemQty.toString()
                textSize = textSizeBase // Use dynamic text size
                gravity = Gravity.CENTER
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                layoutParams = qtyParams
                setPadding(8, 8, 8, 8)
            }

            // LayoutParams for buttons with dynamic width based on screen size
            val btnParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = 4
                marginStart = 4
                width = buttonWidth // Set dynamic width based on screen size
            }

            // Minus Button
            val minusButton = Button(requireContext()).apply {
                text = "−"
                textSize = textSizeBase // Use dynamic text size
                layoutParams = btnParams
                setOnClickListener {
                    val currentQty = qtyView.text.toString().toInt()
                    if (currentQty > 0) {
                        qtyView.text = (currentQty - 1).toString()
                    }
                }
            }

            // Plus Button
            val plusButton = Button(requireContext()).apply {
                text = "+"
                textSize = textSizeBase // Use dynamic text size
                layoutParams = btnParams
                setOnClickListener {
                    val currentQty = qtyView.text.toString().toInt()
                    val newQty = currentQty + 1
                    qtyView.text = getString(R.string.item_quantity, newQty)
                }
            }

            // Quantity Controls Layout (wraps buttons and quantity view)
            val quantityLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = quantityLayoutParams
                setPadding(8, 4, 8, 4)
                addView(minusButton)
                addView(qtyView)
                addView(plusButton)
                background = borderDrawable  // Ensure this has the border too
            }

            // Add columns to row
            row.addView(idView)
            row.addView(nameView)
            row.addView(quantityLayout)
            tableLayout.addView(row)
        }
    }




}