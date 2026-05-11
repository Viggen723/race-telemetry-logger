package com.adam.roy.model.timer.viewModel

import androidx.lifecycle.*
import com.adam.roy.data.localDatabase.dao.AccelerationRunDao
import com.adam.roy.data.localDatabase.entities.AccelerationRunEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class RunsViewModel(private val dao: AccelerationRunDao) : ViewModel() {

    val allRuns: Flow<List<AccelerationRunEntry>> = dao.getTop10Runs()

    fun deleteRun(run: AccelerationRunEntry) {
        viewModelScope.launch(Dispatchers.IO){
            dao.deleteRun(run)
        }
    }

    // Factory class to allow the viewModel to not freak out
    class RunsViewModelFactory(private val dao: AccelerationRunDao) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RunsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RunsViewModel(dao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
