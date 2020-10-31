package com.example.projecshiftappkotlin

//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.FileOutputStream
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {
    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    private lateinit var sharedPreferences: SharedPreferences
    var toolbar: Toolbar? = null
   private lateinit var loginStatus: String
    var p_number: String? = null
    val SEND_SMS_PERMISSION_REQUEST_CODE = 1
    var send: Button? = null
    var progressBar_main: ProgressBar? = null
    var msg: EditText? = null
    var name: EditText? = null
    var location: EditText? = null
    var user: String? = null
    var inspector: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /* init the instances of each element*/
        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        toolbar = findViewById(R.id.toolbar)
        msg = findViewById(R.id.inMsg)
        name = findViewById(R.id.fname)
        location = findViewById(R.id.input_area)
        send = findViewById(R.id.send_btn)
        progressBar_main = findViewById(R.id.progressBar_main)
        navigationView?.setNavigationItemSelectedListener(this)
        ////////////////////////////////////////////// Start NavigationBar&Menu
        /* set the Tool bar*/setSupportActionBar(toolbar)
        /* set the Navigation Drawer Menu*/
        //hide/show items
        // here we will define the items we want to hide/show Menu menu = navigationView.getMenu();
        navigationView?.bringToFront()
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()
        navigationView?.setCheckedItem(R.id.nav_home)
        //////////////////////////////////////////////// End
        send?.isEnabled = false
        if (checkPermission(Manifest.permission.SEND_SMS)) {
            send?.isEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.SEND_SMS
                ), SEND_SMS_PERMISSION_REQUEST_CODE
            )
        }
        loginStatus = sharedPreferences.getString(resources.getString(
                R.string.preLoginState
            ), "").toString()
        if (loginStatus == "loggedIn") {
            user = sharedPreferences.getString("user", null).toString()
            inspector = sharedPreferences.getString("inspector", null).toString()
        } else {
            val `in` = intent
            user = Objects.requireNonNull(`in`.extras)?.getString("user")
            inspector =
                Objects.requireNonNull(`in`.extras)?.getString("inspector")
        }
        send?.setOnClickListener(View.OnClickListener {
            if (TextUtils.isEmpty(name?.getText().toString()) &&
                TextUtils.isEmpty(location?.getText().toString())
            ) {
                name?.error = "Name is Required."
                location?.error = "Location is Required"
                return@OnClickListener
            }
            if (TextUtils.isEmpty(name?.text.toString())) {
                name?.setError("Name is Required.")
                return@OnClickListener
            }
            if (TextUtils.isEmpty(location?.text.toString())) {
                location?.error = "Location is Required."
            }
            progressBar_main?.visibility = View.VISIBLE
            user?.let { it1 -> takeInfoFromDB(it1) }
        })
    }

    fun onSend() {
        var message = msg!!.text.toString()
        if (message.isEmpty()) {
            Toast.makeText(
                this, "Write your Shift info before you send!",
                Toast.LENGTH_SHORT
            ).show()
            return
        } else if (p_number == null || p_number!!.isEmpty()) {
            Toast.makeText(
                this, "Can not send SMS to this kind of number!"
                , Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (checkPermission(Manifest.permission.SEND_SMS)) {
            val smsManager = SmsManager.getDefault()
            message = ("היי " + name!!.text.toString() + " זרק ברגע זה משמרת באזור עבודה: " +
                    location!!.text
                        .toString() + ".\nתוכן המסירה:\n " + message + "\n מספר לחזררה: "
                    + p_number)
            // smsManager.sendTextMessage(p_number,null,message,null,null);
            val messageList =
                smsManager.divideMessage(message)
            smsManager.sendMultipartTextMessage(
                p_number, null, messageList,
                null, null
            )
            Toast.makeText(this, "Message Sent!", Toast.LENGTH_SHORT).show()
            progressBar_main!!.visibility = View.GONE
        } else {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            progressBar_main!!.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        if (drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            drawerLayout!!.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun checkPermission(permission: String?): Boolean {
        val check = ContextCompat.checkSelfPermission(this, permission!!)
        return check == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == SEND_SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                send!!.isEnabled = true
            }
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
                intent = Intent(this@MainActivity, profile_Activity::class.java)
                if (loginStatus != "loggedIn") {
                    val `in` = getIntent()
                    intent.putExtra(
                        "user", Objects.requireNonNull(`in`.extras)
                            ?.getString("user")
                    )
                    intent.putExtra("inspector", `in`.extras!!.getString("inspector"))
                }
                startActivityForResult(intent, 2)
                Toast.makeText(this, "פרופיל עובד", Toast.LENGTH_LONG).show()
            }
            R.id.data_pass -> {
                intent = Intent(this@MainActivity, WeeklyShiftActivity::class.java)
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
                    @SuppressLint("CommitPrefEdits")
                    val editor = sharedPreferences.edit()
                    editor.putString(resources.getString(R.string.preLoginState), "loggedOut")
                    editor.putString("user", null)
                    editor.putString("inspector", null)
                    editor.apply()
                }
                startActivity(Intent(this@MainActivity, Login::class.java))
                finish()
            }
        }
        drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    private fun takeInfoFromDB(user: String) {
        //final ArrayList list2=new ArrayList<String>();
        val myRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        try {
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        if (ds.child(user).exists()) {
                            for (d in ds.children) {
                                if (d.child("phone").value != ds.child(user)
                                        .child("phone").value
                                ) {
                                    p_number = Objects.requireNonNull(
                                        Objects.requireNonNull<Any>(
                                            d.child("phone").value
                                        ).toString()
                                    )
                                    onSend()
                                }
                            }
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
                logFile = openFileOutput("MainLog.txt", Context.MODE_APPEND)
                logFile?.write(
                    """${Calendar.getInstance().time}${error.toString()}
                --------------------------------""".toByteArray())
                logFile?.close()
                Toast.makeText(this@MainActivity, "Save in log", Toast.LENGTH_SHORT
                ).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
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
        private const val TAG = "MainActivity"
    }
}
