package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.FolderWithCount
import com.example.ui.theme.PurpleAccent
import com.example.ui.theme.XBlue
import com.example.ui.theme.parseHexColor
import com.example.util.ParsedTweet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SaveBookmarkBottomSheet(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    parsedTweet: ParsedTweet?,
    isFetchingTweet: Boolean,
    isOfflinePreview: Boolean,
    folders: List<FolderWithCount>,
    selectedFolderId: Long?,
    onSelectFolder: (Long) -> Unit,
    userNote: String,
    onUserNoteChange: (String) -> Unit,
    tags: String,
    onTagsChange: (String) -> Unit,
    onOpenCreateFolder: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isSaveEnabled = urlInput.isNotBlank() && selectedFolderId != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(XBlue.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BookmarkAdd,
                            contentDescription = null,
                            tint = XBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "حفظ التغريدة في مجموعة",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "إغلاق",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlChange,
                label = { Text("رابط التغريدة (X / Twitter)") },
                placeholder = { Text("https://x.com/username/status/...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Link, contentDescription = null, tint = XBlue)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XBlue),
                modifier = Modifier.fillMaxWidth()
            )

            if (isFetchingTweet) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = XBlue.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, XBlue.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = XBlue,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "جاري جلب نص التغريدة والوسائط المرفقة…",
                            color = XBlue,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (parsedTweet != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!parsedTweet.authorAvatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = parsedTweet.authorAvatarUrl,
                                    contentDescription = parsedTweet.authorName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Column {
                                Text(
                                    text = parsedTweet.authorName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = parsedTweet.authorHandle,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (parsedTweet.isContentFetched && parsedTweet.content.isNotBlank()) {
                            Text(
                                text = parsedTweet.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else if (isOfflinePreview || !parsedTweet.isContentFetched) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.CloudOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "لا يوجد اتصال بالإنترنت — سيتم حفظ الرابط فقط وعرضه لاحقاً عند التوفر",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        val photosCount = parsedTweet.mediaItems.count { it.type == "photo" }
                        val videosCount = parsedTweet.mediaItems.count { it.type == "video" || it.type == "gif" }
                        if (photosCount > 0 || videosCount > 0) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (photosCount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = XBlue.copy(alpha = 0.14f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Image,
                                                contentDescription = null,
                                                tint = XBlue,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "$photosCount صور مرفقة",
                                                color = XBlue,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                                if (videosCount > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PurpleAccent.copy(alpha = 0.14f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Videocam,
                                                contentDescription = null,
                                                tint = PurpleAccent,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "فيديو / GIF مرفق",
                                                color = PurpleAccent,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "اختر مجموعة الحفظ:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = onOpenCreateFolder) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = XBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مجموعة جديدة", color = XBlue, fontWeight = FontWeight.Bold)
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    folders.forEach { item ->
                        val folder = item.folder
                        val isSelected = selectedFolderId == folder.id
                        val color = parseHexColor(folder.colorHex, XBlue)

                        Surface(
                            onClick = { onSelectFolder(folder.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) color.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) color else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = folder.iconEmoji, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = folder.name,
                                    color = if (isSelected) color else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = userNote,
                onValueChange = onUserNoteChange,
                label = { Text("أضف ملاحظة شخصية (اختياري)") },
                placeholder = { Text("مثال: ثريد مفيد لتطوير التطبيقات…") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.StickyNote2, contentDescription = null)
                },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XBlue),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tags,
                onValueChange = onTagsChange,
                label = { Text("وسوم التصنيف (مفصولة بفواصل)") },
                placeholder = { Text("ذكاء_اصطناعي, تقنية, برمجة") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XBlue),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onSave,
                enabled = isSaveEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = XBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Filled.BookmarkAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ في المجموعة الآن", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
