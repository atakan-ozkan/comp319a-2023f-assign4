package com.example.myphonebookapp.contact

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val surname: String,
    val phoneNumber: String,
    val email: String?,
    val address: String?,
    val avatarKey: String
)