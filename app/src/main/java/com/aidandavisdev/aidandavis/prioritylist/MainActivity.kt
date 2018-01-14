package com.aidandavisdev.aidandavis.prioritylist

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mPriorityListAdapter: PriorityListItemAdapter

    private val listSelected = "" // if empty list, then see all items ('master list')

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { CreateEditItemActivity.open(this, null, listSelected) }

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        main_list_view.setHasFixedSize(true)
        main_list_view.layoutManager = LinearLayoutManager(this)
        mPriorityListAdapter = PriorityListItemAdapter()
        main_list_view.adapter = mPriorityListAdapter
    }

    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser == null) {
            val providers: List<AuthUI.IdpConfig> = Arrays.asList(
                    AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
            )
            startActivity(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build())
        }
    }

    override fun onResume() {
        super.onResume()
        if (mAuth.currentUser != null) {
            val itemList = ArrayList<PrioritisedItem>()
            getItemsInList(mAuth.currentUser!!.uid, listSelected)
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result.mapTo(itemList) { mapToPrioritisedItem(it) }
                            mPriorityListAdapter.updateList(itemList, listSelected)
                        } else {
                            Log.w(TAG, "Error getting items", task.exception)
                        }
                    }
        }
    }

    private fun mapToPrioritisedItem(result: DocumentSnapshot): PrioritisedItem {
        return PrioritisedItem(
                result.id,
                result["name"] as String,
                result["description"] as String,
                result["startDate"] as Date?,
                result["endDate"] as Date?,
                (result["importance"] as Long).toInt(),
                (result["effort"] as Long).toInt(),
                result["ticked"] as Boolean)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    companion object {
        fun getItemsCollection(uId: String): CollectionReference {
            return FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uId)
                    .collection("items")
        }

        fun getItem(uId: String, itemId: String): DocumentReference {
            return getItemsCollection(uId)
                    .document(itemId)
        }

        // empty list means get all lists
        fun getItemsInList(uId: String, list: String): Query {
            return getItemsCollection(uId)
                    .whereEqualTo("list", list)
                    .whereEqualTo("ticked", false)
        }

        fun getTickedItems(uId: String): Query {
            return getItemsCollection(uId)
                    .whereEqualTo("ticked", true)
        }
    }
}
