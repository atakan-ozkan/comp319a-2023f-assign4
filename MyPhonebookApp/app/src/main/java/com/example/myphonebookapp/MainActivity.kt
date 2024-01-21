package com.example.myphonebookapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.myphonebookapp.appdb.AppDatabase
import com.example.myphonebookapp.contact.ContactRepository
import com.example.myphonebookapp.contact.ContactViewModel
import com.example.myphonebookapp.screen.ContactDetailScreen
import com.example.myphonebookapp.screen.ContactScreen
import com.example.myphonebookapp.ui.theme.MyPhonebookAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var contactViewModel: ContactViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "contacts"
        ).allowMainThreadQueries().build()

        val contactRepository = ContactRepository(db.contactDao())
        contactViewModel = ContactViewModel(contactRepository)
        setContent {
            MyPhonebookAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                )
                {
                    PhoneBookAppNavigation(contactViewModel,this)
                }
            }
        }
    }

    @Composable
    fun PhoneBookAppNavigation(contactsViewModel: ContactViewModel,context: Context) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "contactsList") {
            composable("contactsList") {
                ContactScreen(
                    contactViewModel = contactsViewModel,
                    navController = navController,
                    context = context
                )
            }
            composable(
                "contactDetail/{contactId}",
                arguments = listOf(navArgument("contactId") { type = NavType.IntType })
            ) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getInt("contactId") ?: return@composable
                ContactDetailScreen(contactId, contactViewModel, navController, context)
            }
        }
    }
}