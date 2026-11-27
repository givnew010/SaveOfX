package com.example.ui.components

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.FolderWithCount
import com.example.data.PersonEntity
import com.example.ui.theme.FavoriteRed
import com.example.ui.theme.XBlue
import com.example.ui.theme.parseHexColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SavePersonBottomSheet(
    personToEdit: PersonEntity?,
    folders: List<FolderWithCount>,
    selectedFolderId: Long?,
    onSelectFolder: (Long) -> Unit,
    onOpenCreateFolder: () -> Unit,
    onSave: (
        name: String,
        role: String,
        avatarUrl: String?,
        bio: String,
        userNote: String?,
        tags: String,
        folderId: Long
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember(personToEdit?.id) { mutableStateOf(personToEdit?.name.orEmpty()) }
    var role by remember(personToEdit?.id) { mutableStateOf(personToEdit?.role.orEmpty()) }
    var avatarUrl by remember(personToEdit?.id) { mutableStateOf(personToEdit?.avatarUrl.orEmpty()) }
    var bio by remember(personToEdit?.id) { mutableStateOf(personToEdit?.bio.orEmpty()) }
    var userNote by remember(personToEdit?.id) { mutableStateOf(personToEdit?.userNote.orEmpty()) }
    var tags by remember(personToEdit?.id) { mutableStateOf(personToEdit?.tags.orEmpty()) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            avatarUrl = uri.toString()
        }
    }

    val resolvedFolderId = selectedFolderId
        ?: personToEdit?.folderId
        ?: folders.firstOrNull()?.folder?.id
    val isSaveEnabled = name.trim().isNotEmpty() && resolvedFolderId != null

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
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = null,
                            tint = XBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (personToEdit != null) "تعديل ملف الشخص" else "إضافة شخص جديد",
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

            // Photo preview + Picker
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(XBlue, Color(0xFF0EA5E9)))),
                            contentAlignment = Alignment.Center
                        ) {
                            if (avatarUrl.isNotBlank()) {
                                SubcomposeAsyncImage(
                                    model = avatarUrl,
                                    contentDescription = name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    error = {
                                        Text(
                                            text = name.trim().take(1).ifEmpty { "👤" },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    }
                                )
                            } else {
                                Text(
                                    text = name.trim().take(1).ifEmpty { "👤" },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "صورة الشخص",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { imagePickerLauncher.launch(arrayOf("image/*")) },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Image,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("اختيار صورة من الجهاز", fontSize = 12.sp)
                                }

                                if (avatarUrl.isNotBlank()) {
                                    IconButton(onClick = { avatarUrl = "" }) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "إزالة الصورة",
                                            tint = FavoriteRed
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = avatarUrl,
                        onValueChange = { avatarUrl = it },
                        label = { Text("أو رابط الصورة (URL)") },
                        placeholder = { Text("https://example.com/photo.jpg") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Link, contentDescription = null, tint = XBlue)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XBlue),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("اسم الشخص *") },
                placeholder = { Text("مثال: عمر الشريف، كريستوفر نولان…") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = XBlue)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XBlue),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = role,
                onValueChange = { role = it },
                label = { Text("الصفة / المجال (اختياري)") },
                placeholder = { Text("مثال: ممثل، مخرج، كاتب، مصور…") },
                leadingIcon = {
                    Icon(imageVector = Icons.Filled.Badge, contentDescription = null, tint = XBlue)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XBlue),
                modifier = Modifier.fillMaxWidth()
            )

            // Folder selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "اختر المجموعة:",
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
                        val isSelected = resolvedFolderId == folder.id
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
                value = bio,
                onValueChange = { bio = it },
                label = { Text("نبذة تعريفية عن الشخص (اختياري)") },
                placeholder = { Text("معلومات مختصرة عن المسيرة أو الأعمال…") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XBlue),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = userNote,
                onValueChange = { userNote = it },
                label = { Text("ملاحظتي الخاصة (اختياري)") },
                placeholder = { Text("مثال: أعمال مميزة أرغب في متابعتها…") },
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
                onValueChange = { tags = it },
                label = { Text("وسوم التصنيف (مفصولة بفواصل)") },
                placeholder = { Text("ممثل, سينما, دراما") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XBlue),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (isSaveEnabled && resolvedFolderId != null) {
                        onSave(
                            name.trim(),
                            role.trim(),
                            avatarUrl.trim().takeIf { it.isNotEmpty() },
                            bio.trim(),
                            userNote.trim().takeIf { it.isNotEmpty() },
                            tags.trim(),
                            resolvedFolderId
                        )
                    }
                },
                enabled = isSaveEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = XBlue),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (personToEdit != null) "حفظ التعديلات" else "حفظ الشخص في المجموعة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
