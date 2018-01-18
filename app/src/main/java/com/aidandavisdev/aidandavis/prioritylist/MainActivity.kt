package com.aidandavisdev.aidandavis.prioritylist

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val TAG = "MainActivity"

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mPriorityListAdapter: PriorityListItemAdapter

    private var listSelected = "" // if empty list, then see all items ('master list')
    private val lists = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { CreateEditItemActivity.open(this, null, listSelected) }

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
        supportActionBar?.title = "Master List"

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
            updateLists()
            updateItems()
        }
    }

    private fun updateLists() {
        getUserDoc(mAuth.currentUser!!.uid)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result.exists()) {
                            lists.clear()
                            nav_view.menu.clear()
                            menuInflater.inflate(R.menu.main_drawer_menu, nav_view.menu)
                            val listMenu = nav_view.menu.addSubMenu(R.string.list_submenu_title)
                            for (listName in task.result["lists"] as ArrayList<String>) {
                                lists.add(listName)
                                listMenu.add(listName)
                            }
                            nav_view.invalidate()
                        } else {
                            val emptyList = HashMap<String, Any>()
                            emptyList.put("lists", ArrayList<String>())
                            getUserDoc(mAuth.currentUser!!.uid)
                                    .set(emptyList)
                            updateLists()
                        }
                    } else {
                        Log.w(TAG, "Error getting lists", task.exception)
                    }
                }
    }

    private fun updateItems() {
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

    private fun showTickedItems() {
        val itemList = ArrayList<PrioritisedItem>()
        getTickedItems(mAuth.currentUser!!.uid)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.delete_list -> {
                if (listSelected == "") {
                    Toast.makeText(this, "You can't delete this list", Toast.LENGTH_SHORT).show()
                    return true
                } else {
                    Toast.makeText(this, "Items not deleted, they will still show in master list", Toast.LENGTH_SHORT).show()
                    lists.remove(listSelected)
                    getUserDoc(mAuth.currentUser!!.uid)
                            .update("lists", lists)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    updateLists()
                                    listSelected = ""
                                    updateItems()
                                    supportActionBar?.setTitle("Master List")
                                } else {
                                    Log.w(TAG, "Error adding list", task.exception)
                                }
                            }
                    return true
                }
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // un-check all items first
        for (i in 0 until nav_view.menu.size()) {
            val menu = nav_view.menu.getItem(i)
            menu.isChecked = false
            if (menu.hasSubMenu()) {
                val subMenu = menu.subMenu
                for (j in 0 until subMenu.size()) {
                    subMenu.getItem(j).isChecked = false
                }
            }
        }
        when {
            item.itemId == R.id.add_list -> createAddListDialogue()
            item.itemId == R.id.ticked_items -> {
                item.isChecked = true
                showTickedItems()
                supportActionBar?.setTitle("Ticked Items")
            }
            item.itemId == R.id.master_list -> {
                item.isChecked = true
                listSelected = ""
                updateItems()
                supportActionBar?.setTitle("Master List")
            }
            else -> {
                item.isChecked = true
                listSelected = item.title.toString()
                updateItems()
                supportActionBar?.setTitle(item.title)
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun createAddListDialogue() {
        val dialogView = this.layoutInflater.inflate(R.layout.dialog_new_list, null)
        AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Create", { _, _ ->
                    val enteredName = dialogView.findViewById<EditText>(R.id.new_list_text).text.toString()
                    if (enteredName != "") {
                        lists.add(enteredName)
                        getUserDoc(mAuth.currentUser!!.uid)
                                .update("lists", lists)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        updateLists()
                                    } else {
                                        Log.w(TAG, "Error adding list", task.exception)
                                    }
                                }
                    }
                })
                .setNegativeButton("Cancel", { dialog, _ ->
                    dialog.cancel()
                })
                .create().show()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


    companion object {
        fun getUserDoc(uId: String): DocumentReference {
            return FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uId)
        }

        fun getItemsCollection(uId: String): CollectionReference {
            return getUserDoc(uId)
                    .collection("items")
        }

        fun getItem(uId: String, itemId: String): DocumentReference {
            return getItemsCollection(uId)
                    .document(itemId)
        }

        // empty list means get all lists
        fun getItemsInList(uId: String, list: String): Query {
            return if (list != "") {
                getItemsCollection(uId)
                        .whereEqualTo("list", list)
                        .whereEqualTo("ticked", false)
            } else {
                return getItemsCollection(uId)
                        .whereEqualTo("ticked", false)
            }
        }

        fun getTickedItems(uId: String): Query {
            return getItemsCollection(uId)
                    .whereEqualTo("ticked", true)
        }
    }
}
