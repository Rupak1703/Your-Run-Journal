package com.example.runningtracker.adapters

import android.icu.util.Calendar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.runningtracker.R
import com.example.runningtracker.databinding.ItemRunBinding
import com.example.runningtracker.db.Run
import com.example.runningtracker.utils.TrackingUtility
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_run,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]

        Glide.with(holder.itemView).load(run.img).into(holder.binding.ivRunImage)

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp /// basically a date in milliseconds
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy" , Locale.getDefault())
        holder.binding.tvDate.text = dateFormat.format(calendar.time)

        val avgSpeed = "${run.avgSpeedInKMPH}km/h"
        holder.binding.tvAvgSpeed.text = avgSpeed

        val distanceInKM = "${run.distanceInMeters / 1000f}km"
        holder.binding.tvDistance.text = distanceInKM

        holder.binding.tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMilliSeconds)

        val caloriesBurned = "${run.calorieBurned}kcal"
        holder.binding.tvCalories.text = caloriesBurned
    }

    private val diffUtilComparator = object : DiffUtil.ItemCallback<Run>(){
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this , diffUtilComparator)

    fun submitList(list: List<Run>) = differ.submitList(list)

    inner class RunViewHolder(itemView: View): ViewHolder(itemView) {
        var binding : ItemRunBinding

        init {
            binding =ItemRunBinding.bind(itemView)
        }
    }

}