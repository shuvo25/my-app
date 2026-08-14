package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberPrimary

@Composable
fun CategoryChipsRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmberPrimary,
                    selectedLabelColor = Color.Black,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = null
            )
        }
    }
}
