package com.ae.invoice.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Customer(
    val key: String? = null,
    val name: String? = null,
    val address: String? = null,
    val country: String? = null,
    val servicesAvailed: List<String>? = null
): Parcelable {
    // Null default values creates a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}