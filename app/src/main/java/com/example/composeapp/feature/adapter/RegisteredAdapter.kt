package com.example.composeapp.feature.adapter


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.composeapp.databinding.ItemApBinding
import com.example.composeapp.model.ApPositionInfo

class RegisteredAdapter : RecyclerView.Adapter<RegisteredAdapter.ViewHolder>() {

    private val mList: MutableList<ApPositionInfo> = mutableListOf()
    var itemClickListener: (ApPositionInfo) -> Unit = {}

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

    @SuppressLint("NotifyDataSetChanged")
    fun applyNewData(data: List<ApPositionInfo>) {
        mList.clear()
        mList.addAll(data)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemApBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(model: ApPositionInfo) {
            binding.apply {
                tvName.text = model.name
                tvMac.text = model.uid
                tvRo.text = model.ro.toString()
                tvPro.text = model.pro.toString()
                tvRssi.visibility = View.INVISIBLE
                textRssi.visibility = View.INVISIBLE
                executePendingBindings()
            }
        }
    }
}