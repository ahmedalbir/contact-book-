package com.example.contactbook

data class Contact(
    var id: Long,
    var name: String,
    var phone: String,
    var email: String = "",
    var note: String = ""
)
