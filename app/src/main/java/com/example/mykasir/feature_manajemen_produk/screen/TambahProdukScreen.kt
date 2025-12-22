package com.example.mykasir.feature_manajemen_produk.screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mykasir.core_ui.LocalNotifier
import com.example.mykasir.core_ui.NotificationType
import com.example.mykasir.feature_manajemen_produk.model.Product
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahProdukScreen(
    navController: NavController,
    viewModel: ProductViewModel,
    productId: Long?
) {
    val notifier = LocalNotifier.current
    val isEditMode = productId != null
    val title = if (isEditMode) "Edit Produk" else "Tambah Produk"
    val subtitle = if (isEditMode) "Perbarui informasi produk" else "Tambahkan produk baru ke stok"

    var productToEdit by remember { mutableStateOf<Product?>(null) }

    // State Vars
    var nama by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var stokAwal by remember { mutableStateOf("") }
    var minStok by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(productId) {
        if (isEditMode && productId != null) {
            productToEdit = viewModel.products.find { it.id == productId }
            productToEdit?.let { product ->
                nama = product.name
                kategori = product.category
                harga = product.price.toString()
                stokAwal = product.stock.toString()
                minStok = product.minStock.toString()
                selectedImageUri = product.imageUri?.toUri()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        // Header (Blue Section)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = { navController.popBackStack() },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali"
                    )
                }

                Column(
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Content (White Curved Section)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Upload Foto Section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "Foto Produk",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    UploadFotoSection(
                        selectedImageUri = selectedImageUri,
                        onImageSelected = { uri -> selectedImageUri = uri },
                        size = 120.dp
                    )
                }

                Text(
                    text = "Informasi Produk",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Input Fields
                InputField(value = nama, onValueChange = { nama = it }, label = "Nama Produk")
                InputField(value = kategori, onValueChange = { kategori = it }, label = "Kategori")
                InputField(value = harga, onValueChange = { harga = it }, label = "Harga")
                InputField(value = stokAwal, onValueChange = { stokAwal = it }, label = "Stok Awal")
                InputField(value = minStok, onValueChange = { minStok = it }, label = "Stok Minimum")

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val stokAwalInt = stokAwal.toIntOrNull() ?: 0
                        val minStokInt = minStok.toIntOrNull() ?: 0
                        
                        when {
                            nama.isBlank() -> {
                                notifier?.show("Nama produk harus diisi", NotificationType.Error, 2000)
                            }
                            harga.isBlank() -> {
                                notifier?.show("Harga produk harus diisi", NotificationType.Error, 2000)
                            }
                            stokAwalInt < minStokInt -> {
                                notifier?.show("Stok awal tidak boleh lebih kecil dari stok minimum", NotificationType.Error, 2500)
                            }
                            else -> {
                                if (isEditMode) {
                                    val updatedProduct = productToEdit!!.copy(
                                        name = nama,
                                        category = kategori,
                                        price = harga.toIntOrNull() ?: 0,
                                        stock = stokAwalInt,
                                        minStock = minStokInt,
                                        imageUri = selectedImageUri?.toString()
                                    )
                                    viewModel.updateProduct(
                                        updatedProduct,
                                        onSuccess = {
                                            notifier?.show("Produk berhasil diperbarui", NotificationType.Success, 1800)
                                            navController.popBackStack()
                                        },
                                        onError = { error ->
                                            notifier?.show(error, NotificationType.Error, 2000)
                                        }
                                    )
                                } else {
                                    val newProduct = Product(
                                        id = System.currentTimeMillis(),
                                        name = nama,
                                        category = kategori,
                                        price = harga.toIntOrNull() ?: 0,
                                        stock = stokAwalInt,
                                        minStock = minStokInt,
                                        imageUri = selectedImageUri?.toString()
                                    )
                                    viewModel.addProduct(
                                        newProduct,
                                        onSuccess = {
                                            notifier?.show("Produk berhasil disimpan", NotificationType.Success, 1800)
                                            navController.popBackStack()
                                        },
                                        onError = { error ->
                                            notifier?.show(error, NotificationType.Error, 2000)
                                        }
                                    )
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        "Simpan Produk",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun InputField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun UploadFotoSection(
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    size: Dp = 140.dp
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher Galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> onImageSelected(uri) }

    // Launcher Kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) {
            onImageSelected(tempUri)
        }
    }

    // Fungsi untuk membuka kamera
    val launchCamera: () -> Unit = {
        val photoFile = createImageFile(context)
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            photoFile
        )
        tempUri = uri
        cameraLauncher.launch(uri)
    }

    // Permission launcher untuk kamera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, launch camera
            launchCamera()
        }
    }

    // Dialog Pilihan
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Pilih Foto", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Kamera (Ambil Foto Langsung)",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showDialog = false
                                // Cek permission dulu sebelum buka kamera
                                val cameraPermission = Manifest.permission.CAMERA
                                when {
                                    ContextCompat.checkSelfPermission(context, cameraPermission) == 
                                        PackageManager.PERMISSION_GRANTED -> {
                                        // Permission sudah ada, langsung buka kamera
                                        launchCamera()
                                    }
                                    else -> {
                                        // Minta permission dulu
                                        cameraPermissionLauncher.launch(cameraPermission)
                                    }
                                }
                            }
                            .padding(vertical = 12.dp)
                    )
                    Divider()
                    Text(
                        "Galeri (Pilih dari Penyimpanan)",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                galleryLauncher.launch("image/*")
                                showDialog = false
                            }
                            .padding(vertical = 12.dp)
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { showDialog = true }, // Klik memunculkan dialog
        contentAlignment = Alignment.Center
    ) {
        if (selectedImageUri != null) {
            Image(
                painter = rememberAsyncImagePainter(selectedImageUri),
                contentDescription = "Preview Foto",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Upload Foto",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Upload", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// Fungsi Helper Membuat File Temp
fun createImageFile(context: Context): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = context.externalCacheDir
    return File.createTempFile(
        "JPEG_${timeStamp}_", /* prefix */
        ".jpg", /* suffix */
        storageDir /* directory */
    )
}