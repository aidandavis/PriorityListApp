package com.aidandavisdev.aidandavis.prioritylist

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.aidandavisdev.aidandavis.prioritylist.MainActivity.Companion.getItemsCollection
import com.google.firebase.auth.FirebaseAuth

/**
 * Created by Aidan Davis on 10/12/2017.
 */

class PriorityListItemAdapter : RecyclerView.Adapter<PriorityListItemAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        internal var itemName: TextView = view.findViewById(R.id.item_name)
        internal var itemDescription: TextView = view.findViewById(R.id.item_description)
        internal var itemScore: TextView = view.findViewById(R.id.item_score)
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
        holder.itemScore.text = "%.2f".format(item.getScore())
        holder.tickedCheckbox.isChecked = item.ticked

        holder.itemView.setOnLongClickListener {
            if (!item.ticked) {
                CreateEditItemActivity.open(context, itemList[position], list)
            }
            true
        }

        val uId = FirebaseAuth.getInstance().currentUser?.uid
        if (uId != null) {
            holder.tickedCheckbox.setOnClickListener {
                getItemsCollection(uId)
                        .document(item.id)
                        .update("ticked", holder.tickedCheckbox.isChecked)
            }
        }
    }

    override fun getItemCount(): Int = itemList.size
}
