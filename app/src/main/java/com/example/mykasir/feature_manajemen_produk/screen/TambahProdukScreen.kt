package com.example.mykasir.feature_manajemen_produk.screen

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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mykasir.core_ui.LocalNotifier
import com.example.mykasir.core_ui.NotificationType
import com.example.mykasir.core_ui.SimpleTopBar
import com.example.mykasir.feature_manajemen_produk.model.Product
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahProdukScreen(
    navController: NavController,
    viewModel: ProductViewModel,
    productId: Long? // <-- 1. TERIMA productId
) {
    val notifier = LocalNotifier.current

    // Tentukan apakah ini mode Edit
    val isEditMode = productId != null

    // State untuk menyimpan data produk yang akan diedit
    var productToEdit by remember { mutableStateOf<Product?>(null) }

    // State untuk field input
    var nama by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var stokAwal by remember { mutableStateOf("") }
    var minStok by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // --- 2. LOGIKA UNTUK MENGISI DATA JIKA MODE EDIT ---
    LaunchedEffect(productId) {
        if (isEditMode && productId != null) {
            // Cari produk di ViewModel berdasarkan ID
            productToEdit = viewModel.products.find { it.id == productId }

            // Isi semua state dengan data produk
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
    // --------------------------------------------------

    // Tentukan judul header
    val title = if (isEditMode) "Edit Produk" else "Tambah Produk"

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = title, // <-- 3. Judul dinamis
                onBackPress = { navController.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Upload Foto + judul (tepat di tengah atas kamera)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = "Foto Produk",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        UploadFotoSection(
                            selectedImageUri = selectedImageUri,
                            onImageSelected = { uri -> selectedImageUri = uri },
                            size = 96.dp
                        )
                    }

                    // Judul kecil section
                    Text(
                        text = "Detail Produk",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Input Fields
                    InputField(value = nama, onValueChange = { nama = it }, label = "Nama Produk")
                    InputField(value = kategori, onValueChange = { kategori = it }, label = "Kategori")
                    InputField(value = harga, onValueChange = { harga = it }, label = "Harga")
                    InputField(value = stokAwal, onValueChange = { stokAwal = it }, label = "Stok Awal")
                    InputField(value = minStok, onValueChange = { minStok = it }, label = "Stok Minimum")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tombol Simpan
            Button(
                onClick = {
                    if (nama.isNotBlank() && harga.isNotBlank()) {

                        // --- 4. LOGIKA TOMBOL SIMPAN ---
                        if (isEditMode) {
                            // Jika Mode Edit, panggil updateProduct
                            val updatedProduct = productToEdit!!.copy(
                                name = nama,
                                category = kategori,
                                price = harga.toIntOrNull() ?: 0,
                                stock = stokAwal.toIntOrNull() ?: 0,
                                minStock = minStok.toIntOrNull() ?: 0,
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
                            // Jika Mode Tambah, panggil addProduct
                            val newProduct = Product(
                                id = System.currentTimeMillis(), // Akan diganti oleh server
                                name = nama,
                                category = kategori,
                                price = harga.toIntOrNull() ?: 0,
                                stock = stokAwal.toIntOrNull() ?: 0,
                                minStock = minStok.toIntOrNull() ?: 0,
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
                        // ---------------------------------
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    "Simpan",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// Composable InputField dan UploadFotoSection tidak perlu diubah
// (Saya salin dari kode Anda sebelumnya)

@Composable
fun InputField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadFotoSection(
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    size: Dp = 140.dp
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showBottomSheet by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    
    // Launcher untuk pilih dari galeri
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> onImageSelected(uri) }
    
    // Launcher untuk ambil foto dari kamera
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onImageSelected(tempCameraUri)
        }
    }
    
    // Launcher untuk request permission kamera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, buka kamera
            val uri = createImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }
    
    // Fungsi untuk membuka kamera dengan permission check
    fun openCamera() {
        val permission = android.Manifest.permission.CAMERA
        when {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                // Permission sudah granted
                val uri = createImageUri(context)
                tempCameraUri = uri
                cameraLauncher.launch(uri)
            }
            else -> {
                // Request permission
                cameraPermissionLauncher.launch(permission)
            }
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { showBottomSheet = true },
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
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "Upload Foto",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier.size(48.dp)
            )
        }
    }
    
    // Bottom Sheet untuk pilih sumber foto
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Pilih Sumber Foto",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Opsi Kamera
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showBottomSheet = false
                            openCamera()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Kamera",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Ambil Foto",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                HorizontalDivider()
                
                // Opsi Galeri
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            showBottomSheet = false
                            galleryLauncher.launch("image/*")
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Galeri",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Pilih dari Galeri",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// Helper function untuk membuat Uri file gambar sementara
private fun createImageUri(context: android.content.Context): Uri {
    val imageFile = java.io.File(
        context.cacheDir,
        "camera_photo_${System.currentTimeMillis()}.jpg"
    )
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}