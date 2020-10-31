package com.example.projecshiftappkotlin

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.row_item.view.*

class Adapter(val arrayList: ArrayList<Model>, val context:Myshift):
    RecyclerView.Adapter<Adapter.ViewHolder>() {
    class ViewHolder (itemView: View): RecyclerView.ViewHolder(itemView){
        fun bindItems(model: Model){
           Log.i("i1","chap1")
            itemView.cal_info.text = model.day
            Log.i("i1",model.day.toString())
            itemView.month_info.text = model.month
            Log.i("i1","chap1.2")

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.i("i1","chap2")
        val  v = LayoutInflater.from(parent.context).inflate(R.layout.fragment_myshift, parent, false)
        Log.i("i1","chap2.5")
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i("i1","chap3")
      holder.bindItems(arrayList[position])
    }

    override fun getItemCount(): Int {
        if(arrayList.size > 0) {
            return arrayList.size
            Log.i("i1","chap3.5")
        }
        return 0
    }
}