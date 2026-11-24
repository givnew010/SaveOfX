package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.FolderEntity
import com.example.data.TweetBookmarkEntity
import com.example.ui.theme.FavoriteRed
import com.example.ui.theme.NoteAmber
import com.example.ui.theme.NoteAmberDarkText
import com.example.ui.theme.XBlue
import com.example.ui.theme.parseHexColor
import com.example.util.TweetMediaItem
import com.example.util.TweetParser

import java.util.Locale

private fun formatCompactCount(count: Long): String {
    return when {
        count >= 1_000_000L -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
        count >= 1_000L -> String.format(Locale.US, "%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TweetCard(
    bookmark: TweetBookmarkEntity,
    folder: FolderEntity?,
    isRefreshing: Boolean,
    onRefreshContent: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMoveToFolder: () -> Unit,
    onDelete: () -> Unit,
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val folderColor = parseHexColor(folder?.colorHex ?: "#1D9BF0", XBlue)
    val initial = bookmark.authorName.trim().take(1).ifEmpty { "𝕏" }

    var isTextExpanded by remember(bookmark.id, bookmark.content) { mutableStateOf(false) }
    var isTextTruncated by remember(bookmark.id, bookmark.content) { mutableStateOf(false) }

    val mediaItems: List<TweetMediaItem> = remember(bookmark.mediaJson, bookmark.mediaUrl, bookmark.mediaType) {
        val parsed = TweetParser.deserializeMediaItems(bookmark.mediaJson)
        when {
            parsed.isNotEmpty() -> parsed
            !bookmark.mediaUrl.isNullOrBlank() -> listOf(
                TweetMediaItem(
                    type = bookmark.mediaType ?: "photo",
                    url = bookmark.mediaUrl
                )
            )
            else -> emptyList()
        }
    }

    val tagsList = remember(bookmark.tags) {
        bookmark.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    Surface(
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
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(folderColor, XBlue))),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!bookmark.authorAvatarUrl.isNullOrBlank()) {
                            SubcomposeAsyncImage(
                                model = bookmark.authorAvatarUrl,
                                contentDescription = bookmark.authorName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                error = {
                                    Text(
                                        text = initial,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            )
                        } else {
                            Text(
                                text = initial,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = bookmark.authorName,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = "حساب موثق",
                                tint = XBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = bookmark.authorHandle,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (bookmark.postedAt.isNotBlank()) {
                                Text(
                                    text = " · ${bookmark.postedAt}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!bookmark.isContentFetched) {
                        Surface(
                            onClick = { if (!isRefreshing) onRefreshContent() },
                            shape = RoundedCornerShape(8.dp),
                            color = XBlue.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isRefreshing) {
                                    CircularProgressIndicator(
                                        color = XBlue,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = null,
                                        tint = XBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "جلب",
                                        color = XBlue,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "𝕏",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tweet Body
            if (bookmark.isContentFetched) {
                if (bookmark.content.isNotBlank() && bookmark.content != bookmark.tweetUrl) {
                    Text(
                        text = bookmark.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (isTextExpanded) Int.MAX_VALUE else 6,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { result ->
                            if (!isTextExpanded) {
                                isTextTruncated = result.hasVisualOverflow
                            }
                        }
                    )

                    if (isTextTruncated || isTextExpanded) {
                        Text(
                            text = if (isTextExpanded) "عرض أقل" else "عرض المزيد",
                            color = XBlue,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { isTextExpanded = !isTextExpanded }
                                .padding(vertical = 2.dp)
                        )
                    }
                }

                if (mediaItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TweetMediaGallery(
                        mediaItems = mediaItems,
                        tweetUrl = bookmark.tweetUrl
                    )
                }

                if (!bookmark.quoteText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = bookmark.quoteAuthorName ?: "اقتباس",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!bookmark.quoteAuthorHandle.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = bookmark.quoteAuthorHandle,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = bookmark.quoteText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (bookmark.likesCount > 0L || bookmark.repostsCount > 0L) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (bookmark.repostsCount > 0L) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Repeat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatCompactCount(bookmark.repostsCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (bookmark.likesCount > 0L) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.FavoriteBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formatCompactCount(bookmark.likesCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                Surface(
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(bookmark.tweetUrl)))
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CloudOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "يعرض الرابط فقط (يتطلب إنترنت لجلب المحتوى)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Link,
                                contentDescription = null,
                                tint = XBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = bookmark.tweetUrl,
                                color = XBlue,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                textDecoration = TextDecoration.Underline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            if (!bookmark.userNote.isNullOrBlank()) {
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
                                text = bookmark.userNote,
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
                    IconButton(onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(bookmark.tweetUrl)))
                        }.onFailure {
                            onFeedback("تعذر فتح الرابط")
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.OpenInNew,
                            contentDescription = "فتح في منصة X",
                            tint = XBlue
                        )
                    }

                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (bookmark.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (bookmark.isFavorite) FavoriteRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onMoveToFolder) {
                        Icon(
                            imageVector = Icons.Filled.DriveFileMove,
                            contentDescription = "تغيير المجموعة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        cm?.setPrimaryClip(ClipData.newPlainText("Tweet URL", bookmark.tweetUrl))
                        onFeedback("تم نسخ رابط التغريدة إلى الحافظة")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "نسخ الرابط",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = {
                        val shareBody = if (bookmark.isContentFetched && bookmark.content != bookmark.tweetUrl) {
                            "${bookmark.content}\n\n${bookmark.tweetUrl}"
                        } else {
                            bookmark.tweetUrl
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, bookmark.authorName)
                            putExtra(Intent.EXTRA_TEXT, shareBody)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة المنشور"))
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
                            contentDescription = "حذف",
                            tint = FavoriteRed.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}
