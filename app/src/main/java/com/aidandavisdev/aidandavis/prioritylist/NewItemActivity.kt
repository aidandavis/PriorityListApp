package com.aidandavisdev.aidandavis.prioritylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_new_item.*
import java.util.*

/**
 * Created by Aidan Davis on 16/12/2017.
 */

class NewItemActivity : AppCompatActivity() {

    private val TAG = "NewItemActivity"

    companion object {
        fun open(context: Context) {
            val openIntent = Intent(context, NewItemActivity::class.java)
            context.startActivity(openIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_item)
        val db = FirebaseFirestore.getInstance()
        val uId = FirebaseAuth.getInstance().currentUser?.uid

        item_create_button.setOnClickListener({ createItem(db, uId) })
    }

    private fun createItem(db: FirebaseFirestore, uId: String?) {
        if (item_name.text.isEmpty()) {
            Toast.makeText(this, "Enter an item name", Toast.LENGTH_SHORT).show()
            return
        }

        if (uId != null) {
            item_create_button.isEnabled = false
            val item = HashMap<String, Any>()
            item.put("name", item_name.text.toString())
            item.put("description", item_description.text.toString())
            item.put("importance", importance_seekbar.progress + 1)
            item.put("urgency", urgency_seekbar.progress + 1)
            item.put("effort", effort_seekbar.progress + 1)

            db.collection("users")
                    .document(uId)
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

            item_create_button.isEnabled = true
        }

        Log.i(TAG, "Name ${item_name.text}")
        Log.i(TAG, "Description ${item_description.text}")
        Log.i(TAG, "Importance ${importance_seekbar.progress}")
        Log.i(TAG, "Urgency ${urgency_seekbar.progress}")
        Log.i(TAG, "Effort ${effort_seekbar.progress}")
    }

}