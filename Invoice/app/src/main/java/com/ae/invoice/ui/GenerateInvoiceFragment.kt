package com.ae.invoice.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ae.invoice.R
import com.ae.invoice.databinding.FragmentGenerateInvoiceBinding

class GenerateInvoiceFragment : Fragment(R.layout.fragment_generate_invoice) {

    private var _binding: FragmentGenerateInvoiceBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenerateInvoiceBinding.inflate(inflater, container, false)

        with(binding) {
            taxesApplicable.setOnCheckedChangeListener { _, isChecked ->
                cgstInputLayout.isEnabled = isChecked
                sgstInputLayout.isEnabled = isChecked
            }
            generateInvoiceBtn.setOnClickListener {
                TODO("To Generate the Invoice")
            }
        }

        return binding.root
    }
}