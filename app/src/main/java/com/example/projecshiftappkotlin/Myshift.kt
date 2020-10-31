package com.example.projecshiftappkotlin

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projecshiftappkotlin.R.id.recyclerView
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_myshift.view.*
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
/**
 * A simple [Fragment] subclass.
 * Use the [Myshift.newInstance] factory method to
 * create an instance of this fragment.
 */
class Myshift : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var recyclerView: RecyclerView
    private lateinit var arrayList: ArrayList<Model>
    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("i1","chap0")
        arrayList = ArrayList()
        arrayList.add(Model(Calendar.DAY_OF_MONTH.toString(), Calendar.DATE.toString()))
        arrayList.add(Model(Calendar.DAY_OF_MONTH.toString(), Calendar.DATE.toString()))
        arrayList.add(Model(Calendar.DAY_OF_MONTH.toString(), Calendar.DATE.toString()))
        Log.i("i1","chap.5")
        adapter = Adapter(arrayList, this)
        Log.i("i1", adapter.arrayList[0].day.toString())

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        Log.i("i1","chap00")
        val view = inflater.inflate(R.layout.fragment_myshift, container, false)
        //arrayList = ArrayList<Model>()
        recyclerView = view.findViewById(R.id.recyclerView) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this.context)
        Log.i("i1","chap4")
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        Log.i("i1","chap5--------------------")
        return view
    }

}