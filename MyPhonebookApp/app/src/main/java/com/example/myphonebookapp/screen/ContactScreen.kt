package com.example.myphonebookapp.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.example.myphonebookapp.model.ContactModel
import com.example.myphonebookapp.viewModel.ContactViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(contactViewModel: ContactViewModel, navController: NavController, context: Context) {
    var showAddContactDialog by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }
    var agreeAlertDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contactViewModel.loadContacts()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("My Contacts") },
                actions = {

                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Menu")
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Delete All Contacts") },
                                onClick = {
                                    showMenu = false
                                    agreeAlertDialog = true
                                })
                        }
                    }
                }

            )
            SearchBar(
                searchText = searchText,
                onSearchTextChanged = { searchText = it },
                onSearchButtonClicked = { contactViewModel.searchContactsByFullName(searchText) },
                onResetFilters = {
                    searchText = ""
                    contactViewModel.loadContacts()
                }
            )
            ContactList(context, contactViewModel.contacts, contactViewModel, navController)
        }
        Column(modifier = Modifier.fillMaxSize()) {
            ContactFloatingButton {
                showAddContactDialog = true
            }
        }
        if(agreeAlertDialog){
            ShowAgreeAlert(
                onDismiss = {agreeAlertDialog = false},
                onClickAgree = {
                    agreeAlertDialog = false
                    contactViewModel.deleteAllContacts()
                    contactViewModel.loadContacts()
                }

            )
        }
    }
    if (showAddContactDialog) {
        ShowContactDialog(
            contactViewModel= contactViewModel,
            onDismiss = { showAddContactDialog = false },
            onContactAdded = { contact ->
                contactViewModel.addContact(contact)
                showAddContactDialog = false
                contactViewModel.loadContacts()
            }
        )
    }
}

@Composable
fun ContactList(context: Context, contacts: LiveData<List<ContactModel>>, contactViewModel: ContactViewModel, navController: NavController) {
    val contactList by contacts.observeAsState(initial = emptyList())
    var expandedContactId by remember { mutableStateOf<Int?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .padding(8.dp),
        contentPadding = PaddingValues(8.dp),
        content = {
            items(contactList.size) { index ->
                ContactItem(
                    context=context,
                    contact = contactList[index],
                    contactViewModel= contactViewModel,
                    isDropdownVisible = expandedContactId == contactList[index].id,
                    onCardClick = { expandedContactId = if (expandedContactId == contactList[index].id) null else contactList[index].id },
                    navController = navController
                )
            }
        }
    )
}
@Composable
fun ContactItem(
    context: Context,
    contact: ContactModel,
    contactViewModel: ContactViewModel,
    isDropdownVisible: Boolean,
    onCardClick: () -> Unit,
    navController: NavController
) {
    LaunchedEffect(contact.id) {
        contactViewModel.fetchAvatar(contact.id, contact.avatarKey)
    }

    val avatarBitmap by contactViewModel.getAvatarImageLiveData(contact.id).observeAsState()


    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .clickable(onClick = onCardClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            avatarBitmap?.let { bitmap ->
                Image(bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Contact Image",
                    modifier = Modifier.size(64.dp).clip(CircleShape).align(Alignment.CenterHorizontally)
                    )
            } ?: CircularProgressIndicator()

            Spacer(Modifier.height(5.dp))
            Text(
                text = "${contact.name} ${contact.surname}",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            AnimatedVisibility(visible = isDropdownVisible) {
                DropdownPanel(context,contact, navController)
            }
        }
    }
}

@Composable
fun DropdownPanel(context: Context, contact: ContactModel, navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.navigate("contactDetail/${contact.id}")  }) {
                Icon(Icons.Default.Info, contentDescription = "Info")
            }
            IconButton(onClick = { makePhoneCall(context, contact.phoneNumber) }) {
                Icon(Icons.Default.Phone, contentDescription = "Call")
            }
            IconButton(onClick = { sendMessage(context, contact.phoneNumber) }) {
                Icon(Icons.Default.Email, contentDescription = "Message")
            }

        }

    }
}

