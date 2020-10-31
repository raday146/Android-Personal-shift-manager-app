package com.example.projecshiftappkotlin

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


class WeeklyShiftActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {
    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var dialog: Dialog? = null
    var toolbar: Toolbar? = null
    private lateinit var loginStatus: String
    var plus_btn: Button? = null
    var datePicker_btn: Button? = null
    var counter: String? = null
    var to_Day: String? = null
    var user_id: String? = null
   private lateinit var sp: SharedPreferences
    var counter_view: TextView? = null
    var dateview: TextView? = null
    var bell: MediaPlayer? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weekly_shift)
        counter_view = findViewById(R.id.counter_view)
        drawerLayout = findViewById(R.id.drawer_layout3)
        navigationView = findViewById(R.id.nav_view3)
        to_Day = "No date selected"
        toolbar = findViewById(R.id.toolbar3)
        dateview = findViewById(R.id.dateview)
        bell = MediaPlayer.create(this@WeeklyShiftActivity, R.raw.bell)
        plus_btn = findViewById(R.id.plus_btn)
        datePicker_btn = findViewById(R.id.datePicker_btn)
        navigationView?.setNavigationItemSelectedListener(this)
        sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        loginStatus = sp.getString(resources.getString(
                R.string.preLoginState
            ), "").toString()
        user_id = if (loginStatus == "loggedIn") {
            sp.getString("user", null).toString()
        } else {
            val `in` = intent
            Objects.requireNonNull(`in`.extras)?.getString("user")
        }
        takeCountFromDB(user_id)

        ////////////////////////////////////////////////////////////////
        /* set the Tool bar*/setSupportActionBar(toolbar)

        /* set the Navigation Drawer Menu*/
        //hide/show items
        // here we will define the items we want to hide/show Menu menu = navigationView.getMenu();
        navigationView?.bringToFront()
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()
        navigationView?.setCheckedItem(R.id.nav_home)
        //////////////////////////////////////////////////////////////
        val calendar =
            Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.clear()
        val toDay = MaterialDatePicker.todayInUtcMilliseconds()
        //build the calender constrains/////////////////
        calendar.timeInMillis = toDay
        calendar[Calendar.MONTH] = Calendar.JANUARY
        val january = calendar.timeInMillis
        calendar[Calendar.MONTH] = Calendar.DECEMBER
        val december = calendar.timeInMillis
        val constrainBuilder = CalendarConstraints.Builder()
        constrainBuilder.setStart(january)
        constrainBuilder.setEnd(december)
        //////////////////////////////////////////////////
        /*Build the Calendar picker */
        val builder: MaterialDatePicker.Builder<Long> = MaterialDatePicker.Builder.datePicker()
        builder.setTitleText("Select Date")
        builder.setSelection(toDay)
        builder.setCalendarConstraints(constrainBuilder.build())
        val materialDatePicker = builder.build()
        datePicker_btn?.setOnClickListener {
            materialDatePicker.show(
                supportFragmentManager,
                "Date Picker"
            )
        }
        materialDatePicker.addOnPositiveButtonClickListener {
            val mPicker = checkValidPickerDate(
                materialDatePicker.headerText
                    .substring(0, 3)
            )
            val mNow = checkValidPickerDate(
                Calendar.getInstance().time.toGMTString().substring(3, 6)
            )
            if (mPicker > mNow) {
                dateview?.text = "Selected Day " + materialDatePicker.headerText
                to_Day = materialDatePicker.headerText
            } else if (mPicker == mNow) {
                val dayPicker = materialDatePicker.headerText.substring(4, 6).toInt()
                val dayNow = Calendar.getInstance().time
                    .toGMTString().substring(0, 2).toInt()
                if (dayNow <= dayPicker) {
                    dateview?.text = "Selected Day " + materialDatePicker.headerText
                    to_Day = materialDatePicker.headerText
                } else {
                    Toast.makeText(
                        this@WeeklyShiftActivity, "Try other day",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@WeeklyShiftActivity, "Try again",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        plus_btn?.setOnClickListener {
            counter = counter_view?.text.toString()
            val count = counter!!.toInt()
            if (counter!!.toInt() < 10) {
                if (count > 3) {
                    createDialog(counter!!)
                    if (count < 6) {
                        bell?.start()
                    } else if (count >= 8) {
                        bell?.start()
                    }
                }
                counter = counter_view?.text.toString()
                updateCount()
            } else {
                Toast.makeText(
                    this@WeeklyShiftActivity, "You have already " +
                            "passed the legal amount of shifts, so you can no longer throw " +
                            "shifts.", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkValidPickerDate(d: String?): Int {
        when (d) {
            "Jan" -> return 1
            "Feb" -> return 2
            "Mar" -> return 3
            "Apr" -> return 4
            "May" -> return 5
            "Jun" -> return 6
            "Jul" -> return 7
            "Aug" -> return 8
            "Sep" -> return 9
            "Oct" -> return 10
            "Nov" -> return 11
            "Dec" -> return 12
        }
        return 0
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
                        "user", Objects.requireNonNull(
                            `in`.extras
                        )?.getString("user")
                    )
                    intent.putExtra("inspector", `in`.extras!!.getString("inspector"))
                }
                startActivityForResult(intent, 2)
                Toast.makeText(this, "חלון הפעולות", Toast.LENGTH_LONG).show()
            }
            R.id.nav_profile -> {
                intent = Intent(this, profile_Activity::class.java)
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

    fun DaysBetween(c: Calendar, fromTime: Long, toTime: Long): Int {
        var result = 0
        if (toTime <= fromTime) {
            return result
        }
        c.timeInMillis = toTime
        val toYear = c[Calendar.YEAR]
        result += c[Calendar.DAY_OF_YEAR]
        c.timeInMillis = fromTime
        result -= c[Calendar.DAY_OF_YEAR]
        while (c[Calendar.YEAR] < toYear) {
            result += c.getActualMaximum(Calendar.DAY_OF_YEAR)
            c.add(Calendar.YEAR, 1)
        }
        return TimeUnit.MILLISECONDS.toDays(result.toLong()).toInt()
    }

    private fun takeCountFromDB(user: String?) {
        val myRef = FirebaseDatabase.getInstance().reference
        val date = Calendar.getInstance()
        val currentDay = date.timeInMillis

        try {
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (ds in dataSnapshot.children) {
                        if (ds.child(user!!).exists()) {
                            val last_time = Objects.requireNonNull(ds.child(user).child(
                                    "first threw").value).toString().toLong()
                            // int daysPas = currentDay - last_time;
                            val days = DaysBetween(date, last_time, currentDay)
                            // Toast.makeText(WeeklyShiftActivity.this,String.valueOf(days),
                            // Toast.LENGTH_SHORT).show();
                            if (days >= 7) {
                                val key = ds.key
                                myRef.child(key!!).child(user_id!!).child(
                                    "counter shifts")
                                    .setValue(0)
                                myRef.child(key).child(user_id!!).child("first threw")
                                    .setValue(0)
                                myRef.child(key).child(user_id!!).child("shift day")
                                    .removeValue()
                            } else {
                                counter_view!!.text = ds?.child(user)?.child(
                                    "counter shifts")?.value.toString()
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
                logFile = openFileOutput("WeeklyShiftLog.txt", Context.MODE_APPEND)
                logFile?.write(
                    """${Calendar.getInstance().time}$error--------------------------------
                        |
                    """.trimMargin().toByteArray()
                )
                logFile?.close()
                Toast.makeText(
                    this@WeeklyShiftActivity, "Save in log",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateCount() {
        val myRef = FirebaseDatabase.getInstance().reference
        val date = Calendar.getInstance()
        val currentDay = date.timeInMillis
        //  myRef.child(inspector).getRoot().child(user_id).child("counter shifts").setValue(counter);
        try {
            myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children) {
                        if (ds.child(user_id!!).exists()) {
                            val tmp = Objects.requireNonNull(
                                ds.child(user_id!!)
                                    .child("counter shifts").value
                            ).toString().toInt()
                            val key = ds.key
                            if (tmp == 0) { // will save the first day shift in millisec in database
                                myRef.child(key!!).child(user_id!!).child("first threw")
                                    .setValue(currentDay)
                            }
                            if (to_Day == "No date selected") {
                                myRef.child(key!!).child(user_id!!).child("shift date")
                                    .child(to_Day!!).setValue("thrown on the date " +
                                            Calendar.getInstance().time.toGMTString())
                                counter = (counter!!.toInt() + 1).toString()
                                counter_view!!.text = counter
                                myRef.child(key).child(user_id!!).child("counter shifts")
                                    .setValue(counter)
                            } else if (ds.child(user_id!!).child("shift date").child(to_Day!!)
                                    .exists()
                            ) {
                                Toast.makeText(
                                    this@WeeklyShiftActivity,
                                    "You already threw on this date",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                myRef.child(key!!).child(user_id!!).child("shift date")
                                    .child(to_Day!!)
                                    .setValue("I threw a shift")
                                counter = (counter!!.toInt() + 1).toString()
                                counter_view!!.text = counter
                                myRef.child(key).child(user_id!!).child("counter shifts")
                                    .setValue(counter)
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
                logFile = openFileOutput("WeeklyShiftLog.txt", Context.MODE_APPEND)
                logFile?.write(
                    """${Calendar.getInstance().time}$error--------------------------------
                        |
                    """.trimMargin().toByteArray())
                logFile?.close()
                Toast.makeText(this@WeeklyShiftActivity, "Save in log",
                    Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun createDialog(counter: String) {
        if (counter.toInt() in 4..5) {
            dialog = Dialog(this)
            dialog!!.setContentView(R.layout.dialog_alert)
            dialog!!.setTitle("Alert")
            dialog!!.setCancelable(true)
            val win: Window? = dialog?.window
            dialog!!.show()
        } else if (counter.toInt() > 7) {
            dialog = Dialog(this)
            dialog!!.setContentView(R.layout.dialog_msg)
            dialog!!.setTitle("Login")
            dialog!!.setCancelable(true)
            dialog!!.show()
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
        private const val TAG = "WeeklyShiftActivity"
    }
}