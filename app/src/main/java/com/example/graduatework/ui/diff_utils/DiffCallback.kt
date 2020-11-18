package com.example.graduatework.ui.diff_utils

import androidx.recyclerview.widget.DiffUtil
import com.example.graduatework.tf.RecognizedSign

class DiffCallback(
    private val new: List<RecognizedSign>,
    private val old: List<RecognizedSign>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return old.size
    }

    override fun getNewListSize(): Int {
        return new.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return true
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return false
    }
}