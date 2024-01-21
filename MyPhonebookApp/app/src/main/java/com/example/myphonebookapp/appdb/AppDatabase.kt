package com.example.myphonebookapp.appdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myphonebookapp.contact.ContactDao
import com.example.myphonebookapp.contact.ContactModel

@Database(entities = [ContactModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
