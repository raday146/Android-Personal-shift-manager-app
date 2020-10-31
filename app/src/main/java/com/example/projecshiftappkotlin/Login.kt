package com.example.projecshiftappkotlin


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import org.json.JSONException
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*

class Login : AppCompatActivity() {
    var workerId:EditText? =null;
    var password:EditText?=null
   private lateinit var sharedPreferences:SharedPreferences
    lateinit var loginState:CheckBox
    var mLoginBtn:Button?=null
    var top_anim: Animation? = null
   // var bottom_anim: Animation? = null
   private var img_layout: LinearLayout?=null
    var progressBar:ProgressBar?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE)
        workerId = findViewById(R.id.workerID)
        password = findViewById(R.id.workerPass)
        progressBar = findViewById(R.id.progressBar)
        loginState = findViewById(R.id.loginState)
        mLoginBtn = findViewById(R.id.click)
        top_anim = AnimationUtils.loadAnimation(this, R.anim.top_animation)
        // bottom_anim = AnimationUtils.loadAnimation(this, R.anim.buttom_animation)
        img_layout = findViewById(R.id.imageView)
        img_layout?.animation = top_anim
        mLoginBtn?.setOnClickListener(View.OnClickListener {
            val Id:String= workerId?.text.toString().trim()
            val workerPassword:String = password?.text.toString().trim()
            if(TextUtils.isEmpty(Id) && TextUtils.isEmpty(workerPassword)){
              workerId?.error = "ID is Required."
              password?.error = "Password is Required."
              return@OnClickListener
            }
            if(TextUtils.isEmpty(Id)){
             workerId?.error = "ID is Required."
             return@OnClickListener
            }
            if(TextUtils.isEmpty(workerPassword)){
             password?.error = "Password is Required."
             return@OnClickListener
            }
            progressBar?.visibility = View.VISIBLE

            // put here the connection func
            login(Id,workerPassword)

        })
        val loginStatus: String = sharedPreferences.getString(
            "loginState","").toString()
        if (loginStatus == "loggedIn")
        {
            Toast.makeText(this,"Logged in successfully", Toast.LENGTH_SHORT)
                .show()
            startActivity(Intent(this,MainActivity::class.java))
        }
    }

    private fun login(workerId:String, password:String){
     val url ="It is necessary to insert a connection link to the server in order to verify the identity"
        val request: StringRequest = object : StringRequest(Method.POST, url,
            Response.Listener<String>{ response ->
                try {
                    if (response != "Error: mismatch ID or password" &&
                        response != "user not exist") {
                        val intent = Intent(this, MainActions::class.java)
                        Toast.makeText(
                            this, "Login Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val editor = sharedPreferences.edit()
                        if (loginState.isChecked) {
                            editor.putString(resources.getString(R.string.preLoginState), "loggedIn")
                            editor.putString("user", workerId)
                            editor.putString("inspector", response)
                        } else {
                            editor.putString(resources.getString(R.string.preLoginState), "loggedOut")
                            editor.putString("user", null)
                            editor.putString("inspector", null)
                            intent.putExtra("user", workerId)
                            intent.putExtra("inspector", response)
                        }
                        editor.apply()
                        startActivityForResult(intent, 2)
                        finish()
                    } else {
                        Toast.makeText(this, response + workerId + password,
                            Toast.LENGTH_SHORT).show()
                        progressBar!!.visibility = View.GONE
                    }
                }catch (e:JSONException){
                    var logFile1: FileOutputStream? = null
                    logFile1 = openFileOutput("ServerConnectionLog.txt",
                        Context.MODE_APPEND
                    )
                    logFile1?.write("""${Calendar.getInstance().time} ${e.message}-------------
|                    -------------------""".trimMargin().toByteArray()
                    )
                    logFile1?.close()
                    Toast.makeText(this, "Save log", Toast.LENGTH_SHORT)
                        .show()
                    //use this json as you want

                }
            },
            Response.ErrorListener { error ->
                val response = error.networkResponse
                if (response != null && response.statusCode == 404) {
                    try {
                        val charset: Charset = Charsets.UTF_8
                        val res:String  = String(response.data, charset)
                        // Now you can use any deserializer to make sense of data
                        val obj = JSONObject(res)
                        var logFile: FileOutputStream? = null
                        logFile = openFileOutput("ServerConnectionLog.txt",
                            Context.MODE_APPEND
                        )
                        logFile?.write("""${Calendar.getInstance().time} $obj 
|                       --------------------------------""".trimMargin().toByteArray()
                        )
                        logFile?.close()
                        Toast.makeText(this@Login, "Save log", Toast.LENGTH_SHORT)
                            .show()
                        //use this json as you want
                    } catch (e1: UnsupportedEncodingException) {
                        // Couldn't properly decode data to string
                        Toast.makeText(this, e1.message, Toast.LENGTH_SHORT)
                            .show()
                    } // returned data is not JSONObject?
                    catch (e1: JSONException) {
                        Toast.makeText(this, e1.message, Toast.LENGTH_SHORT)
                            .show()
                    } catch (e1: FileNotFoundException) {
                        Toast.makeText(this, e1.message, Toast.LENGTH_SHORT)
                            .show()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                Toast.makeText(this, error.toString(), Toast.LENGTH_SHORT)
                    .show()
                progressBar!!.visibility = View.GONE
            }) {

            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {
                val param = HashMap<String, String>()
                param["workerId"] = workerId
                param["password"] = password
                return param
            }

        }
        request.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        mySingleton.getInstance(this)!!.addIntoRequestQueue(request)
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
        private const val TAG = "LoginActivity"
    }
}