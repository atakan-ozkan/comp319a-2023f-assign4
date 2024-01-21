package com.example.myphonebookapp.viewModel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myphonebookapp.model.AvatarModel
import com.example.myphonebookapp.client.RetrofitClient
import com.example.myphonebookapp.repository.ContactRepository
import com.example.myphonebookapp.model.ContactModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class ContactViewModel(private val repository: ContactRepository) : ViewModel() {

    private val _contacts = MutableLiveData<List<ContactModel>>()
    val contacts: LiveData<List<ContactModel>> = _contacts
    private val avatarCache = hashMapOf<Int, MutableLiveData<Bitmap?>>()
    private val _previewAvatar = MutableLiveData<AvatarModel?>()
    val previewAvatar: LiveData<AvatarModel?> = _previewAvatar
    private var isInitialLoad = true


    fun getContactById(contactId: Int): LiveData<ContactModel>{
        val liveData = MutableLiveData<ContactModel>()
        viewModelScope.launch {
            liveData.value = repository.getContactById(contactId)
        }
        return liveData
    }

    fun loadContacts(): LiveData<List<ContactModel>> {
        viewModelScope.launch {
            val contactsList = repository.getAllContacts()
            _contacts.value = contactsList
            if (isInitialLoad) {
                preloadContactImages(contactsList)
                isInitialLoad = false
            }
        }
        return contacts
    }

    fun addContact(contact: ContactModel) {
        viewModelScope.launch {
            repository.insertContact(contact)
            loadContacts()
        }
    }
    fun updateContact(contact: ContactModel) {
        viewModelScope.launch {
            repository.updateContact(contact)
            loadContacts()
        }
    }
    fun deleteContact(contact: ContactModel) {
        viewModelScope.launch {
            repository.deleteContact(contact)
            loadContacts()
        }
    }
    fun deleteAllContacts() {
        viewModelScope.launch {
            repository.deleteAllContacts()
            _contacts.value = emptyList()
        }
    }

    fun searchContactsByFullName(searchString: String) {
        viewModelScope.launch {
            val results = repository.searchContactsByFullName("%$searchString%")
            _contacts.postValue(results)
        }
    }

    private fun preloadContactImages(contacts: List<ContactModel>) {
        contacts.forEach { contact ->
            if (!avatarCache.containsKey(contact.id)) {
                fetchAvatar(contact.id, contact.avatarKey)
            }
        }
    }

    fun fetchAvatar(contactId: Int, avatarKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.service.getAvatar(avatarKey).execute()
                if (response.isSuccessful) {
                    response.body()?.byteStream()?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        synchronized(avatarCache) {
                            avatarCache.getOrPut(contactId) { MutableLiveData() }.postValue(bitmap)
                        }
                    }
                } else {
                    synchronized(avatarCache) {
                        avatarCache.getOrPut(contactId) { MutableLiveData() }.postValue(null)
                    }
                }
            } catch (e: Exception) {
                synchronized(avatarCache) {
                    avatarCache.getOrPut(contactId) { MutableLiveData() }.postValue(null)
                }
            }
        }
    }

    fun getAvatarImageLiveData(contactId: Int): LiveData<Bitmap?> {
        return synchronized(avatarCache) {
            avatarCache.getOrPut(contactId) { MutableLiveData() }
        }
    }

    fun fetchRandomAvatarForPreview(): AvatarModel {
        val randomKey = UUID.randomUUID().toString()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.service.getAvatar(randomKey).execute()
                if (response.isSuccessful) {
                    response.body()?.byteStream()?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        _previewAvatar.postValue(AvatarModel(randomKey, bitmap))
                    }
                } else {
                    _previewAvatar.postValue(AvatarModel(randomKey, null))
                }
            } catch (e: Exception) {
                _previewAvatar.postValue(AvatarModel(randomKey, null))
            }
        }

        return AvatarModel(randomKey, null)
    }
}