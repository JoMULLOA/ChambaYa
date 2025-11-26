package com.example.chambaya.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chambaya.R
import com.example.chambaya.model.Job
import com.example.chambaya.model.JobType

class JobsAdapter(
    private val onJobClick: (Job) -> Unit
) : ListAdapter<Job, JobsAdapter.JobViewHolder>(JobDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view, onJobClick)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class JobViewHolder(
        itemView: View,
        private val onJobClick: (Job) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvProvider: TextView = itemView.findViewById(R.id.tvProviderName)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val tvType: TextView = itemView.findViewById(R.id.tvJobType)
        private val tvRating: TextView = itemView.findViewById(R.id.tvRating)

        fun bind(job: Job) {
            tvTitle.text = job.title
            tvProvider.text = "${job.providerName} • ${job.distance} km"
            tvPrice.text = job.price
            tvType.text = if (job.type == JobType.OFFER) "Oferta" else "Demanda"

            // Cambiar color de fondo según tipo
            val typeColor = if (job.type == JobType.OFFER) {
                0xFF2196F3.toInt() // Azul
            } else {
                0xFF4CAF50.toInt() // Verde
            }
            tvType.setBackgroundColor(typeColor)

            if (job.rating != null) {
                tvRating.text = "⭐ ${job.rating}"
                tvRating.visibility = View.VISIBLE
            } else {
                tvRating.visibility = View.GONE
            }

            itemView.setOnClickListener {
                onJobClick(job)
            }
        }
    }

    private class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
        override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean {
            return oldItem == newItem
        }
    }
}

