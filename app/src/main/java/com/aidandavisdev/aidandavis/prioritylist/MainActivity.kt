package com.aidandavisdev.aidandavis.prioritylist

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var mAuth: FirebaseAuth

    private lateinit var mPriorityListAdapter: PriorityListItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { CreateEditItemActivity.open(this, null) }

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
        val currentUser = mAuth.currentUser
        if (currentUser == null) {
            val providers: List<AuthUI.IdpConfig> = Arrays.asList(
                    AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()
            )
            startActivity(AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build())
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mAuth.currentUser != null) {
            val itemList = ArrayList<PrioritisedItem>()
            val uId = mAuth.currentUser!!.uid
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uId)
                    .collection("list1")
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result.mapTo(itemList) {
                                PrioritisedItem(
                                        it.id,
                                        it.data["name"] as String,
                                        it.data["description"] as String,
                                        it.data["startDate"] as Date?,
                                        it.data["endDate"] as Date?,
                                        (it.data["importance"] as Long).toInt(),
                                        (it.data["effort"] as Long).toInt(),
                                        if (it.data["ticked"] != null) {
                                            it.data["ticked"] as Boolean
                                        } else {
                                            false
                                        }
                                )
                            }
                            mPriorityListAdapter.updateList(itemList)
                        } else {
                            Log.w(TAG, "Error getting items", task.exception)
                        }
                    }
        }
    }
}
