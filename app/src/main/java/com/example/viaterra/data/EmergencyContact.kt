package com.example.viaterra.data


data class EmergencyContact(
    val id: Int,
    val name: String,
    val phoneNumber: String,
    var isPriority: Boolean = false
)