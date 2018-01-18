package com.aidandavisdev.aidandavis.prioritylist

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.aidandavisdev.aidandavis.prioritylist.MainActivity.Companion.getItemsCollection
import com.google.firebase.auth.FirebaseAuth

/**
 * Created by Aidan Davis on 10/12/2017.
 */

class PriorityListItemAdapter : RecyclerView.Adapter<PriorityListItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var itemName: TextView = view.findViewById(R.id.item_name)
        internal var itemDescription: TextView = view.findViewById(R.id.item_description)
        internal var itemList: TextView = view.findViewById(R.id.item_list_name)
        internal var itemDueDate: TextView = view.findViewById(R.id.item_due_date)
        internal var tickedCheckbox: CheckBox = view.findViewById(R.id.ticked_checkbox)
    }

    private var itemList = ArrayList<PrioritisedItem>()
    private lateinit var context: Context
    private lateinit var list: String

    fun updateList(newList: List<PrioritisedItem>, list: String) {
        this.list = list
        itemList.clear()
        itemList.addAll(newList.sortedWith(compareBy(PrioritisedItem::getScore)).reversed()) // sort according to priority score
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder? {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.priority_item_card, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.itemName.text = item.name
        holder.itemDescription.text = item.description
        holder.itemList.text = item.list
        holder.itemDueDate.text = if (item.endDate != null) Constants.DateFormats.fullDate.format(item.endDate) else ""
        holder.tickedCheckbox.isChecked = item.ticked

        holder.itemView.setOnLongClickListener {
            if (!item.ticked) {
                CreateEditItemActivity.open(context, itemList[position], list)
            } else {
                Toast.makeText(context, "Untick the item to edit or delete", Toast.LENGTH_SHORT).show()
            }
            true
        }

        holder.tickedCheckbox.setOnClickListener {
            getItemsCollection(FirebaseAuth.getInstance().currentUser!!.uid)
                    .document(item.id)
                    .update("ticked", holder.tickedCheckbox.isChecked)
            itemList.remove(item)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = itemList.size
}
