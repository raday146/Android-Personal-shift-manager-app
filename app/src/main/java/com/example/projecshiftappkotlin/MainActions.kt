package com.example.projecshiftappkotlin

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import com.example.projecshiftappkotlin.R.string.navigation_drawer_close
import com.example.projecshiftappkotlin.R.string.navigation_drawer_open
import com.google.android.material.navigation.NavigationView
import java.util.*

class MainActions : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    var drawerLayout: DrawerLayout? = null
    var navigationView: NavigationView? = null
    var toolbar: Toolbar? = null
    private lateinit var add_shift: CardView
    private lateinit var threw_shift: CardView
    private lateinit var shift_status: CardView
    private lateinit var my_shift: CardView
    private lateinit var setting: CardView
    private lateinit var w_profile: CardView
    private var user: String? = null
    private var flag: Boolean = false
    private var inspector: String? = null
    private lateinit var loginStatus: String
    private lateinit var sp: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)
        drawerLayout = findViewById(R.id.drawer_layout4)
        navigationView = findViewById(R.id.nav_view4)
        toolbar = findViewById(R.id.toolbar4)
        navigationView?.setNavigationItemSelectedListener(this)
        add_shift = findViewById(R.id.add_shift)
        threw_shift = findViewById(R.id.threw_shift)
        shift_status = findViewById(R.id.shift_status)
        my_shift = findViewById(R.id.my_shift)
        setting = findViewById(R.id.setting)
        w_profile = findViewById(R.id.user_profile)
        sp = getSharedPreferences("userInfo", Context.MODE_PRIVATE)

        ////////////////////////////////////////////////////////////////
        /* set the Tool bar*/
        setSupportActionBar(toolbar)

        /* set the Navigation Drawer Menu*/
        //hide/show items
        // here we will define the items we want to hide/show Menu menu = navigationView.getMenu();
        navigationView?.bringToFront()
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar,
            navigation_drawer_open,
            navigation_drawer_close
        )
        drawerLayout?.addDrawerListener(toggle)
        toggle.syncState()
        navigationView?.setCheckedItem(R.id.nav_home)
        //////////////////////////////////////////////////////////////
        loginStatus = sp.getString(resources.getString(
            R.string.preLoginState
        ), "").toString()
        if (loginStatus == "loggedIn") {
            user = sp.getString("user", null).toString()
            inspector = sp.getString("inspector", null).toString()
            flag = true
        } else {
            val `in` = intent
            user = Objects.requireNonNull(`in`.extras)?.getString("user")
            inspector = Objects.requireNonNull(`in`.extras)?.getString("inspector")
            flag = false
        }
        add_shift.setOnClickListener(View.OnClickListener {
            return@OnClickListener
            /**
            val intent = Intent(this, MainActivity::class.java)
            if (!flag) {
            intent.putExtra("user", user)
            intent.putExtra("inspector", inspector)
            }
            startActivityForResult(intent, 2)
            finish()
             */
        })
       threw_shift.setOnClickListener(View.OnClickListener {
          val intent = Intent(this, MainActivity::class.java)
          if (!flag) {
              intent.putExtra("user", user)
              intent.putExtra("inspector", inspector)
          }
          startActivityForResult(intent, 2)
          finish()
       })
        shift_status.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, WeeklyShiftActivity::class.java)
            if (!flag) {
                intent.putExtra("user", user)
                intent.putExtra("inspector", inspector)
            }
            startActivityForResult(intent, 2)
            finish()
        })
        my_shift.setOnClickListener(View.OnClickListener {
             val movFragment = supportFragmentManager.beginTransaction().replace(R.id.fr1, Myshift() )
             movFragment.addToBackStack(null)
             movFragment.commit()
            /**
            val intent = Intent(this, MainActivity::class.java)
            if (!flag) {
                intent.putExtra("user", user)
                intent.putExtra("inspector", inspector)
            }
            startActivityForResult(intent, 2)
            finish()
            */
        })
        setting.setOnClickListener(View.OnClickListener {
            return@OnClickListener
            /**
            val intent = Intent(this, MainActivity::class.java)
            if (!flag) {
            intent.putExtra("user", user)
            intent.putExtra("inspector", inspector)
            }
            startActivityForResult(intent, 2)
            finish()
             */
        })
        w_profile.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, profile_Activity::class.java)
            if (!flag) {
            intent.putExtra("user", user)
            intent.putExtra("inspector", inspector)
            }
            startActivityForResult(intent, 2)
            finish()
        })

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val intent: Intent
        when (item.itemId) {
            R.id.nav_home -> {
            }
            R.id.nav_profile -> {
                intent = Intent(this, profile_Activity::class.java)
                if (!flag) {
                    intent.putExtra("user", user)
                    intent.putExtra("inspector", inspector)
                }
                startActivityForResult(intent, 2)
                Toast.makeText(this, "פרופיל עובד", Toast.LENGTH_LONG).show()
            }
            R.id.data_pass -> {
                intent = Intent(this, WeeklyShiftActivity::class.java)
                if (!flag) {
                    intent.putExtra("user", user)
                    intent.putExtra("inspector", inspector)
                }
                startActivityForResult(intent, 2)
                Toast.makeText(this, "סטטוס מסירות", Toast.LENGTH_LONG).show()
            }
            R.id.exit -> {
                if (loginStatus == "loggedIn") {
                    @SuppressLint("CommitPrefEdits")
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
}