package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.FolderEntity
import com.example.data.PersonEntity
import com.example.ui.theme.FavoriteRed
import com.example.ui.theme.NoteAmber
import com.example.ui.theme.NoteAmberDarkText
import com.example.ui.theme.XBlue
import com.example.ui.theme.parseHexColor
import com.example.util.PersonWorkCodec

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonCard(
    person: PersonEntity,
    folder: FolderEntity?,
    onOpenDetail: () -> Unit,
    onQuickAddWork: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMoveToFolder: () -> Unit,
    onDelete: () -> Unit,
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val folderColor = parseHexColor(folder?.colorHex ?: "#1D9BF0", XBlue)
    val initial = person.name.trim().take(1).ifEmpty { "👤" }

    var isBioExpanded by remember(person.id, person.bio) { mutableStateOf(false) }
    var isBioTruncated by remember(person.id, person.bio) { mutableStateOf(false) }

    val works = remember(person.worksJson) { PersonWorkCodec.parseWorks(person.worksJson) }
    val worksSummary = remember(works) { PersonWorkCodec.summarizeWorks(works) }
    val photoWorks = remember(works) { works.filter { it.type == "photo" }.take(3) }
    val tagsList = remember(person.tags) {
        person.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    Surface(
        onClick = onOpenDetail,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(folderColor, XBlue))),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!person.avatarUrl.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model = person.avatarUrl,
                                contentDescription = person.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                error = {
                                    Text(
                                        text = initial,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                }
                            )
                        } else {
                            Text(
                                text = initial,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (person.role.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = person.role,
                                style = MaterialTheme.typography.bodySmall,
                                color = XBlue,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    onClick = onQuickAddWork,
                    shape = RoundedCornerShape(10.dp),
                    color = XBlue.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = XBlue,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "إضافة عمل",
                            color = XBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (person.bio.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = person.bio,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (isBioExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { result ->
                        if (!isBioExpanded) {
                            isBioTruncated = result.hasVisualOverflow
                        }
                    }
                )
                if (isBioTruncated || isBioExpanded) {
                    Text(
                        text = if (isBioExpanded) "عرض أقل" else "عرض المزيد",
                        color = XBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { isBioExpanded = !isBioExpanded }
                            .padding(vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Works summary strip
            Surface(
                onClick = onOpenDetail,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CollectionsBookmark,
                                contentDescription = null,
                                tint = XBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (worksSummary.total > 0) {
                                    "الأعمال والمرفقات (${worksSummary.total})"
                                } else {
                                    "لا توجد أعمال أو مرفقات بعد"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "عرض البروفايل",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = XBlue
                        )
                    }

                    if (worksSummary.total > 0) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (worksSummary.links > 0) {
                                WorkCountBadge(
                                    icon = Icons.Filled.Link,
                                    label = "${worksSummary.links} رابط"
                                )
                            }
                            if (worksSummary.photos > 0) {
                                WorkCountBadge(
                                    icon = Icons.Filled.Image,
                                    label = "${worksSummary.photos} صورة"
                                )
                            }
                            if (worksSummary.videos > 0) {
                                WorkCountBadge(
                                    icon = Icons.Filled.Videocam,
                                    label = "${worksSummary.videos} فيديو"
                                )
                            }
                            if (worksSummary.others > 0) {
                                WorkCountBadge(
                                    icon = Icons.Filled.CollectionsBookmark,
                                    label = "${worksSummary.others} أخرى"
                                )
                            }
                        }
                    }

                    if (photoWorks.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            photoWorks.forEach { photo ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    SubcomposeAsyncImage(
                                        model = photo.url,
                                        contentDescription = photo.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (!person.userNote.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NoteAmber.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, NoteAmber.copy(alpha = 0.30f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Filled.StickyNote2,
                            contentDescription = null,
                            tint = NoteAmberDarkText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "ملاحظتي الخاصة:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NoteAmberDarkText
                            )
                            Text(
                                text = person.userNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            if (folder != null || tagsList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (folder != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = folderColor.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, folderColor.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = folder.iconEmoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = folder.name,
                                    color = folderColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    tagsList.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "#$tag",
                                color = XBlue,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Row
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onOpenDetail) {
                        Icon(
                            imageVector = Icons.Filled.OpenInNew,
                            contentDescription = "فتح البروفايل",
                            tint = XBlue
                        )
                    }

                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "تعديل بيانات الشخص",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (person.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (person.isFavorite) FavoriteRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onMoveToFolder) {
                        Icon(
                            imageVector = Icons.Filled.DriveFileMove,
                            contentDescription = "نقل إلى مجموعة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = {
                        val lines = buildString {
                            append(person.name)
                            if (person.role.isNotBlank()) append(" — ${person.role}")
                            if (person.bio.isNotBlank()) append("\n${person.bio}")
                            if (works.isNotEmpty()) {
                                append("\n\nالأعمال:")
                                works.forEach { append("\n• ${it.title}: ${it.url}") }
                            }
                        }
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        cm?.setPrimaryClip(ClipData.newPlainText("Person Profile", lines))
                        onFeedback("تم نسخ ملخص الشخص إلى الحافظة")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "نسخ ملخص الشخص",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = {
                        val lines = buildString {
                            append(person.name)
                            if (person.role.isNotBlank()) append(" (${person.role})")
                            if (person.bio.isNotBlank()) append("\n${person.bio}")
                            if (works.isNotEmpty()) {
                                append("\n\nالأعمال والمرفقات:")
                                works.forEach { append("\n• ${it.title}: ${it.url}") }
                            }
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, person.name)
                            putExtra(Intent.EXTRA_TEXT, lines)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة ملف الشخص"))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "حذف الشخص",
                            tint = FavoriteRed.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkCountBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = XBlue,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
