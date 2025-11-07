package com.example.midterm

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import android.util.Base64

data class Product(
    val name: String = "",
    val category: String = "",
    val price: String = "",
    val imageBase64: String = ""
)

fun bitmapToBase64(bitmap: Bitmap): String {
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    val byteArray = outputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.DEFAULT)
}

fun base64ToBitmap(base64: String): Bitmap? {
    return try {
        val decodedBytes = Base64.decode(base64, Base64.DEFAULT)
        android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var imageBase64 by remember { mutableStateOf<String?>(null) }

    var productList by remember { mutableStateOf(listOf<Pair<String, Product>>()) }
    var isEditing by remember { mutableStateOf(false) }
    var editId by remember { mutableStateOf<String?>(null) }

    val backgroundColor = Color(0xFFFFF8F4)
    val buttonColor = Color(0xFF8B5CF6)
    val accentRed = Color(0xFF8B5CF6)

    // Picker chọn ảnh
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(context.contentResolver, it)
            } else {
                val source = ImageDecoder.createSource(context.contentResolver, it)
                ImageDecoder.decodeBitmap(source)
            }
            imageBase64 = bitmapToBase64(bitmap)
        }
    }

    LaunchedEffect(Unit) {
        db.collection("products").addSnapshotListener { snapshot, e ->
            if (e == null && snapshot != null) {
                productList = snapshot.documents.mapNotNull { doc ->
                    val product = doc.toObject(Product::class.java)
                    if (product != null) Pair(doc.id, product) else null
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Image(
            painter = painterResource(id = R.drawable.highlands_logo),
            contentDescription = "Highlands Coffee Logo",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Quản lý sản phẩm Highlands",
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = accentRed,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên sản phẩm") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Loại sản phẩm") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("Giá sản phẩm") },
            modifier = Modifier.fillMaxWidth()
        )

        // === Chọn ảnh ===
        Button(
            onClick = { picker.launch("image/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Chọn hình ảnh", color = Color.White)
        }

        // Hiển thị ảnh xem trước nếu có
        imageBase64?.let { b64 ->
            base64ToBitmap(b64)?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Ảnh đã chọn",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(top = 8.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (name.isNotEmpty() && category.isNotEmpty() && price.isNotEmpty()) {
                    val newProduct = Product(name, category, price, imageBase64 ?: "")

                    if (isEditing && editId != null) {
                        db.collection("products").document(editId!!).set(newProduct)
                        isEditing = false
                        editId = null
                    } else {
                        db.collection("products").add(newProduct)
                    }

                    name = ""
                    category = ""
                    price = ""
                    imageBase64 = null
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                if (isEditing) "CẬP NHẬT SẢN PHẨM" else "THÊM SẢN PHẨM",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Danh sách sản phẩm:",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(modifier = Modifier.fillMaxHeight(0.7f)) {
            items(productList) { (id, product) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        base64ToBitmap(product.imageBase64)?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Ảnh sản phẩm",
                                modifier = Modifier
                                    .size(60.dp)
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        ) {
                            Text("Tên sp: ${product.name}", fontWeight = FontWeight.Bold)
                            Text("Giá sp: ${product.price}")
                            Text("Loại sp: ${product.category}")
                        }

                        Row {
                            IconButton(onClick = {
                                name = product.name
                                category = product.category
                                price = product.price
                                imageBase64 = product.imageBase64
                                isEditing = true
                                editId = id
                            }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Sửa",
                                    tint = Color(0xFFFFC107)
                                )
                            }

                            IconButton(onClick = {
                                db.collection("products").document(id).delete()
                            }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Xóa",
                                    tint = Color.Red
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                navController.navigate(Screen.Signin.rout) // sửa rout -> route
            },
            colors = ButtonDefaults.buttonColors(containerColor = accentRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
        ) {
            Text("ĐĂNG XUẤT", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
