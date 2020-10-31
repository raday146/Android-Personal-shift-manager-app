package com.example.projecshiftappkotlin

import android.content.Context
import com.android.volley.Cache
import com.android.volley.Network
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.Volley

/**
 * restricts the instantiation of a class to one "single" instance.
 * This is useful when exactly one object is needed to coordinate actions across the system
 * The singleton design pattern solves problems like:
 * How can it be ensured that a class has only one instance?
 * How can the sole instance of a class be accessed easily?
 * How can a class control its instantiation?
 * How can the number of instances of a class be restricted?
 */
class mySingleton(private val context: Context) {
    private var requestQueue: RequestQueue?

    /**
     * A RequestQueue needs two things to do its job: a network to perform transport of the requests,
     * and a cache to handle caching. There are standard implementations of these available in the Volley
     * toolbox: DiskBasedCache provides a one-file-per-response cache with an in-memory index, and
     * BasicNetwork provides a network transport based on your preferred HTTP client.
     * BasicNetwork is Volley's default network implementation. A BasicNetwork must be initialized
     * with the HTTP client your app is using to connect to the network. Typically this
     * is an HttpURLConnection.
     */
    fun getRequestQueue(): RequestQueue? {
        if (requestQueue == null) {
            val cache: Cache = DiskBasedCache(context.cacheDir, 1024 * 1024)
            val network: Network = BasicNetwork(HurlStack())
            requestQueue = RequestQueue(cache, network)
            requestQueue = Volley.newRequestQueue(context.applicationContext)
        }
        return requestQueue
    }

    fun <T> addIntoRequestQueue(request: Request<T>?) {
        requestQueue!!.add(request)
    }

    companion object {
        private var instance: mySingleton? = null

        @Synchronized
        fun getInstance(context: Context): mySingleton? {
            if (instance == null) {
                instance = mySingleton(context)
            }
            return instance
        }
    }

    init {
        requestQueue = getRequestQueue()
    }
}
