package com.example.myphonebookapp.screen

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myphonebookapp.R
import com.example.myphonebookapp.viewModel.ContactViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(contactId: Int, contactViewModel: ContactViewModel, navController: NavController, context: Context) {
    val contact by contactViewModel.getContactById(contactId).observeAsState()
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var surname by remember { mutableStateOf(contact?.surname ?: "") }
    var phoneNumber by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(contact?.email ?: "") }
    var address by remember { mutableStateOf(contact?.address ?: "") }
    var avatarKey by remember { mutableStateOf(contact?.avatarKey ?: "") }
    val isNameValid = remember(name) { isValidNameOrSurname(name) }
    val isSurnameValid = remember(surname) { isValidNameOrSurname(surname) }
    val isPhoneNumberValid = remember(phoneNumber) { isValidPhoneNumber(phoneNumber) }
    val isRequiredFieldsValid = remember(isNameValid,isSurnameValid,isPhoneNumberValid)
    { isNameValid && isSurnameValid && isPhoneNumberValid}

    LaunchedEffect(contact?.id) {
        contact?.let { contactViewModel.fetchAvatar(it.id, it.avatarKey) }
    }

    val avatarBitmap = contact?.id?.let { id ->
        contactViewModel.getAvatarImageLiveData(id).observeAsState().value
    }

    Column {
        TopAppBar(
            title = { Text("Contact Detail") },
            navigationIcon = {
                IconButton(onClick = { navController.navigate("contactsList") }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = {
                    if (isRequiredFieldsValid){
                        val updatedContact = contact?.let {
                            contact!!.copy(
                                name= name,
                                surname = surname,
                                phoneNumber = phoneNumber,
                                email = email,
                                address = address
                            )
                        }
                        if (updatedContact != null) {
                            contactViewModel.updateContact(updatedContact)
                            Toast.makeText(context, "Contact is updated!", Toast.LENGTH_SHORT).show()
                        }
                        navController.popBackStack()
                    }
                }) {
                    Icon(painterResource(R.drawable.save_icon), contentDescription = "Save")
                }
                IconButton(onClick = {
                    contact?.let { contactViewModel.deleteContact(it) }
                    Toast.makeText(context, "Contact is deleted!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }) {
                    Icon(painterResource(R.drawable.delete_icon), contentDescription = "Delete")
                }
            }
        )
        Column(
            modifier = Modifier.align(CenterHorizontally).padding(10.dp)

        ) {
            avatarBitmap?.let { bitmap ->
                Image(bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Contact Image",
                    modifier = Modifier.size(128.dp).clip(CircleShape).align(Alignment.CenterHorizontally)
                )
            } ?: Text("No Image Available")

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                isError = name.isEmpty() || name == ""
            )
            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it },
                label = { Text("Surname") },
                isError = surname.isEmpty() || surname == ""
            )
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone number") },
                isError = phoneNumber.isEmpty() || phoneNumber == ""
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
            )
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
            )
            if (!isNameValid) {
                Text("Contact name must be entered in order to save!", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
            }
            if (!isSurnameValid) {
                Text("Contact surname must be entered in order to save!", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
            }
            if (!isPhoneNumberValid) {
                Text("Valid Contact phone number entered in order to save!",
                    color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
            }

        }
    }
}
