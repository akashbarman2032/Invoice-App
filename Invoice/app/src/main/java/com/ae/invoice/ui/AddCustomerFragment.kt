package com.ae.invoice.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.ae.invoice.R
import com.ae.invoice.adapter.ServicesAdapter
import com.ae.invoice.databinding.FragmentAddCustomerBinding
import com.ae.invoice.model.Customer
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase

class AddCustomerFragment: Fragment(R.layout.fragment_add_customer) {

    private var _binding: FragmentAddCustomerBinding? = null
    private val binding get() = _binding!!
    private  lateinit var db: FirebaseDatabase
    private var services = mutableSetOf<String>()
    private val args: AddCustomerFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = FirebaseDatabase.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCustomerBinding.inflate(inflater, container, false)

        val serviceAdapter = ServicesAdapter(emptyList())

        if (args.customer != null) {
            val customer = args.customer
            customer!!.servicesAvailed?.let {
                serviceAdapter.submitList(it)
                services.addAll(it)
            }
            binding.apply {
                customerName.setText(customer.name!!)
                customerAddress.setText(customer.address!!)
                country.setText(customer.country)
            }
        }

        with (binding) {
            createCustomerBtn.setOnClickListener {
                val _name = customerName.text.toString().trim()
                val _add = customerAddress.text.toString().trim()
                val _country = country.text.toString().trim()

                makeCustomer(_name, _add, _country)
            }
            servicesAvailed.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = serviceAdapter
            }
            addService.setOnClickListener {
                services.add(availableService.text.toString())
                serviceAdapter.submitList(services.toList())
            }
            country.setAdapter(
                ArrayAdapter(requireContext(), R.layout.dropdown_layout, resources.getStringArray(R.array.countries))
            )
            availableService.setAdapter(
                ArrayAdapter(requireContext(), R.layout.dropdown_layout, resources.getStringArray(R.array.serviceList))
            )
        }

        return binding.root
    }

    private fun makeCustomer(name: String, address: String, country: String) {
        if (name.isEmpty() || name.isBlank()) {
            showWarning(INVALID_NAME)
        }
        else if (address.isEmpty() || address.isBlank()) {
            showWarning(INVALID_ADDRESS)
        }
        else if (country.isEmpty() || country.isBlank()) {
            showWarning(INVALID_COUNTRY)
        }
        else {
            if (args.customer == null) {
                val path = db.getReference(CUSTOMERS_NODE).push()

                val customer = Customer(
                    key = path.key,
                    name = name,
                    address = address,
                    country = country,
                    servicesAvailed = services.toList()
                )
                path.setValue(customer)
            } else {
                val key = args.customer!!.key!!
                val customer = Customer(
                    key = key,
                    name = name,
                    address = address,
                    country = country,
                    servicesAvailed = services.toList()
                )
                db.getReference(CUSTOMERS_NODE).child(key).setValue(customer)
            }

            findNavController().navigate(
                AddCustomerFragmentDirections.actionAddCustomerFragmentToCustomerViewFragment()
            )
        }
    }

    private fun showWarning(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG) }
    }

    companion object {
        private const val INVALID_NAME = "Please enter a name"
        private const val INVALID_ADDRESS = "Please enter your address"
        private const val INVALID_COUNTRY = "Please select a country"

        private const val CUSTOMERS_NODE = "CUSTOMERS"
    }
}