package com.aidandavisdev.aidandavis.prioritylist

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.aidandavisdev.aidandavis.prioritylist.Constants.Intents.PARAM_ITEM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_create_edit_item.*
import java.util.*

/**
 * Created by Aidan Davis on 16/12/2017.
 */

class CreateEditItemActivity : AppCompatActivity() {

    private val TAG = "CreateEditItemActivity"

    companion object {
        fun open(context: Context, item: PrioritisedItem?) {
            val openIntent = Intent(context, CreateEditItemActivity::class.java)
            openIntent.putExtra(PARAM_ITEM, item)
            context.startActivity(openIntent)
        }
    }

    private var item: PrioritisedItem? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var uId: String

    private var startDate: Date? = null
    private var endDate: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_edit_item)
        db = FirebaseFirestore.getInstance()
        uId = FirebaseAuth.getInstance().currentUser?.uid!!
        item = intent.getSerializableExtra(PARAM_ITEM) as PrioritisedIlllllllllllltem

        item_delete_button.visibility = View.GONE
        edit_create_progress_bar.visibility = View.GONE
        if (item != null) changeToEdit()
        item_create_button.setOnClickListener({ createOrEditItem() })

        // start and end date pickers
        start_date_button.setOnClickListener { setDate(startDate, start_date_date, start_date_time) }
        end_date_button.setOnClickListener { setDate(endDate, end_date_date, end_date_time) }
    }

    private fun setDate(date: Date?, dateText: TextView, timeText: TextView) {

        var calendar = GregorianCalendar()

        val datePicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

        },
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH))
                .show()
    }

    // populate fields with pre-existing data
    private fun changeToEdit() {
        item_name.setText(item!!.name)
        item_description.setText(item!!.description)
        importance_seekbar.progress = item!!.importance
        effort_seekbar.progress = item!!.effort

        item_create_button.text = getString(R.string.create_save_button_save)
        item_delete_button.visibility = View.VISIBLE
        item_delete_button.setOnClickListener {
            edit_create_progress_bar.visibility = View.VISIBLE
            create_edit_item_button_bar.visibility = View.GONE
            db.collection("users")
                    .document(uId)
                    .collection("list1")
                    .document(item!!.id)
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
        newDetails.put("name", item_name.text.toString())
        newDetails.put("description", item_description.text.toString())
        newDetails.put("startDate", startDate)
        newDetails.put("endDate", endDate)
        newDetails.put("importance", importance_seekbar.progress + 1)
        newDetails.put("effort", effort_seekbar.progress + 1)

        if (item == null) {
            db.collection("users")
                    .document(uId)
                    .collection("list1")
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
            db.collection("users")
                    .document(uId)
                    .collection("list1")
                    .document(item!!.id)
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

        Log.i(TAG, "Name ${item_name.text}")
        Log.i(TAG, "Description ${item_description.text}")
        Log.i(TAG, "Importance ${importance_seekbar.progress}")
        Log.i(TAG, "Effort ${effort_seekbar.progress}")
    }
}