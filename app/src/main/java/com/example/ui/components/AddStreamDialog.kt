package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberPrimary

@Composable
fun AddStreamDialog(
    onDismiss: () -> Unit,
    onAddStream: (title: String, url: String, category: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var streamUrl by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Trending") }
    val categories = listOf("Trending", "Cinematic", "Music", "Animation", "Tech", "Gaming", "Nature")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddLink,
                    contentDescription = null,
                    tint = AmberPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Import Media / Stream URL",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Stream or download any direct MP4, HLS (.m3u8), or web media URL directly into StreamVault.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it },
                    label = { Text("Stream / Media URL (MP4, HLS)") },
                    placeholder = { Text("https://example.com/video.mp4") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        focusedLabelColor = AmberPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Video Title (Optional)") },
                    placeholder = { Text("My Custom Stream") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AmberPrimary,
                        focusedLabelColor = AmberPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Category:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(4).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberPrimary,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (streamUrl.isNotBlank()) {
                        onAddStream(
                            if (title.isNotBlank()) title else "Stream ${System.currentTimeMillis() % 10000}",
                            streamUrl.trim(),
                            selectedCategory
                        )
                    }
                },
                enabled = streamUrl.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Stream Now", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
