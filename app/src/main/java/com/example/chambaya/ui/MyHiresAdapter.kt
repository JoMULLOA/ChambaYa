package com.example.chambaya.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chambaya.databinding.ItemHireBinding
import com.example.chambaya.model.ContratoInfo

class MyHiresAdapter : ListAdapter<ContratoInfo, MyHiresAdapter.HireViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HireViewHolder {
        val binding = ItemHireBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HireViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HireViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    class HireViewHolder(private val binding: ItemHireBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hire: ContratoInfo) {
            binding.apply {
                tvJobTitle.text = hire.job.title
                tvProvider.text = "Ofrecido por: ${hire.job.providerName}"
                tvPrice.text = hire.job.price
                tvStatus.text = hire.contrato.estado
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ContratoInfo>() {
        override fun areItemsTheSame(oldItem: ContratoInfo, newItem: ContratoInfo) =
            oldItem.contrato.id == newItem.contrato.id

        override fun areContentsTheSame(oldItem: ContratoInfo, newItem: ContratoInfo) =
            oldItem == newItem
    }
}
