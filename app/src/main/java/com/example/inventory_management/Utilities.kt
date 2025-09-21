package com.example.inventory_management

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.inventory_management.ui.inventory.Item

fun greetUser(name: String): String {
    return "Hello, $name!"
}




class ItemAdapter(private var itemList: List<Item>) :
    RecyclerView.Adapter<ItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvItemId)
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvQty: TextView = view.findViewById(R.id.tvItemQty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = itemList[position]
        holder.tvId.text = item.itemId.toString()
        holder.tvName.text = item.itemName
        holder.tvQty.text = item.itemQty.toString()
    }

    override fun getItemCount(): Int = itemList.size
}