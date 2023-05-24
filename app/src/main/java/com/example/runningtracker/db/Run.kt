package com.example.runningtracker.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_table")
data class Run(
    var img: Bitmap? = null,
    var timeStamp: Long = 0L, /* when our run was */
    var avgSpeedInKMPH: Float = 0F,
    var distanceInMeters: Int = 0,
    var timeInMilliSeconds: Long = 0L, /* how long our run was */
    var calorieBurned: Int = 0
){
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}