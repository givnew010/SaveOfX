package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FolderWithCount
import com.example.ui.theme.FavoriteRed
import com.example.ui.theme.XBlue
import com.example.ui.theme.parseHexColor

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
    allLabel: String = "كل العلامات",
    allIcon: String = "📑",
    getFolderCount: (FolderWithCount) -> Int = { it.count }
) {
    val isAllSelected = selectedFolderId == null && !showFavoritesOnly

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item(key = "all") {
            FilterChipPill(
                selected = isAllSelected,
                label = allLabel,
                iconEmoji = allIcon,
                count = totalBookmarksCount,
                accentColor = XBlue,
                onClick = onSelectAll
            )
        }

        item(key = "favorites") {
            FilterChipPill(
                selected = showFavoritesOnly,
                label = "المفضلة",
                iconEmoji = "⭐",
                count = null,
                accentColor = FavoriteRed,
                onClick = onToggleFavorites
            )
        }

        items(items = folders, key = { it.folder.id }) { item ->
            val folder = item.folder
            val isSelected = selectedFolderId == folder.id && !showFavoritesOnly
            FilterChipPill(
                selected = isSelected,
                label = folder.name,
                iconEmoji = folder.iconEmoji,
                count = getFolderCount(item),
                accentColor = parseHexColor(folder.colorHex, XBlue),
                onClick = { onSelectFolder(folder.id) }
            )
        }

        item(key = "add_new") {
            Surface(
                onClick = onAddNewFolder,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.heightIn(min = 40.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = XBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "مجموعة جديدة",
                        color = XBlue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipPill(
    selected: Boolean,
    label: String,
    iconEmoji: String,
    count: Int?,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (selected) accentColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) accentColor else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.heightIn(min = 40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(text = iconEmoji, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
            if (count != null) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selected) accentColor
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = count.toString(),
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
