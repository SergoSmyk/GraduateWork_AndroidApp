package com.example.graduatework.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.graduatework.R
import com.example.graduatework.tf.RecognizedSign
import com.example.graduatework.ui.diff_utils.DiffCallback

class Adapter : RecyclerView.Adapter<Adapter.ViewHolder>() {

    private var items: List<RecognizedSign> = listOf()

    fun setNewItems(new: List<RecognizedSign>) {
        val callback = DiffCallback(new, items)
        val result = DiffUtil.calculateDiff(callback)
        items = new
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sign, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val signName: TextView = view.findViewById(R.id.signName)

        fun bind(sign: RecognizedSign) {
            signName.text = sign.label
            signName.setTextColor(sign.getColor())
        }
    }
}