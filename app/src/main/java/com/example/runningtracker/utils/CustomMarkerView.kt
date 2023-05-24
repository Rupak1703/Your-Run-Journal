package com.example.runningtracker.utils

import android.content.Context
import android.icu.util.Calendar
import android.os.Parcel
import android.os.Parcelable
import android.text.Html
import android.view.View
import android.widget.TextView
import com.example.runningtracker.R
import com.example.runningtracker.databinding.MarkerViewBinding
import com.example.runningtracker.db.Run
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

class CustomMarkerView(
    private val runs : List<Run>,
    context: Context,
    layoutId: Int,
    private val binding: MarkerViewBinding
): MarkerView(context , layoutId){

    private val mvDate = findViewById<TextView>(R.id.mv_tvDate)
    private val mvDuration = findViewById<TextView>(R.id.mv_tvDuration)
    private val mvAvgSpeed = findViewById<TextView>(R.id.mv_tvAvgSpeed)
    private val mvDistance = findViewById<TextView>(R.id.mv_tvDistance)
    private val mvCaloriesBurned = findViewById<TextView>(R.id.mv_tvCaloriesBurned)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {

        super.refreshContent(e, highlight)

        if (e == null){
            return
        }


        val curRunId = e.x.toInt() // because the x val of our BarEntry is our indices of our run
        val run = runs[curRunId]

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timeStamp /// basically a date in milliseconds
        }
        val dateFormat = SimpleDateFormat("dd.MM.yy" , Locale.getDefault())

        mvDate.text = Html.fromHtml("<b>Date:</b> ${dateFormat.format(calendar.time)}")

        val avgSpeed = "${run.avgSpeedInKMPH}km/h"
        mvAvgSpeed.text = Html.fromHtml("<b>Avg speed:</b> ${avgSpeed}")

        val distanceInKM = "${run.distanceInMeters / 1000f}km"
        mvDistance.text = Html.fromHtml("<b>Distance:</b> ${distanceInKM}")

        mvDuration.text = Html.fromHtml("<b>Duration:</b> ${TrackingUtility.getFormattedStopWatchTime(run.timeInMilliSeconds)}")

        val caloriesBurned = "${run.calorieBurned}kcal"
        mvCaloriesBurned.text = Html.fromHtml("<b>Calories:</b> $caloriesBurned")
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-width/2f , -height.toFloat())
    }

}