fun makePhoneCall(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phoneNumber")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("INTENT ERROR","Error occurred when opening dial!")
    }
}

fun sendMessage(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("INTENT ERROR","Error occurred when opening message!")
    }
}

@Composable
fun ContactFloatingButton(onContactAdded: () -> Unit) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(10.dp)
        .zIndex(1f), contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = {onContactAdded() },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Note")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowContactDialog(
    contactViewModel: ContactViewModel,
    onDismiss: () -> Unit,
    onContactAdded: (ContactModel) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    val isNameValid = remember(name) { isValidNameOrSurname(name) }
    val isSurnameValid = remember(surname) { isValidNameOrSurname(surname) }
    val isPhoneNumberValid = remember(phoneNumber) { isValidPhoneNumber(phoneNumber) }
    val isRequiredFieldsValid = remember(isNameValid,isSurnameValid,isPhoneNumberValid)
    { isNameValid && isSurnameValid && isPhoneNumberValid}
    var avatarKey by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        if(contactViewModel.previewAvatar.value == null)contactViewModel.fetchRandomAvatarForPreview()
    }
    val avatarModel by contactViewModel.previewAvatar.observeAsState()
    LaunchedEffect(avatarModel) {
        avatarKey = avatarModel?.avatarKey ?: ""
    }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Add New Contact") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            )  {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    avatarModel?.bitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    } ?: CircularProgressIndicator()
                }
                IconButton(onClick = { contactViewModel.fetchRandomAvatarForPreview() }) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Change Avatar")
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    isError = name.isNotEmpty() && !isNameValid
                )
                OutlinedTextField(
                    value = surname,
                    onValueChange = { surname = it },
                    label = { Text("Surname") },
                    isError = surname.isNotEmpty() && !isSurnameValid
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone number") },
                    isError = phoneNumber.isNotEmpty() && !isPhoneNumberValid
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
                    Text("Contact name must be entered!", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                }
                if (!isSurnameValid) {
                    Text("Contact surname must be entered!", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                }
                if (!isPhoneNumberValid) {
                    Text("Contact phone number must be valid!",
                        color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onContactAdded(
                        ContactModel(
                            name= name,
                            surname = surname,
                            phoneNumber = phoneNumber,
                            email = email,
                            address = address,
                            avatarKey = avatarKey
                        )
                    )
                    onDismiss()
                },
                enabled = isRequiredFieldsValid
            ) {
                Text("Create Contact")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    searchText: String,
    onSearchTextChanged: (String) -> Unit,
    onSearchButtonClicked: () -> Unit,
    onResetFilters: () -> Unit,
) {
    Column {
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = { Text("Search Contact") },
            trailingIcon = {
                IconButton(
                    onClick = { onSearchButtonClicked() },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            }
        )
        Button(
            onClick = onResetFilters,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Reset")
        }
    }
}

@Composable
fun ShowAgreeAlert(
    onDismiss: () -> Unit,
    onClickAgree: () -> Unit){

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Are you sure") },
        text = {
            Text(text = "You are deleting all contacts, are you sure?")
        },
        confirmButton = {
            Button(
                onClick = {
                    onClickAgree()
                    onDismiss()
                }
            ) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("No")
            }
        }
    )
}

fun isValidNameOrSurname(input: String): Boolean {
    val pattern = Regex("^[A-Za-zÀ-ÖØ-öø-ÿ'-]+(?:\\s[A-Za-zÀ-ÖØ-öø-ÿ'-]+)*$")
    return pattern.matches(input)
}

fun isValidPhoneNumber(input: String): Boolean {
    val pattern = Regex("^\\+?\\d{1,3}?[- .]?\\(?(\\d{1,3})\\)?[- .]?\\d{1,4}[- .]?\\d{1,4}[- .]?\\d{1,9}$")
    return pattern.matches(input)
}
