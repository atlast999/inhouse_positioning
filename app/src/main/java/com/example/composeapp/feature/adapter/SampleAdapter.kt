package com.example.composeapp.feature.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.composeapp.databinding.ItemSampleBinding
import com.example.composeapp.model.PositionSample
import java.text.SimpleDateFormat
import java.util.*

class SampleAdapter : RecyclerView.Adapter<SampleAdapter.ViewHolder>() {

    private val mList: MutableList<PositionSample> = mutableListOf()
    var itemClickListener: (PositionSample) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSampleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mList[position].let { model ->
            holder.binding.root.setOnClickListener {
                itemClickListener.invoke(model)
            }
            holder.bind(model)
        }

    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun getData(): List<PositionSample> = mList

    @SuppressLint("NotifyDataSetChanged")
    fun applyNewData(data: List<PositionSample>) {
        mList.clear()
        notifyItemRangeRemoved(0, itemCount)
        mList.addAll(data)
        notifyItemRangeChanged(0, data.size)
    }

    class ViewHolder(val binding: ItemSampleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: PositionSample) {
            binding.apply {
                tvSampleName.text = model.name
                tvSampleDate.text = model.uid.toTimeString()
                executePendingBindings()
            }
        }
    }
}

fun Long.toTimeString(pattern: String = "yyyy/MM/dd HH:mm:ss"): String {
    return SimpleDateFormat(pattern, Locale.getDefault()).format(this)
}