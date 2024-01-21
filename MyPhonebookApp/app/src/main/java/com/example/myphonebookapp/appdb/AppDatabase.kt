package com.example.myphonebookapp.appdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.myphonebookapp.dao.ContactDao
import com.example.myphonebookapp.model.ContactModel

@Database(entities = [ContactModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
