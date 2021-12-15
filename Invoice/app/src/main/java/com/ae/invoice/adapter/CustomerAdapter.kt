package com.ae.invoice.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.ae.invoice.R
import com.ae.invoice.databinding.CustomerModelBinding
import com.ae.invoice.model.Customer

class CustomerAdapter(
    private var customerList: List<Customer>,
    private val listener: OnClickListener
) : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    class CustomerViewHolder(
        private val binding: CustomerModelBinding,
        private val listener: OnClickListener
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(model: Customer) {
            with(binding) {
                root.setOnClickListener {
                    if (adapterPosition != RecyclerView.NO_POSITION)
                        listener.onClick(model, it)
//                    val menu = PopupMenu(context, root)
//                    menu.inflate(R.menu.recycler_options)
//                    menu.setOnMenuItemClickListener { item ->
//                        when (item.itemId) {
//                            R.id.edit_customer -> {
//                                listener.editCustomer(model)
//                                true
//                            }
//                            R.id.generate_invoice -> {
//                                listener.generateInvoice(model)
//                                true
//                            }
//                            R.id.delete_customer -> {
//                                listener.deleteCustomer(model)
//                                true
//                            }
//                            else -> {
//                                false
//                            }
//                        }
//                    }
//                    menu.show()
                }

                customerName.text = model.name
                servicesAvailedCount.text = "Services availed: ${model.servicesAvailed?.size}"
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newList: List<Customer>) {
        customerList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding =
            CustomerModelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(customerList[position])
    }

    override fun getItemCount(): Int {
        return customerList.size
    }

    interface OnClickListener {
        fun onClick(customer: Customer, view: View)
//        fun editCustomer(customer: Customer)
//        fun generateInvoice(customer: Customer)
//        fun deleteCustomer(customer: Customer)
    }
}