package com.example.composeapp.feature.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.composeapp.databinding.ItemApBinding
import com.example.composeapp.model.AccessPoint

class ScannedAdapter : RecyclerView.Adapter<ScannedAdapter.ViewHolder>() {

    private val mList: MutableList<AccessPoint> = mutableListOf()
    var itemClickListener: (AccessPoint) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemApBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    fun getData(): List<AccessPoint> = mList

    @SuppressLint("NotifyDataSetChanged")
    fun applyNewData(data: List<AccessPoint>) {
        mList.clear()
        mList.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemApBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: AccessPoint) {
            binding.apply {
                tvName.text = model.name
                tvMac.text = model.uid
                tvRssi.text = model.rssi.toString()
                tvRo.visibility = View.INVISIBLE
                tvPro.visibility = View.INVISIBLE
                textRo.visibility = View.INVISIBLE
                textPro.visibility = View.INVISIBLE
                executePendingBindings()
            }
        }
    }
}