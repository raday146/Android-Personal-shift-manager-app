package com.example.projecshiftappkotlin

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class profile_Activity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {
    var name: TextView? = null
    var last_name: TextView? = null
    var work_area: TextView? = null
    var inspector: TextView? = null
    var phone: TextView? = null
    private val database: FirebaseDatabase? = null
    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    private lateinit var loginStatus: String
    var toolbar: Toolbar? = null
    private lateinit var sp: SharedPreferences
    private var progressBar_profile: ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile__activty)
        name = findViewById(R.id.w_name)
        last_name = findViewById(R.id.last_name)
        work_area = findViewById(R.id.work_area)
        inspector = findViewById(R.id.inspector)
        phone = findViewById(R.id.phone)
        drawerLayout = findViewById(R.id.drawer_layout2)
        navigationView = findViewById(R.id.nav_view2)
        toolbar = findViewById(R.id.toolbar2)
        progressBar_profile = findViewById(R.id.progressBar_profile)
        navigationView?.setNavigationItemSelectedListener(this)
        /* set the Tool bar*/setSupportActionBar(toolbar)

        /* set the Navigation Drawer Menu*/
        //hide/show items
        // here we will define the items we want to hide/show Menu menu = navigationView.getMenu();
        navigationView?.bringToFront()
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close)
        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()
        navigationView?.setCheckedItem(R.id.nav_home)
        //////////////////////////////////////////////////////////////
        var user: String? = ""
        sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        loginStatus = sp.getString(resources.getString(
                R.string.preLoginState
            ), "").toString()
        user = if (loginStatus == "loggedIn") {
            sp.getString("user", null).toString()
        } else {
            val `in` = intent
            Objects.requireNonNull(`in`.extras)?.getString("user")
        }
        progressBar_profile?.visibility = View.VISIBLE
        takeInfoFromDB(user)
        //FirebaseFirestore db = FirebaseFirestore.getInstance();
    }

    private fun takeInfoFromDB(user: String?) {
        val myRef = FirebaseDatabase.getInstance().reference
        try {
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        if (snapshot.child(user!!).exists()) {
                            name!!.text = Objects.requireNonNull(snapshot.child(user).
                            child("name").value
                            ).toString()
                            last_name!!.text = Objects.requireNonNull(
                                snapshot.child(user).child("last name").value
                            ).toString()
                            work_area!!.text = Objects.requireNonNull(snapshot.child(user).
                            child("work area").value
                            ).toString()
                            phone!!.text = Objects.requireNonNull(snapshot.child(user).
                            child("phone").value
                            ).toString()
                            inspector!!.text = Objects.requireNonNull(snapshot.child(user).
                            child("inspector").value
                            ).toString()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        } catch (error: DatabaseException) {
            Log.d(
                TAG,
                "Failed to read value." + error.message
            )
            var logFile: FileOutputStream? = null
            try {
                logFile = openFileOutput("ProfileDataLog.txt", Context.MODE_APPEND)
                logFile.write(
                    """${Calendar.getInstance().time}
$error
--------------------------------
""".toByteArray()
                )
                logFile.close()
                showToast("Save in log")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        progressBar_profile!!.visibility = View.GONE
    }

    fun showToast(Text: String?) {
        runOnUiThread {
            Toast.makeText(
                this@profile_Activity,
                Text, Toast.LENGTH_LONG
            ).show()
            progressBar_profile!!.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        val intent: Intent
        when (menuItem.itemId) {
            R.id.nav_home -> {
                intent = Intent(this, MainActions::class.java)
                if (loginStatus != "loggedIn") {
                    val `in` = getIntent()
                    intent.putExtra(
                        "user", Objects.requireNonNull(`in`.extras)
                            ?.getString("user")
                    )
                    intent.putExtra("inspector", `in`.extras!!.getString("inspector"))
                }
                startActivityForResult(intent, 2)
                Toast.makeText(this, "חלון הפעולות", Toast.LENGTH_LONG).show()
            }
            R.id.nav_profile -> {
            }
            R.id.data_pass -> {
                intent = Intent(this, WeeklyShiftActivity::class.java)
                if (loginStatus != "loggedIn") {
                    val `in` = getIntent()
                    intent.putExtra(
                        "user", Objects.requireNonNull(`in`.extras)
                            ?.getString("user")
                    )
                    intent.putExtra("inspector", `in`.extras!!.getString("inspector"))
                }
                startActivityForResult(intent, 2)
                Toast.makeText(this, "סטטוס מסירות", Toast.LENGTH_LONG).show()
            }
            R.id.exit -> {
                if (loginStatus == "loggedIn") {
                    sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
                    val editor = sp.edit()
                    editor.putString(resources.getString(R.string.preLoginState), "loggedOut")
                    editor.putString("user", null)
                    editor.putString("inspector", null)
                    editor.apply()
                }
                startActivity(Intent(this, Login::class.java))
                finish()
            }
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "-- onStart--")
    }

    override fun onRestart() {
        super.onRestart()
        Log.i(TAG, "-- onRestart--")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "--onResume--")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "--onPause--")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "--onStop--")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "--onDestroy--")
    }

    companion object {
        private const val TAG = "Profile"
    }
}