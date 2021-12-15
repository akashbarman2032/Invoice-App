package com.ae.invoice.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ae.invoice.databinding.SimpleServiceViewBinding

class ServicesAdapter(private var services: List<String>): RecyclerView.Adapter<ServicesAdapter.ServicesViewHolder>() {
    class ServicesViewHolder(private val binding: SimpleServiceViewBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(name: String) {
            binding.serviceName.text = name
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServicesViewHolder {
        val binding = SimpleServiceViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ServicesViewHolder(binding)
    }
    override fun onBindViewHolder(holder: ServicesViewHolder, position: Int) {
        holder.bind(services[position])
    }
    override fun getItemCount(): Int {
        return services.size
    }
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<String>) {
        services = newList
        notifyDataSetChanged()
    }
}