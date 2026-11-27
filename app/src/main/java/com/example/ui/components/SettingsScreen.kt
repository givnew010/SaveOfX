package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BackupManager
import com.example.data.BackupPayload
import com.example.data.RestoreMode
import com.example.data.ThemeMode
import com.example.ui.theme.FavoriteRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.XBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun formatBackupDate(iso: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = parser.parse(iso) ?: return iso
        SimpleDateFormat("d MMM yyyy · HH:mm", Locale.forLanguageTag("ar")).format(date)
    } catch (_: Exception) {
        iso.take(16)
    }
}

@Composable
fun SettingsScreen(
    totalFolders: Int,
    totalBookmarks: Int,
    totalAccounts: Int,
    totalPeople: Int,
    lastBackupAt: String?,
    themeMode: ThemeMode,
    onSetThemeMode: (ThemeMode) -> Unit,
    onExportBackupJson: suspend () -> String,
    onRestoreBackup: (BackupPayload, RestoreMode) -> Unit,
    onFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var restoreMode by remember { mutableStateOf(RestoreMode.MERGE) }
    var pendingBackup by remember { mutableStateOf<BackupPayload?>(null) }
    var pendingSourceName by remember { mutableStateOf("") }
    var restoreError by remember { mutableStateOf<String?>(null) }
    var showPasteBox by remember { mutableStateOf(false) }
    var pastedJson by remember { mutableStateOf("") }

    fun processRawBackupText(rawText: String, sourceLabel: String) {
        restoreError = null
        val parsed = BackupManager.parseBackupJson(rawText)
        if (parsed == null) {
            pendingBackup = null
            pendingSourceName = ""
            restoreError = "الملف أو النص المحدد لا يحتوي على نسخة احتياطية صالحة لتطبيق X Folders."
        } else {
            pendingBackup = parsed
            pendingSourceName = sourceLabel
        }
    }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val json = onExportBackupJson()
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(json.toByteArray(Charsets.UTF_8))
                    } ?: error("Cannot open output stream")
                }
            }
            if (result.isSuccess) {
                onFeedback("تم حفظ ملف النسخة الاحتياطية بنجاح")
            } else {
                onFeedback("تعذر حفظ ملف النسخة الاحتياطية")
            }
        }
    }

    val openBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val textResult = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use {
                        it.readText()
                    } ?: error("Cannot read file")
                }
            }
            textResult.onSuccess { content ->
                processRawBackupText(content, "ملف JSON")
            }.onFailure {
                restoreError = "تعذر قراءة الملف المحدد."
            }
        }
    }

    val systemDark = isSystemInDarkTheme()
    val isCurrentlyDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Backup Section
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(XBlue.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Backup,
                                contentDescription = null,
                                tint = XBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "النسخ الاحتياطي للبيانات",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "احفظ نسخة كاملة من مجموعاتك وعلاماتك المرجعية والحسابات والأشخاص وملاحظاتك في ملف JSON لنقلها أو استعادتها في أي وقت.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "$totalFolders مجموعات · $totalBookmarks علامة · $totalAccounts حساب · $totalPeople شخص",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (lastBackupAt != null) {
                                    "آخر نسخة احتياطية: ${formatBackupDate(lastBackupAt)}"
                                } else {
                                    "لم يتم إنشاء نسخة احتياطية بعد"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val datePart = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                                createBackupLauncher.launch("x-folders-backup-$datePart.json")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = XBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("حفظ نسخة (JSON)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val json = onExportBackupJson()
                                    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cm?.setPrimaryClip(ClipData.newPlainText("X Folders Backup", json))
                                    onFeedback("تم نسخ بيانات النسخة الاحتياطية إلى الحافظة")
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نسخ", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Restore Section
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Restore,
                                contentDescription = null,
                                tint = SuccessGreen
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "استعادة البيانات من نسخة احتياطية",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "استورد ملف نسخة احتياطية بصيغة JSON لاسترجاع جميع المجموعات والتغريدات والحسابات والأشخاص.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Restore Mode Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RestoreModeOptionCard(
                            selected = restoreMode == RestoreMode.MERGE,
                            title = "دمج مع البيانات الحالية",
                            subtitle = "يضيف المجموعات والعناصر الجديدة فقط.",
                            onClick = { restoreMode = RestoreMode.MERGE },
                            modifier = Modifier.weight(1f)
                        )
                        RestoreModeOptionCard(
                            selected = restoreMode == RestoreMode.REPLACE,
                            title = "استبدال البيانات الحالية",
                            subtitle = "يستبدل جميع المجموعات والعلامات والحسابات والأشخاص.",
                            onClick = { restoreMode = RestoreMode.REPLACE },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // File Picker & Paste Actions
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.UploadFile,
                                contentDescription = null,
                                tint = XBlue,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "اختر ملف النسخة الاحتياطية (.json) أو الصق المحتوى",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        openBackupLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = XBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.UploadFile,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("اختيار ملف JSON", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { showPasteBox = !showPasteBox },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentPaste,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("لصق JSON يدوياً", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (showPasteBox) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = pastedJson,
                                onValueChange = { pastedJson = it },
                                placeholder = {
                                    Text(
                                        text = "{\"app\":\"X Folders\",\"version\":1,\"folders\":[...],\"bookmarks\":[...]}",
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp
                                    )
                                },
                                minLines = 3,
                                maxLines = 6,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = XBlue),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (pastedJson.isNotBlank()) {
                                        processRawBackupText(pastedJson, "نص JSON ملصق")
                                    }
                                },
                                enabled = pastedJson.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = XBlue),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("فحص البيانات", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (restoreError != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FavoriteRed.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, FavoriteRed.copy(alpha = 0.30f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ErrorOutline,
                                    contentDescription = null,
                                    tint = FavoriteRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = restoreError.orEmpty(),
                                    color = FavoriteRed,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    pendingBackup?.let { backup ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SuccessGreen.copy(alpha = 0.10f),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = SuccessGreen
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "النسخة الاحتياطية جاهزة للاستعادة ($pendingSourceName)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${backup.folders.size} مجموعات · ${backup.bookmarks.size} علامة · ${backup.accounts.size} حساب · ${backup.people.size} شخص · ${formatBackupDate(backup.exportedAt)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            pendingBackup = null
                                            pendingSourceName = ""
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("إلغاء")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            onRestoreBackup(backup, restoreMode)
                                            pendingBackup = null
                                            pendingSourceName = ""
                                            pastedJson = ""
                                            showPasteBox = false
                                            restoreError = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = if (restoreMode == RestoreMode.REPLACE) {
                                                "تأكيد الاستبدال والاستعادة"
                                            } else {
                                                "تأكيد الدمج والاستعادة"
                                            },
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Appearance Section
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "مظهر التطبيق",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "اختر بين الوضع النهاري والوضع الليلي.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            Surface(
                                onClick = { onSetThemeMode(ThemeMode.LIGHT) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isCurrentlyDark) MaterialTheme.colorScheme.surface else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LightMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (!isCurrentlyDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "نهاري",
                                        fontSize = 12.sp,
                                        fontWeight = if (!isCurrentlyDark) FontWeight.Bold else FontWeight.Medium,
                                        color = if (!isCurrentlyDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                onClick = { onSetThemeMode(ThemeMode.DARK) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrentlyDark) MaterialTheme.colorScheme.surface else Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DarkMode,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = if (isCurrentlyDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ليلي",
                                        fontSize = 12.sp,
                                        fontWeight = if (isCurrentlyDark) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrentlyDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun RestoreModeOptionCard(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) XBlue.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) XBlue else MaterialTheme.colorScheme.outline
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) XBlue else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
