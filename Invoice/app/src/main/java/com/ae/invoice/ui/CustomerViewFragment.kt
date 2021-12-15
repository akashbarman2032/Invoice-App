package com.ae.invoice.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ae.invoice.R
import com.ae.invoice.databinding.FragmentCustomerViewBinding
import com.ae.invoice.model.Customer
import com.ae.invoice.adapter.CustomerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.io.File
import com.ae.invoice.MainActivity
import java.io.FileOutputStream
import java.io.IOException


class CustomerViewFragment : Fragment(R.layout.fragment_customer_view) {

    private var _binding: FragmentCustomerViewBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: FirebaseDatabase

    private val contract = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = FirebaseDatabase.getInstance()
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerViewBinding.inflate(inflater, container, false)

        val listener = object : CustomerAdapter.OnClickListener {
            override fun onClick(customer: Customer, view: View) {
                val menu = PopupMenu(requireContext(), view)
                menu.inflate(R.menu.recycler_options)
                menu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.edit_customer -> {
                            findNavController().navigate(
                                CustomerViewFragmentDirections.actionCustomerViewFragmentToAddCustomerFragment(
                                    customer
                                )
                            )
                            true
                        }
                        R.id.generate_invoice -> {
//                            findNavController().navigate(
//                                CustomerViewFragmentDirections.actionCustomerViewFragmentToGenerateInvoiceFragment(
//                                    customer
//                                )
//                            )
                            if (generateInvoice(customer))
                                showMessage(INVOICE_GENERATED)
                            else
                                showMessage(INVOICE_NOT_GENERATED)
                            true
                        }
                        R.id.delete_customer -> {
                            db.getReference(CUSTOMERS_NODE).child(customer.key!!).removeValue()
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }
                menu.show()
            }
        }

        val customerAdapter = CustomerAdapter(emptyList(), listener)

        db.getReference(CUSTOMERS_NODE).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = mutableListOf<Customer>()
                for (child in snapshot.children) {
                    child.getValue(Customer::class.java)?.let { children.add(it) }
                }
                customerAdapter.submitList(children)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d(TAG, "onCancelled: Database event cancelled")
            }

        })

        with(binding) {
            with(customersList) {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(requireContext())
                adapter = customerAdapter
            }
        }

        return binding.root
    }

    private fun generateInvoice(customer: Customer): Boolean {
        permissionsCheck()
        var gstApplicable = false
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Goods and Service Taxes")
            .setMessage("Is GST(Goods and Service Tax) applicable?")
            .setPositiveButton("Yes") { _, _ ->
                gstApplicable = true
            }
            .setNegativeButton("No") { _, _ ->
            }
            .create()
        dialog.show()
        showMessage(GENERATING_INVOICE)
        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(792, 1120, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        val canvas = page.canvas
        val writer = Paint()

        writer.color = ContextCompat.getColor(requireContext(), R.color.black)

        writer.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        writer.textSize = 28f
        writer.textAlign = Paint.Align.CENTER
        canvas.drawText(getString(R.string.app_name), 209f, 80f, writer)

        writer.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        writer.textSize = 20f
        writer.textAlign = Paint.Align.LEFT
        canvas.drawText("Name: ${customer.name!!}", 209f, 100f, writer)
        canvas.drawText("Address: ${customer.address!!}", 209f, 120f, writer)
        canvas.drawText("Country: ${customer.country!!}", 209f, 140f, writer)


        writer.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "---------------------------------------------------------------------------------",
            0f,
            140f,
            writer
        )

        writer.textAlign = Paint.Align.LEFT

        val services = resources.getStringArray(R.array.serviceList)
        val rates = resources.getStringArray(R.array.rates)
        val rateChart = mutableMapOf<String, Double>()
        for (i in services.indices) {
            rateChart[services[i]] = rates[i].toDouble()
        }

        var yOffset = 0
        var bill = 0.0
        for (data in customer.servicesAvailed!!) {
            canvas.drawText(
                "$data\t\t${rateChart[data].toString()}",
                100f,
                yOffset * 20 + 160f,
                writer
            )
            bill += rateChart[data]!!
            yOffset += 1
        }

        writer.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "---------------------------------------------------------------------------------",
            0f,
            140f,
            writer
        )

        canvas.drawText("Total: $bill", 209f, (yOffset + 2) * 20 + 160f, writer)

        var tax = 0.0
        if (gstApplicable) {
            // Assuming 18% gst (cgst+sgst) for all services
            canvas.drawText(
                "Tax: ${bill * .18} + ${bill * .18}",
                209f,
                (yOffset + 3) * 20 + 160f,
                writer
            )
            tax += 2 * bill * .18
        }
        canvas.drawText("Grand Total: ${bill + tax}", 209f, (yOffset + 2) * 20 + 160f, writer)

        pdfDoc.finishPage(page)

        val file = File(Environment.DIRECTORY_DOWNLOADS, "${customer.name}_INVOICE")
        try {
            pdfDoc.writeTo(FileOutputStream(file))
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        pdfDoc.close()
        return true
    }

    private fun permissionsCheck() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            contract.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            contract.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.customers_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.newCustomer -> {
                findNavController().navigate(
                    CustomerViewFragmentDirections.actionCustomerViewFragmentToAddCustomerFragment(
                        null
                    )
                )
                true
            }
            R.id.logoutBtn -> {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(
                    CustomerViewFragmentDirections.actionCustomerViewFragmentToLoginFragment()
                )
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    companion object {
        private const val TAG = "INV_CustomersFragment"
        private const val CUSTOMERS_NODE = "CUSTOMERS"
        private const val GENERATING_INVOICE = "Generating Invoice...Please be patient"
        private const val INVOICE_GENERATED = "Invoice generated succesfully"
        private const val INVOICE_NOT_GENERATED = "Invoice generated succesfully"
    }
}