package com.example.runningtracker.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runningtracker.db.Run
import com.example.runningtracker.repositories.MainRepository
import com.example.runningtracker.utils.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mainRepository: MainRepository
) : ViewModel() {

    private val runSortedByDate = mainRepository.getAllRunSortedByDate()
    private val runSortedByDistance = mainRepository.getAllRunSortedByDistance()
    private val runSortedByCaloriesBurned = mainRepository.getAllRunSortedByCaloriesBurned()
    private val runSortedByTimeInMillis = mainRepository.getAllRunSortedByTimeInMillis()
    private val runSortedByAvgSpeed = mainRepository.getAllRunSortedByAvgSpeed()


    val runs = MediatorLiveData<List<Run>>()

    var sortType = SortType.DATE

    init {
        /// merging the livedata
        runs.addSource(runSortedByDate) { result -> /// this lambda (observer) function is called everytime when there is change in 'runSortedByDate' livedata
            if (sortType == SortType.DATE) {
                result?.let {
                    runs.value = it // it -> value of "runSortedByDate" livedata

                    Timber.d("running runs.addsource for DATE")
                }
            }
        }
        runs.addSource(runSortedByDistance) { result ->
                if (sortType == SortType.DISTANCE) {
                    result?.let {
                        runs.value = it
                    }

                    Timber.d("running runs.addsource for DISTANCE")
                }
        }
        runs.addSource(runSortedByAvgSpeed) { result ->
                if (sortType == SortType.AVG_SPEED) {
                    result?.let {
                        runs.value = it
                    }

                    Timber.d("running runs.addsource for AVG_SPEED")
                }
        }
        runs.addSource(runSortedByCaloriesBurned) { result ->
                if (sortType == SortType.CALORIES_BURNED) {
                    result?.let {
                        runs.value = it
                    }

                    Timber.d("running runs.addsource for CALORIE_BURNED")
                }
        }
        runs.addSource(runSortedByTimeInMillis) { result ->
                if (sortType == SortType.RUNNING_TIME) {
                    result?.let {
                        runs.value = it
                    }


                    Timber.d("running runs.addsource for RUNNING_TIME")
                }
        }


    }

    fun sortRuns(sortType: SortType) = when (sortType) {
        SortType.DATE -> runSortedByDate.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runSortedByTimeInMillis.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runSortedByAvgSpeed.value?.let { runs.value = it }
        SortType.DISTANCE -> runSortedByDistance.value?.let { runs.value = it }
        SortType.CALORIES_BURNED -> runSortedByCaloriesBurned.value?.let { runs.value = it }
    }.also {
        this.sortType = sortType
    }


    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }



}