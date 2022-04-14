package com.example.funrun.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.funrun.database.Run
import com.example.funrun.databinding.ItemRunBinding
import com.example.funrun.utils.getFormattedStopWatchTime
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter() : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val binding = ItemRunBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RunViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.binding.ivRunImage.apply {
            Glide.with(this).load(item.image).into(this)
        }
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    inner class RunViewHolder(val binding: ItemRunBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(run: Run) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(calendar.time)
            binding.tvAvgSpeed.text = "${run.avgSpeedInKMH}km/h"
            binding.tvDistance.text = "${run.distanceInMeters / 1000f}km"
            binding.tvTime.text = getFormattedStopWatchTime(run.timeInMillis)
            binding.tvCalories.text = "${run.caloriesBurned}kcal"
        }
    }

}