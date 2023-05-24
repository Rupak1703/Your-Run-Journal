package com.example.runningtracker.repositories

import com.example.runningtracker.db.Run
import com.example.runningtracker.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDAO: RunDAO
) {

    suspend fun insertRun(run: Run) = runDAO.insertRun(run)

    suspend fun deleteRun(run: Run) = runDAO.deleteRun(run)

    fun getAllRunSortedByDate() = runDAO.getAllRunsSortedByDates()

    fun getAllRunSortedByDistance() = runDAO.getAllRunsSortedByDistance()

    fun getAllRunSortedByTimeInMillis() = runDAO.getAllRunsSortedByTimeInMillis()

    fun getAllRunSortedByAvgSpeed() = runDAO.getAllRunsSortedByAverageSpeed()

    fun getAllRunSortedByCaloriesBurned() = runDAO.getAllRunsSortedByCaloriesBurned()


    fun getTotalAvgSpeed() = runDAO.getTotalAvgSpeed()

    fun getTotalAvgDistance() = runDAO.getTotalDistance()

    fun getTotalCaloriesBurned() = runDAO.getTotalCalorieBurned()

    fun getTotalTimeInMillis() = runDAO.getTotalTimeInMillis()


}