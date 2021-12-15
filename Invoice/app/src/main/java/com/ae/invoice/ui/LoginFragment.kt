package com.ae.invoice.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ae.invoice.R
import com.ae.invoice.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar

import com.google.firebase.auth.FirebaseAuth

class LoginFragment: Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)


        with (binding) {
            loginBtn.setOnClickListener {
                val user = username.text.toString().trim()
                val pass = password.text.toString().trim()
                verifyAndLogin(user, pass)
            }
        }

        return binding.root
    }

    private fun verifyAndLogin(user: String, pass: String) {
        if (user.isEmpty() || user.isBlank()) {
            showWarning(INVALID_USERNAME)
        }
        else if (pass.isEmpty() || pass.isBlank()) {
            showWarning(INVALID_PASSWORD)
        }
        else {
            auth.signInWithEmailAndPassword(user, pass)
                .addOnSuccessListener { authResult ->
                    android.util.Log.d(TAG, "verifyAndLogin: Login success")
                    // Login Successful
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToCustomerViewFragment()
                    )
                }
                .addOnFailureListener { exception ->
                    android.util.Log.d(TAG, "verifyAndLogin: Login failed, ${exception.message}")
                    showWarning(LOGIN_FAILURE)
                }
        }
    }

    private fun showWarning(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_LONG) }
    }

    companion object {
        private const val TAG = "INV_LoginFragment"
        private const val INVALID_USERNAME = "Invalid Username"
        private const val INVALID_PASSWORD = "Invalid Password"
        private const val LOGIN_FAILURE = "Some error occured. Login Failed."
    }
}