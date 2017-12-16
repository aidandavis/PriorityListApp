package com.aidandavisdev.aidandavis.prioritylist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_new_item.*

/**
 * Created by Aidan Davis on 16/12/2017.
 */

class NewItemActivity : AppCompatActivity() {

    val TAG = "NewItemActivity"

    companion object {
        fun open(context: Context) {
            val openIntent = Intent(context, NewItemActivity::class.java)
            context.startActivity(openIntent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_item)

        item_create_button.setOnClickListener({ createItem() })
    }

    private fun createItem() {
        if (item_name.text.isEmpty()) {
            Toast.makeText(this, "Enter an item name", Toast.LENGTH_SHORT).show()
            return
        }

        Log.i(TAG, "Name ${item_name.text}")
        Log.i(TAG, "Description ${item_description.text}")
        Log.i(TAG, "Importance ${importance_seekbar.progress}")
        Log.i(TAG, "Urgency ${urgency_seekbar.progress}")
        Log.i(TAG, "Effort ${effort_seekbar.progress}")
    }

}