package com.adam.roy.model.timer.ui

import android.os.Build
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.adam.roy.data.localDatabase.entities.AccelerationRunEntry

import com.adam.roy.databinding.FragmentTop10RunsBinding
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MyRunsRecyclerViewAdapter(
    private val deleteClick: (AccelerationRunEntry) -> Unit,
    private val values: List<AccelerationRunEntry>
) : RecyclerView.Adapter<MyRunsRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = FragmentTop10RunsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val run = values[position]
        val date = convertUnixTime(run.dateRan)

        holder.binding.runInfo.text = "Date: " + date + "\nVehicle: " + run.vehicleUsed +
                "\nTarget Speed: " + run.targetSpeed + " MPH\nTime: " + run.completionTime

        holder.binding.deleteTrashcanLogo.setOnClickListener {
            deleteClick(run)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(val binding: FragmentTop10RunsBinding) :
        RecyclerView.ViewHolder(binding.root)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertUnixTime(time: Long) : String
    {
        val instant = Instant.ofEpochMilli(time)

        val dateTime = instant.atZone(ZoneId.systemDefault())

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDate = dateTime.format(formatter)

        return formattedDate
    }
