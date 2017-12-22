package com.aidandavisdev.aidandavis.prioritylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.aidandavisdev.aidandavis.prioritylist.Constants.Intents.ITEM_UID
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
        fun open(context: Context, itemId: String) {
            val openIntent = Intent(context, CreateEditItemActivity::class.java)
            openIntent.putExtra(ITEM_UID, itemId)
            context.startActivity(openIntent)
        }
    }

    private lateinit var itemId: String
    private lateinit var db: FirebaseFirestore
    private var uId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_edit_item)
        db = FirebaseFirestore.getInstance()
        uId = FirebaseAuth.getInstance().currentUser?.uid
        itemId = intent.getStringExtra(ITEM_UID)

        item_delete_button.visibility = View.GONE

        if (itemId != "") changeToEdit()

        item_create_button.setOnClickListener({ createOrEditItem() })
    }

    // populate fields with pre-existing data
    private fun changeToEdit() {
        item_create_button.text = "Save"
        item_delete_button.visibility = View.VISIBLE
        item_delete_button.setOnClickListener {
            db.collection("users")
                    .document(uId!!)
                    .collection("list1")
                    .document(itemId)
                    .delete()
                    .addOnSuccessListener {
                        Log.d(TAG, "Deleted item $itemId")
                        finish()
                    }
                    .addOnFailureListener {
                        Log.w(TAG, "Error deleting item", it)
                        Toast.makeText(this, "Failure deleting item", Toast.LENGTH_SHORT).show()
                    }
        }

        db.collection("users")
                .document(uId!!)
                .collection("list1")
                .document(itemId)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val doc = task.result
                        item_name.setText(doc["name"] as String)
                        item_description.setText(doc["description"] as String)
                        importance_seekbar.progress = (doc["importance"] as Long).toInt() - 1
                        urgency_seekbar.progress = (doc["urgency"] as Long).toInt() - 1
                        effort_seekbar.progress = (doc["effort"] as Long).toInt() - 1
                    } else {
                        Log.w(TAG, "Error getting items", task.exception)
                    }
                }
    }

    private fun createOrEditItem() {
        if (item_name.text.isEmpty()) {
            Toast.makeText(this, "Enter an item name", Toast.LENGTH_SHORT).show()
            return
        }

        if (uId != null) {
            val item = HashMap<String, Any>()
            item.put("name", item_name.text.toString())
            item.put("description", item_description.text.toString())
            item.put("importance", importance_seekbar.progress + 1)
            item.put("urgency", urgency_seekbar.progress + 1)
            item.put("effort", effort_seekbar.progress + 1)

            if (itemId == "") {
                db.collection("users")
                        .document(uId!!)
                        .collection("list1")
                        .add(item)
                        .addOnSuccessListener {
                            Log.d(TAG, "Item added with ID: ${it.id}")
                            finish()
                        }
                        .addOnFailureListener {
                            Log.w(TAG, "Error adding item", it)
                            Toast.makeText(this, "Failure adding item", Toast.LENGTH_SHORT).show()
                        }
            } else {
                db.collection("users")
                        .document(uId!!)
                        .collection("list1")
                        .document(itemId)
                        .set(item)
                        .addOnSuccessListener {
                            Log.d(TAG, "Updated item $itemId")
                            finish()
                        }
                        .addOnFailureListener {
                            Log.w(TAG, "Error updating item", it)
                            Toast.makeText(this, "Failure updating item", Toast.LENGTH_SHORT).show()
                        }
            }
        }

        Log.i(TAG, "Name ${item_name.text}")
        Log.i(TAG, "Description ${item_description.text}")
        Log.i(TAG, "Importance ${importance_seekbar.progress}")
        Log.i(TAG, "Urgency ${urgency_seekbar.progress}")
        Log.i(TAG, "Effort ${effort_seekbar.progress}")
    }
}