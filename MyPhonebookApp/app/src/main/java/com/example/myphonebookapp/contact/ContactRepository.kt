package com.example.myphonebookapp.contact

import androidx.lifecycle.LiveData

class ContactRepository(private val contactDao: ContactDao) {

    fun getContactById(contactId: Int): ContactModel{
        return contactDao.getContactById(contactId)
    }

    fun getAllContacts(): List<ContactModel> {
        return contactDao.getAllContacts()
    }

    fun insertContact(contact: ContactModel) {
        contactDao.insertContact(contact)
    }

    fun updateContact(contact: ContactModel) {
        contactDao.updateContact(contact)
    }

    fun deleteContact(contact: ContactModel) {
        contactDao.deleteContact(contact)
    }

    fun deleteAllContacts() {
        contactDao.deleteAllContacts()
    }

    fun searchContactsByFullName(searchString: String): List<ContactModel> {
        return contactDao.searchContactsByFullName(searchString)
    }
}
