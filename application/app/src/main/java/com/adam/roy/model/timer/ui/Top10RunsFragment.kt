package com.adam.roy.model.timer.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.adam.roy.R
import com.adam.roy.data.localDatabase.AppDatabase
import com.adam.roy.model.timer.viewModel.RunsViewModel
import kotlinx.coroutines.launch

class Top10RunsFragment : Fragment() {

    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_top10_runs_list, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        // Set the adapter
        recyclerView?.apply {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }

            val database = AppDatabase.Companion.getDatabase(requireContext())
            val dao = database.accelerationRunDao()

            val factory = RunsViewModel.RunsViewModelFactory(dao)
            val viewModel = ViewModelProvider(this@Top10RunsFragment, factory)[RunsViewModel::class.java]

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED)
                {
                    viewModel.allRuns.collect { runs ->
                        adapter = MyRunsRecyclerViewAdapter(
                            values = runs,
                            deleteClick = { run -> viewModel.deleteRun(run) })
                        }
                }
            }
        }
        return view
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
            Top10RunsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}