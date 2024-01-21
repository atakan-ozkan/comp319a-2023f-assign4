package com.example.myphonebookapp.contact

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    fun getContactById(contactId: Int): ContactModel

    @Query("SELECT * FROM contacts")
    fun getAllContacts(): List<ContactModel>

    @Insert
    fun insertContact(contact: ContactModel)

    @Update
    fun updateContact(contact: ContactModel)

    @Delete
    fun deleteContact(contact: ContactModel)

    @Query("DELETE FROM contacts")
    fun deleteAllContacts()

    @Query("""
    SELECT * FROM contacts 
    WHERE (name || ' ' || surname) LIKE :searchQuery
""")
    fun searchContactsByFullName(searchQuery: String): List<ContactModel>

}