package com.example.mykasir.feature_collaborator.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.unit.sp
import com.example.mykasir.feature_collaborator.model.Collaborator
import com.example.mykasir.feature_collaborator.viewmodel.AddCollaboratorState
import com.example.mykasir.feature_collaborator.viewmodel.CollaboratorUiState
import com.example.mykasir.feature_collaborator.viewmodel.CollaboratorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollaboratorScreen(
    viewModel: CollaboratorViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val addState by viewModel.addState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Collaborator?>(null) }
    var editTarget by remember { mutableStateOf<Collaborator?>(null) }
    val editState by viewModel.editState.collectAsState()
    
    // Handle add success
    LaunchedEffect(addState) {
        if (addState is AddCollaboratorState.Success) {
            showAddDialog = false
            viewModel.resetAddState()
        }
    }

    // Handle edit success
    LaunchedEffect(editState) {
        if (editState is com.example.mykasir.feature_collaborator.viewmodel.EditCollaboratorState.Success) {
            editTarget = null
            viewModel.resetEditState()
        }
    }
    
    // Delete confirmation dialog
    deleteTarget?.let { collaborator ->
        com.example.mykasir.core_ui.ConfirmationDialog(
            title = "Hapus Kasir",
            message = "Apakah Anda yakin ingin menghapus ${collaborator.name}?",
            confirmText = "Hapus",
            dismissText = "Batal",
            type = com.example.mykasir.core_ui.DialogType.Delete,
            onConfirm = {
                viewModel.deleteCollaborator(collaborator.id)
                deleteTarget = null
            },
            onDismiss = { deleteTarget = null }
        )
    }

    // Edit dialog
    editTarget?.let { collaborator ->
        EditCollaboratorDialog(
            collaborator = collaborator,
            editState = editState,
            onDismiss = { 
                editTarget = null
                viewModel.resetEditState()
            },
            onSave = { name, password ->
                viewModel.updateCollaborator(collaborator.id.toLong(), name, password)
            }
        )
    }
    
    // Add dialog
    if (showAddDialog) {
        AddCollaboratorDialog(
            addState = addState,
            onDismiss = { 
                showAddDialog = false
                viewModel.resetAddState()
            },
            onAdd = { email, password, name ->
                viewModel.addCollaborator(email, password, name)
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Kasir", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah Kasir")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is CollaboratorUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is CollaboratorUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadCollaborators() }) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }
                is CollaboratorUiState.Success -> {
                    if (state.collaborators.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Belum Ada Kasir",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tambahkan kasir untuk membantu mengelola toko Anda",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "${state.collaborators.size} Kasir Terdaftar",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            
                            items(state.collaborators) { collaborator ->
                                CollaboratorCard(
                                    collaborator = collaborator,
                                    onEdit = { editTarget = collaborator },
                                    onDelete = { deleteTarget = collaborator }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollaboratorCard(
    collaborator: Collaborator,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F7)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = collaborator.name.take(2).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = collaborator.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = collaborator.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = "Kasir",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Edit button
            IconButton(
                onClick = onEdit,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = Color(0xFF1E88E5)
                )
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Edit")
            }
            
            // Delete button
            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Hapus")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCollaboratorDialog(
    addState: AddCollaboratorState,
    onDismiss: () -> Unit,
    onAdd: (email: String, password: String, name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = androidx.compose.ui.graphics.Color(0xFF1E88E5).copy(alpha = 0.12f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = androidx.compose.ui.graphics.Color(0xFF1E88E5)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            "Tambah Kasir Baru",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.ui.graphics.Color(0xFF1A1A2E)
                        )
                        Text(
                            "Isi data kasir di bawah ini",
                            fontSize = 13.sp,
                            color = androidx.compose.ui.graphics.Color(0xFF888888)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Input fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap") },
                    leadingIcon = { Icon(Icons.Filled.Person, null, tint = androidx.compose.ui.graphics.Color(0xFF1E88E5)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF1E88E5),
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
                        focusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF8F9FA),
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF8F9FA)
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Filled.Email, null, tint = androidx.compose.ui.graphics.Color(0xFF1E88E5)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF1E88E5),
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
                        focusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF8F9FA),
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF8F9FA)
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null, tint = androidx.compose.ui.graphics.Color(0xFF1E88E5)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color(0xFF1E88E5),
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color(0xFFE0E0E0),
                        focusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF8F9FA),
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color(0xFFF8F9FA)
                    )
                )
                
                AnimatedVisibility(visible = addState is AddCollaboratorState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (addState as? AddCollaboratorState.Error)?.message ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = androidx.compose.ui.graphics.Color(0xFF666666)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFE0E0E0))
                    ) {
                        Text("Batal", fontWeight = FontWeight.SemiBold)
                    }
                    
                    Button(
                        onClick = { onAdd(email, password, name) },
                        enabled = addState !is AddCollaboratorState.Loading,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFF1E88E5),
                            contentColor = androidx.compose.ui.graphics.Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (addState is AddCollaboratorState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = androidx.compose.ui.graphics.Color.White
                            )
                        } else {
                            Text("Tambah", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCollaboratorDialog(
    collaborator: Collaborator,
    editState: com.example.mykasir.feature_collaborator.viewmodel.EditCollaboratorState,
    onDismiss: () -> Unit,
    onSave: (name: String?, password: String?) -> Unit
) {
    var name by remember { mutableStateOf(collaborator.name) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E88E5).copy(alpha = 0.12f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFF1E88E5)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Title
                Text(
                    text = "Edit Kasir",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A2E)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Email (tidak bisa diubah)
                Text(
                    text = collaborator.email,
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kasir") },
                    leadingIcon = { Icon(Icons.Filled.Person, null, tint = Color(0xFF1E88E5)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1E88E5),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA)
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Password input
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password Baru (Opsional)") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null, tint = Color(0xFF1E88E5)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1E88E5),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA)
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Confirm Password input
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Konfirmasi Password") },
                    leadingIcon = { Icon(Icons.Filled.Lock, null, tint = Color(0xFF1E88E5)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1E88E5),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA)
                    ),
                    enabled = password.isNotEmpty()
                )
                
                // Error message
                AnimatedVisibility(visible = editState is com.example.mykasir.feature_collaborator.viewmodel.EditCollaboratorState.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (editState as? com.example.mykasir.feature_collaborator.viewmodel.EditCollaboratorState.Error)?.message ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // Password mismatch warning
                AnimatedVisibility(visible = password.isNotEmpty() && confirmPassword.isNotEmpty() && password != confirmPassword) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Password tidak cocok",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF666666)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                    ) {
                        Text("Batal", fontWeight = FontWeight.SemiBold)
                    }
                    
                    Button(
                        onClick = {
                            val passwordToSend = if (password.isNotEmpty() && password == confirmPassword) password else null
                            val nameToSend = if (name != collaborator.name) name else null
                            if (nameToSend != null || passwordToSend != null) {
                                onSave(nameToSend, passwordToSend)
                            }
                        },
                        enabled = editState !is com.example.mykasir.feature_collaborator.viewmodel.EditCollaboratorState.Loading &&
                                  (name != collaborator.name || (password.isNotEmpty() && password == confirmPassword)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E88E5),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (editState is com.example.mykasir.feature_collaborator.viewmodel.EditCollaboratorState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Simpan", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
