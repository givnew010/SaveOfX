package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.util.PersonWorkItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PersonDetailSheet(
    person: PersonEntity,
    folder: FolderEntity?,
    autoFocusWorkForm: Boolean,
    onEditProfile: () -> Unit,
    onAddWork: (type: String, title: String, url: String, note: String?) -> Unit,
    onRemoveWork: (workId: String) -> Unit,
    onFeedback: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val folderColor = parseHexColor(folder?.colorHex ?: "#1D9BF0", XBlue)
    val works = remember(person.worksJson) { PersonWorkCodec.parseWorks(person.worksJson) }
    val summary = remember(works) { PersonWorkCodec.summarizeWorks(works) }

    var showAddForm by remember(person.id, autoFocusWorkForm) {
        mutableStateOf(autoFocusWorkForm || works.isEmpty())
    }
    var workType by remember(person.id) { mutableStateOf("link") }
    var workTitle by remember(person.id) { mutableStateOf("") }
    var workUrl by remember(person.id) { mutableStateOf("") }
    var workNote by remember(person.id) { mutableStateOf("") }
    var activeFilter by remember(person.id) { mutableStateOf("all") }

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
            workType = "photo"
            workUrl = uri.toString()
        }
    }

    val filteredWorks = remember(works, activeFilter) {
        if (activeFilter == "all") works else works.filter { it.type == activeFilter }
    }

    val tagsList = remember(person.tags) {
        person.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

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
            // Profile Header
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
                            .size(60.dp)
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
                                        text = person.name.trim().take(1).ifEmpty { "👤" },
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp
                                    )
                                }
                            )
                        } else {
                            Text(
                                text = person.name.trim().take(1).ifEmpty { "👤" },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (person.role.isNotBlank()) {
                            Text(
                                text = person.role,
                                style = MaterialTheme.typography.bodyMedium,
                                color = XBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEditProfile) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "تعديل البيانات",
                            tint = XBlue
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
            }

            if (folder != null || tagsList.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (folder != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = folderColor.copy(alpha = 0.12f),
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

            if (person.bio.isNotBlank()) {
                Text(
                    text = person.bio,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (!person.userNote.isNullOrBlank()) {
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
                        Text(
                            text = person.userNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Toggle Add Work Form Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الأعمال والوسائط (${summary.total})",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Button(
                    onClick = { showAddForm = !showAddForm },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showAddForm) MaterialTheme.colorScheme.surfaceVariant else XBlue,
                        contentColor = if (showAddForm) MaterialTheme.colorScheme.onSurface else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (showAddForm) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showAddForm) "إخفاء النموذج" else "إضافة عمل / وسائط",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(visible = showAddForm) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, XBlue.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "إضافة عمل جديد إلى ملف ${person.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "link" to "رابط عمل",
                                "photo" to "صورة",
                                "video" to "فيديو",
                                "other" to "أخرى"
                            ).forEach { (key, label) ->
                                val selected = workType == key
                                FilterChip(
                                    selected = selected,
                                    onClick = { workType = key },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = XBlue.copy(alpha = 0.16f),
                                        selectedLabelColor = XBlue
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = workTitle,
                            onValueChange = { workTitle = it },
                            label = { Text("عنوان العمل أو الوصف") },
                            placeholder = { Text("مثال: فيلم Inception، صورة جديدة، مقابلة…") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XBlue),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = workUrl,
                            onValueChange = { next ->
                                workUrl = next
                                if (next.isNotBlank()) {
                                    val detected = PersonWorkCodec.detectWorkTypeFromUrl(next)
                                    if (detected != "link") {
                                        workType = detected
                                    }
                                }
                            },
                            label = { Text("الرابط أو ملف الصورة / الفيديو *") },
                            placeholder = { Text("https://... أو اختر صورة من الجهاز") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XBlue),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedButton(
                            onClick = { imagePickerLauncher.launch(arrayOf("image/*")) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("اختيار صورة من الجهاز", fontSize = 12.sp)
                        }

                        OutlinedTextField(
                            value = workNote,
                            onValueChange = { workNote = it },
                            label = { Text("ملاحظة حول العمل (اختياري)") },
                            maxLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XBlue),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (workUrl.trim().isNotEmpty()) {
                                    onAddWork(
                                        workType,
                                        workTitle.trim(),
                                        workUrl.trim(),
                                        workNote.trim().takeIf { it.isNotEmpty() }
                                    )
                                    workTitle = ""
                                    workUrl = ""
                                    workNote = ""
                                }
                            },
                            enabled = workUrl.trim().isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = XBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("إضافة إلى البروفايل", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (works.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "all" to "الكل (${summary.total})",
                        "link" to "روابط (${summary.links})",
                        "photo" to "صور (${summary.photos})",
                        "video" to "فيديو (${summary.videos})",
                        "other" to "أخرى (${summary.others})"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = activeFilter == key,
                            onClick = { activeFilter = key },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = XBlue.copy(alpha = 0.16f),
                                selectedLabelColor = XBlue
                            )
                        )
                    }
                }
            }

            if (filteredWorks.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (works.isEmpty()) {
                            "لم يتم إضافة أعمال أو صور أو روابط لهذا الشخص بعد."
                        } else {
                            "لا توجد عناصر في هذا التصنيف."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredWorks.forEach { work ->
                        PersonWorkRowCard(
                            work = work,
                            onOpenUrl = {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(work.url)))
                                }.onFailure {
                                    onFeedback("تعذر فتح الرابط المحدد")
                                }
                            },
                            onDelete = { onRemoveWork(work.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonWorkRowCard(
    work: PersonWorkItem,
    onOpenUrl: () -> Unit,
    onDelete: () -> Unit
) {
    val typeIcon = when (work.type) {
        "photo" -> Icons.Filled.Image
        "video" -> Icons.Filled.Videocam
        "other" -> Icons.Filled.CollectionsBookmark
        else -> Icons.Filled.Link
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = XBlue.copy(alpha = 0.14f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = typeIcon,
                                contentDescription = null,
                                tint = XBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = PersonWorkCodec.getWorkTypeLabel(work.type),
                                color = XBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = work.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row {
                    IconButton(onClick = onOpenUrl) {
                        Icon(
                            imageVector = if (work.type == "video") Icons.Filled.PlayCircleFilled else Icons.Filled.OpenInNew,
                            contentDescription = "فتح العمل",
                            tint = XBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "حذف العمل",
                            tint = FavoriteRed.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            if (work.type == "photo") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    SubcomposeAsyncImage(
                        model = work.url,
                        contentDescription = work.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (!work.note.isNullOrBlank()) {
                Text(
                    text = work.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = work.url,
                style = MaterialTheme.typography.bodySmall,
                color = XBlue,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
