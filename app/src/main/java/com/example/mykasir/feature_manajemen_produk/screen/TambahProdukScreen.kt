package com.example.mykasir.feature_manajemen_produk.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mykasir.feature_manajemen_produk.model.Product
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel
import com.example.mykasir.ui.theme.PrimaryColor
import com.example.mykasir.ui.theme.BackgroundColor
import com.example.mykasir.ui.theme.SurfaceColor
import com.example.mykasir.ui.theme.TextPrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahProdukScreen(navController: NavController, viewModel: ProductViewModel) {
    var nama by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var stokAwal by remember { mutableStateOf("") }
    var minStok by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 🔹 Header dengan tombol kembali
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Kembali",
                    tint = PrimaryColor
                )
            }
            Text(
                text = "Tambah Produk",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryColor
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Upload Foto
        UploadFotoSection(
            selectedImageUri = selectedImageUri,
            onImageSelected = { uri -> selectedImageUri = uri }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Input Fields
        InputField(value = nama, onValueChange = { nama = it }, label = "Nama Produk")
        InputField(value = kategori, onValueChange = { kategori = it }, label = "Kategori")
        InputField(value = harga, onValueChange = { harga = it }, label = "Harga")
        InputField(value = stokAwal, onValueChange = { stokAwal = it }, label = "Stok Awal")
        InputField(value = minStok, onValueChange = { minStok = it }, label = "Stok Minimum")

        Spacer(modifier = Modifier.height(24.dp))

        // 🔹 Tombol Simpan
        Button(
            onClick = {
                if (nama.isNotBlank() && harga.isNotBlank()) {
                    val newProduct = Product(
                        id = System.currentTimeMillis(),
                        name = nama,
                        category = kategori,
                        price = harga.toIntOrNull() ?: 0,
                        stock = stokAwal.toIntOrNull() ?: 0,
                        minStock = minStok.toIntOrNull() ?: 0
                    )
                    viewModel.addProduct(newProduct)
                    navController.popBackStack()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Simpan", color = Color.White)
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
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryColor,
            unfocusedBorderColor = Color.LightGray
        )
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun UploadFotoSection(selectedImageUri: Uri?, onImageSelected: (Uri?) -> Unit) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> onImageSelected(uri) }

    Box(
        modifier = Modifier
            .size(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceColor)
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
                tint = PrimaryColor.copy(alpha = 0.7f),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
