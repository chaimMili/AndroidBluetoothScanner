package com.chaimmili.bluetoothscanner

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chaimmili.bluetoothscanner.databinding.DeviceCardViewBinding

class AvailableDevicesAdapter :
    RecyclerView.Adapter<AvailableDevicesAdapter.AvailableDevicesViewHolder>() {

    private val devicesList: MutableList<DeviceDetails> = mutableListOf()

    @SuppressLint("NotifyDataSetChanged")
    fun setDevices(devices: List<DeviceDetails>) {
        devicesList.apply {
            clear()
            addAll(devices)
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailableDevicesViewHolder =
        AvailableDevicesViewHolder(
            DeviceCardViewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: AvailableDevicesViewHolder, position: Int) {
        holder.displayDevice(devicesList[position])
    }

    override fun getItemCount(): Int = devicesList.size

    inner class AvailableDevicesViewHolder(
        private val binding: DeviceCardViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun displayDevice(deviceDetails: DeviceDetails) {
            binding.bluetoothName.text = deviceDetails.name
            binding.bluetoothMacAddress.text = deviceDetails.macAddress
        }
    }
}