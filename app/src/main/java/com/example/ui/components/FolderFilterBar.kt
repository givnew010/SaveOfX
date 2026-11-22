package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FolderWithCount
import com.example.ui.theme.XBlue

@Composable
fun FolderFilterBar(
    folders: List<FolderWithCount>,
    selectedFolderId: Long?,
    showFavoritesOnly: Boolean,
    totalBookmarksCount: Int,
    onSelectAll: () -> Unit,
    onToggleFavorites: () -> Unit,
    onSelectFolder: (Long) -> Unit,
    onAddNewFolder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "All Bookmarks" Chip
        val isAllSelected = selectedFolderId == null && !showFavoritesOnly
        FilterPill(
            selected = isAllSelected,
            onClick = onSelectAll,
            label = "كل العلامات",
            icon = "📑",
            count = totalBookmarksCount,
            accentColor = XBlue,
            testTag = "filter_pill_all"
        )

        // "Favorites" Chip
        FilterPill(
            selected = showFavoritesOnly,
            onClick = onToggleFavorites,
            label = "المفضلة",
            icon = "⭐",
            accentColor = Color(0xFFEF4444),
            testTag = "filter_pill_favorites"
        )

        // Individual Folders Chips
        folders.forEach { item ->
            val folder = item.folder
            val isSelected = selectedFolderId == folder.id && !showFavoritesOnly
            val color = try {
                Color(android.graphics.Color.parseColor(folder.colorHex))
            } catch (e: Exception) {
                XBlue
            }

            FilterPill(
                selected = isSelected,
                onClick = { onSelectFolder(folder.id) },
                label = folder.name,
                icon = folder.iconEmoji,
                count = item.count,
                accentColor = color,
                testTag = "filter_pill_folder_${folder.id}"
            )
        }

        // Add Folder Quick Button
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                .clickable { onAddNewFolder() }
                .padding(horizontal = 12.dp, vertical = 7.dp)
                .testTag("filter_add_folder_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "إنشاء مجلد",
                    tint = XBlue,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "مجلد جديد",
                    color = XBlue,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FilterPill(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: String,
    count: Int? = null,
    accentColor: Color,
    testTag: String
) {
    val backgroundColor = if (selected) {
        accentColor.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (selected) {
        accentColor
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = icon, fontSize = 14.sp)
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface
            )

            if (count != null) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selected) accentColor else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$count",
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
