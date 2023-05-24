package com.example.runningtracker.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.runningtracker.R
import com.example.runningtracker.databinding.FragmentStatisticsBinding
import com.example.runningtracker.databinding.MarkerViewBinding
import com.example.runningtracker.ui.viewmodels.MainViewModel
import com.example.runningtracker.ui.viewmodels.StatisticsViewModel
import com.example.runningtracker.utils.CustomMarkerView
import com.example.runningtracker.utils.TrackingUtility
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {
    private lateinit var viewModel : StatisticsViewModel

    private lateinit var binding: FragmentStatisticsBinding

    private lateinit var bindingMarkerView : MarkerViewBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStatisticsBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(StatisticsViewModel::class.java)

        bindingMarkerView = MarkerViewBinding.inflate(layoutInflater )


        subscribeToObservers()
        setupBarChart()

        return binding.root
    }


    private fun subscribeToObservers(){
        viewModel.totalTimeRun.observe(viewLifecycleOwner , Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                binding.tvTotalTime.text = totalTimeRun
            }
        })

        viewModel.totalDistance.observe(viewLifecycleOwner , Observer {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10f
                val totalDistanceString = "${totalDistance}km"

                binding.tvTotalDistance.text = totalDistanceString
            }
        })

        viewModel.totalAvgSpeed.observe(viewLifecycleOwner , Observer {
            it?.let {
                val avgSpeed = round(it * 10f) / 10f
                val avgSpeedString = "${avgSpeed}km/h"
                binding.tvAverageSpeed.text = avgSpeedString
            }
        })

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner , Observer {
            it?.let {
                val totalCalories = "${it}kcal"
                binding.tvTotalCalories.text = totalCalories

            }
        })

        viewModel.runsSortedByDate.observe(viewLifecycleOwner , Observer {
            it?.let {
                val allAvgSpeed = it.indices.map { i ->
                    BarEntry(i.toFloat() , it[i].avgSpeedInKMPH)
                } //Returns an IntRange of the valid indices for this collection. (0 to run.size - 1)

                val barDataSet = BarDataSet(allAvgSpeed , "Avg Speed Over Time").apply {
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext() , R.color.colorAccent)
                }

                binding.barChart.data = BarData(barDataSet)
                binding.barChart.marker = CustomMarkerView(it.reversed() , requireContext() , R.layout.marker_view , bindingMarkerView)
                binding.barChart.invalidate()
            }
        })
    }


    private fun setupBarChart(){
        binding.barChart.xAxis.apply{
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        binding.barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        binding.barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        binding.barChart.apply {
            description.text = "Avg speed Over Time"
            legend.isEnabled = false
        }
    }

}