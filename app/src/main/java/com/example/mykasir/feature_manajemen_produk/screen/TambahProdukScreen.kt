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
                            viewModel.updateProduct(updatedProduct)
                            notifier?.show("Produk berhasil diperbarui", NotificationType.Success, 1800)
                        } else {
                            // Jika Mode Tambah, panggil addProduct
                            val newProduct = Product(
                                id = System.currentTimeMillis(),
                                name = nama,
                                category = kategori,
                                price = harga.toIntOrNull() ?: 0,
                                stock = stokAwal.toIntOrNull() ?: 0,
                                minStock = minStok.toIntOrNull() ?: 0,
                                imageUri = selectedImageUri?.toString()
                            )
                            viewModel.addProduct(newProduct)
                            notifier?.show("Produk berhasil disimpan", NotificationType.Success, 1800)
                        }
                        // ---------------------------------

                        navController.popBackStack() // Kembali ke layar sebelumnya
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

@Composable
fun UploadFotoSection(
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    size: Dp = 140.dp
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> onImageSelected(uri) }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { launcher.launch("image/*") },
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
}