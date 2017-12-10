package com.aidandavisdev.aidandavis.prioritylist

import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private lateinit var mPriorityListAdapter: PriorityListItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        fab.setOnClickListener { } // launch new activity

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

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        mPriorityListAdapter.updateList(mockUpSomeItems())
    }

    private fun mockUpSomeItems(): List<PrioritisedItem> {
        val itemList = ArrayList<PrioritisedItem>()
        itemList.add(PrioritisedItem("", "Walk the dog", "An example item. Importance 4/5, urgency 2/5, effort 4/5", Calendar.getInstance().time, 4, 2, 4))
        itemList.add(PrioritisedItem("", "Pack for tomorrow", "An example item. Importance 2/5, urgency 3/5, effort 1/5", Calendar.getInstance().time, 2, 3, 1))
        itemList.add(PrioritisedItem("", "Breathe", "An example item. Importance 5/5, urgency 4/5, effort 1/5", Calendar.getInstance().time, 5, 4, 1))
        itemList.add(PrioritisedItem("", "Do some gym", "An example item. Importance 5/5, urgency 3/5, effort 5/5", Calendar.getInstance().time, 5, 3, 5))
        itemList.add(PrioritisedItem("", "Write christmas cards", "An example item. Importance 2/5, urgency 3/5, effort 3/5", Calendar.getInstance().time, 2, 3, 3))
        itemList.add(PrioritisedItem("", "Gardening", "An example item. Importance 1/5, urgency 1/5, effort 5/5", Calendar.getInstance().time, 1, 1, 5))

        return itemList
    }
}
