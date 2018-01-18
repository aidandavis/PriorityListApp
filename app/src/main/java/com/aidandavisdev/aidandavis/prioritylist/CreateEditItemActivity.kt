package com.aidandavisdev.aidandavis.prioritylist

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.aidandavisdev.aidandavis.prioritylist.Constants.DateFormats.fullDate
import com.aidandavisdev.aidandavis.prioritylist.Constants.Intents.ITEM
import com.aidandavisdev.aidandavis.prioritylist.Constants.Intents.LIST
import com.aidandavisdev.aidandavis.prioritylist.MainActivity.Companion.getItem
import com.aidandavisdev.aidandavis.prioritylist.MainActivity.Companion.getItemsCollection
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_create_edit_item.*
import java.util.*

/**
 * Created by Aidan Davis on 16/12/2017.
 */

class CreateEditItemActivity : AppCompatActivity() {

    private val TAG = "CreateEditItemActivity"

    companion object {
        fun open(context: Context, item: PrioritisedItem?, list: String) {
            val openIntent = Intent(context, CreateEditItemActivity::class.java)
            openIntent.putExtra(ITEM, item)
            openIntent.putExtra(LIST, list)
            context.startActivity(openIntent)
        }
    }

    private var item: PrioritisedItem? = null
    private lateinit var uId: String
    private lateinit var list: String

    private var startDate: Date? = null
    private var endDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_edit_item)
        uId = FirebaseAuth.getInstance().currentUser?.uid!!
        item = intent.getSerializableExtra(ITEM) as PrioritisedItem?
        list = intent.getStringExtra(LIST)

        start_date_clear_button.visibility = View.GONE
        start_date_button.setOnClickListener {
            if (startDate == null) startDate = Calendar.getInstance().time
            setDate(startDate!!, start_date_date)
            start_date_clear_button.visibility = View.VISIBLE
            start_date_button.visibility = View.GONE
        }
        start_date_clear_button.setOnClickListener {
            startDate = null
            start_date_date.text = ""
            start_date_clear_button.visibility = View.GONE
            start_date_button.visibility = View.VISIBLE
        }
        end_date_clear_button.visibility = View.GONE
        end_date_button.setOnClickListener {
            if (endDate == null) endDate = Calendar.getInstance().time
            setDate(endDate!!, end_date_date)
            end_date_clear_button.visibility = View.VISIBLE
            end_date_button.visibility = View.GONE
        }
        end_date_clear_button.setOnClickListener {
            endDate = null
            end_date_date.text = ""
            end_date_clear_button.visibility = View.GONE
            end_date_button.visibility = View.VISIBLE
        }

        item_delete_button.visibility = View.GONE
        edit_create_progress_bar.visibility = View.GONE
        if (item != null) changeToEdit()
        item_create_button.setOnClickListener({ createOrEditItem() })
    }

    private fun setDate(date: Date, dateText: TextView) {
        val newDate = GregorianCalendar()
        val currentDate = GregorianCalendar()
        currentDate.time = date

        DatePickerDialog(this,
                DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                    newDate.set(Calendar.YEAR, year)
                    newDate.set(Calendar.MONTH, month)
                    newDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    TimePickerDialog(this,
                            TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
                                newDate.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                newDate.set(Calendar.MINUTE, minute)
                                date.time = newDate.timeInMillis
                                dateText.text = fullDate.format(newDate.time)
                            },
                            currentDate.get(Calendar.HOUR_OF_DAY),
                            currentDate.get(Calendar.MINUTE),
                            false)
                            .show()
                },
                currentDate.get(Calendar.YEAR),
                currentDate.get(Calendar.MONTH),
                currentDate.get(Calendar.DAY_OF_MONTH))
                .show()
    }

    // populate fields with pre-existing data
    private fun changeToEdit() {
        item_name.setText(item!!.name)
        item_description.setText(item!!.description)
        startDate = item!!.startDate
        if (startDate != null) {
            start_date_date.text = fullDate.format(startDate)
            start_date_clear_button.visibility = View.VISIBLE
            start_date_button.visibility = View.GONE
        }
        endDate = item!!.endDate
        if (endDate != null) {
            end_date_date.text = fullDate.format(endDate)
            end_date_clear_button.visibility = View.VISIBLE
            end_date_button.visibility = View.GONE
        }
        importance_seekbar.progress = item!!.importance - 1
        effort_seekbar.progress = item!!.effort - 1

        item_create_button.text = getString(R.string.create_save_button_save)
        item_delete_button.visibility = View.VISIBLE
        item_delete_button.setOnClickListener {
            edit_create_progress_bar.visibility = View.VISIBLE
            create_edit_item_button_bar.visibility = View.GONE
            getItem(uId, item!!.id)
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "Deleted item $item")
                        finish()
                    }
                    .addOnFailureListener {
                        Log.w(TAG, "Error deleting item", it)
                        Toast.makeText(this, "Failure deleting item", Toast.LENGTH_SHORT).show()
                        edit_create_progress_bar.visibility = View.GONE
                        create_edit_item_button_bar.visibility = View.VISIBLE
                    }
        }
    }

    private fun createOrEditItem() {
        if (item_name.text.isEmpty()) {
            Toast.makeText(this, "Enter an item name", Toast.LENGTH_SHORT).show()
            return
        }
        // if end date and no start date, then start date is now
        if (endDate != null && startDate == null) startDate = Calendar.getInstance().time
        // if start date and no end date, throw exception
        if (startDate != null && endDate == null) {
            Toast.makeText(this, "There needs to be a deadline", Toast.LENGTH_SHORT).show()
            return
        }
        // if start date equal to or after end date, throw exception
        if (startDate != null && endDate != null) {
            if (startDate!!.time >= endDate!!.time) {
                Toast.makeText(this, "Start date can't be after deadline", Toast.LENGTH_SHORT).show()
                return
            }
        }

        edit_create_progress_bar.visibility = View.VISIBLE
        create_edit_item_button_bar.visibility = View.GONE
        val newDetails = HashMap<String, Any?>()
        newDetails["list"] = list
        newDetails["name"] = item_name.text.toString()
        newDetails["description"] = item_description.text.toString()
        newDetails["startDate"] = startDate
        newDetails["endDate"] = endDate
        newDetails["importance"] = importance_seekbar.progress + 1
        newDetails["effort"] = effort_seekbar.progress + 1
        newDetails["ticked"] = false

        if (item == null) {
            getItemsCollection(uId)
                    .add(newDetails)
                    .addOnSuccessListener {
                        Log.d(TAG, "Item added with ID: ${it.id}")
                        finish()
                    }
                    .addOnFailureListener {
                        Log.w(TAG, "Error adding item", it)
                        Toast.makeText(this, "Failure adding item", Toast.LENGTH_SHORT).show()
                        edit_create_progress_bar.visibility = View.GONE
                        create_edit_item_button_bar.visibility = View.VISIBLE
                    }
        } else {
            getItem(uId, item!!.id)
                    .set(newDetails)
                    .addOnSuccessListener {
                        Log.d(TAG, "Updated item ${this.item}")
                        finish()
                    }
                    .addOnFailureListener {
                        Log.w(TAG, "Error updating item", it)
                        Toast.makeText(this, "Failure updating item", Toast.LENGTH_SHORT).show()
                        edit_create_progress_bar.visibility = View.GONE
                        create_edit_item_button_bar.visibility = View.VISIBLE
                    }
        }
    }
}