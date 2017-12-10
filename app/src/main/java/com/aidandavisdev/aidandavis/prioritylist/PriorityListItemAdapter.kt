package com.aidandavisdev.aidandavis.prioritylist

import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

/**
 * Created by Aidan Davis on 10/12/2017.
 */

class PriorityListItemAdapter : RecyclerView.Adapter<PriorityListItemAdapter.ViewHolder>() {

    class ViewHolder(view: CardView) : RecyclerView.ViewHolder(view) {
        internal var itemName: TextView = view.findViewById(R.id.item_name)
        internal var itemDescription: TextView = view.findViewById(R.id.item_description)
    }

    private var itemList: ArrayList<PrioritisedItem> = ArrayList()

    fun updateList(newList: List<PrioritisedItem>) {
        itemList.clear()
        itemList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.priority_item_card, parent, false)
        return ViewHolder(view as CardView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemName.text = itemList[position].name
        holder.itemDescription.text = itemList[position].description
    }

    override fun getItemCount(): Int = itemList.size
}
