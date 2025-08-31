package com.example.inventory_management.ui.inventory

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.inventory_management.databinding.FragmentInventoryBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

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
        val inventoryViewModel =
            ViewModelProvider(this).get(InventoryViewModel::class.java)
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textInventory
        inventoryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(requireContext())
        val db = FirebaseFirestore.getInstance()
        db.collection("InventoryManagement")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val itemName = document.data["itemName"]
                    val itemQuantity = document.data["itemQuantity"]
                    val itemLocation = document.data["itemLocation"]
                    // You can now use itemName, itemQuantity, and itemLocation as needed
                    Log.d(TAG, "onCreate called in InventoryFragment ${itemName}")

                }
             }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}