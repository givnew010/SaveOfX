package com.example.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ThemeMode
import com.example.ui.components.CreateFolderDialog
import com.example.ui.components.DeleteFolderDialog
import com.example.ui.components.EditFolderDialog
import com.example.ui.components.FolderCard
import com.example.ui.components.FolderFilterBar
import com.example.ui.components.MoveBookmarkDialog
import com.example.ui.components.SaveBookmarkBottomSheet
import com.example.ui.components.SettingsScreen
import com.example.ui.components.TopAppBarSection
import com.example.ui.components.TweetCard
import com.example.ui.theme.XBlue

@Composable
fun MainScreen(viewModel: BookmarkViewModel) {
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val bookmarks by viewModel.filteredBookmarks.collectAsStateWithLifecycle()
    val totalBookmarksCount by viewModel.totalBookmarksCount.collectAsStateWithLifecycle()
    val selectedFolderId by viewModel.selectedFolderId.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showFavoritesOnly.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val lastBackupAt by viewModel.lastBackupAt.collectAsStateWithLifecycle()

    val isSaveSheetOpen by viewModel.isSaveSheetOpen.collectAsStateWithLifecycle()
    val pendingUrlInput by viewModel.pendingUrlInput.collectAsStateWithLifecycle()
    val pendingParsedTweet by viewModel.pendingParsedTweet.collectAsStateWithLifecycle()
    val isFetchingTweet by viewModel.isFetchingTweet.collectAsStateWithLifecycle()
    val isOfflinePreview by viewModel.isOfflinePreview.collectAsStateWithLifecycle()
    val pendingSelectedFolderId by viewModel.pendingSelectedFolderId.collectAsStateWithLifecycle()
    val pendingUserNote by viewModel.pendingUserNote.collectAsStateWithLifecycle()
    val pendingTags by viewModel.pendingTags.collectAsStateWithLifecycle()

    val refreshingIds by viewModel.refreshingBookmarkIds.collectAsStateWithLifecycle()
    val showCreateFolderDialog by viewModel.showCreateFolderDialog.collectAsStateWithLifecycle()
    val folderToEdit by viewModel.folderToEdit.collectAsStateWithLifecycle()
    val folderToDelete by viewModel.folderToDelete.collectAsStateWithLifecycle()
    val bookmarkToMove by viewModel.bookmarkToMove.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.userFeedbackMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val systemDark = isSystemInDarkTheme()

    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBarSection(
                totalBookmarks = totalBookmarksCount,
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::setSearchQuery,
                activeTab = activeTab,
                onTabSelected = viewModel::setActiveTab,
                themeMode = themeMode,
                onToggleTheme = {
                    val currentlyDark = when (themeMode) {
                        ThemeMode.DARK -> true
                        ThemeMode.LIGHT -> false
                        ThemeMode.SYSTEM -> systemDark
                    }
                    viewModel.setThemeMode(if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK)
                }
            )
        },
        floatingActionButton = {
            if (activeTab == MainTab.BOOKMARKS) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openManualAddSheet() },
                    containerColor = XBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(imageVector = Icons.Filled.Add, contentDescription = null) },
                    text = { Text("حفظ منشور", fontWeight = FontWeight.Bold) }
                )
            } else if (activeTab == MainTab.FOLDERS) {
                FloatingActionButton(
                    onClick = { viewModel.openCreateFolderDialog() },
                    containerColor = XBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CreateNewFolder,
                        contentDescription = "إنشاء مجموعة جديدة"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                MainTab.BOOKMARKS -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        FolderFilterBar(
                            folders = folders,
                            selectedFolderId = selectedFolderId,
                            showFavoritesOnly = showFavoritesOnly,
                            totalBookmarksCount = totalBookmarksCount,
                            onSelectAll = { viewModel.selectFolder(null) },
                            onToggleFavorites = { viewModel.toggleFavoritesOnly() },
                            onSelectFolder = { viewModel.selectFolder(it) },
                            onAddNewFolder = { viewModel.openCreateFolderDialog() }
                        )

                        if (bookmarks.isEmpty()) {
                            EmptyBookmarksView(
                                hasSearch = searchQuery.isNotBlank(),
                                onClearSearch = { viewModel.setSearchQuery("") },
                                onAddFirstBookmark = { viewModel.openManualAddSheet() }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 4.dp,
                                    bottom = 96.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(items = bookmarks, key = { it.id }) { bookmark ->
                                    val folder = folders.firstOrNull { it.folder.id == bookmark.folderId }?.folder
                                    TweetCard(
                                        bookmark = bookmark,
                                        folder = folder,
                                        isRefreshing = bookmark.id in refreshingIds,
                                        onRefreshContent = { viewModel.refreshSingleBookmark(bookmark) },
                                        onToggleFavorite = { viewModel.toggleFavorite(bookmark) },
                                        onMoveToFolder = { viewModel.openMoveBookmarkDialog(bookmark) },
                                        onDelete = { viewModel.deleteBookmark(bookmark.id) },
                                        onFeedback = viewModel::showFeedback
                                    )
                                }
                            }
                        }
                    }
                }

                MainTab.FOLDERS -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            end = 16.dp,
                            top = 16.dp,
                            bottom = 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item(key = "header") {
                            FoldersManagerHeader(
                                totalFolders = folders.size,
                                totalBookmarks = totalBookmarksCount,
                                onCreateFolder = { viewModel.openCreateFolderDialog() }
                            )
                        }

                        items(items = folders, key = { it.folder.id }) { item ->
                            FolderCard(
                                item = item,
                                onClick = { viewModel.selectFolder(item.folder.id) },
                                onEdit = { viewModel.openEditFolderDialog(item.folder) },
                                onDelete = { viewModel.promptDeleteFolder(item) }
                            )
                        }
                    }
                }

                MainTab.SETTINGS -> {
                    SettingsScreen(
                        totalFolders = folders.size,
                        totalBookmarks = totalBookmarksCount,
                        lastBackupAt = lastBackupAt,
                        themeMode = themeMode,
                        onSetThemeMode = viewModel::setThemeMode,
                        onExportBackupJson = viewModel::exportBackupJson,
                        onRestoreBackup = viewModel::restoreBackup,
                        onFeedback = viewModel::showFeedback
                    )
                }
            }
        }
    }

    if (isSaveSheetOpen) {
        SaveBookmarkBottomSheet(
            urlInput = pendingUrlInput,
            onUrlChange = viewModel::onUrlInputChanged,
            parsedTweet = pendingParsedTweet,
            isFetchingTweet = isFetchingTweet,
            isOfflinePreview = isOfflinePreview,
            folders = folders,
            selectedFolderId = pendingSelectedFolderId,
            onSelectFolder = viewModel::setPendingSelectedFolderId,
            userNote = pendingUserNote,
            onUserNoteChange = viewModel::setPendingUserNote,
            tags = pendingTags,
            onTagsChange = viewModel::setPendingTags,
            onOpenCreateFolder = viewModel::openCreateFolderDialog,
            onSave = viewModel::savePendingBookmark,
            onDismiss = viewModel::closeSaveSheet
        )
    }

    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = viewModel::closeCreateFolderDialog,
            onConfirm = viewModel::createFolder
        )
    }

    folderToEdit?.let { folder ->
        EditFolderDialog(
            folder = folder,
            onDismiss = viewModel::closeEditFolderDialog,
            onConfirm = { name, emoji, colorHex, description ->
                viewModel.updateFolder(folder, name, emoji, colorHex, description)
            }
        )
    }

    folderToDelete?.let { item ->
        DeleteFolderDialog(
            folderItem = item,
            onDismiss = viewModel::closeDeleteFolderDialog,
            onConfirm = viewModel::confirmDeleteFolder
        )
    }

    bookmarkToMove?.let { bookmark ->
        MoveBookmarkDialog(
            bookmark = bookmark,
            folders = folders,
            onDismiss = viewModel::closeMoveBookmarkDialog,
            onConfirmMove = viewModel::moveBookmarkToFolder
        )
    }
}

@Composable
private fun FoldersManagerHeader(
    totalFolders: Int,
    totalBookmarks: Int,
    onCreateFolder: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, XBlue.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(XBlue.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FolderSpecial,
                            contentDescription = null,
                            tint = XBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "مجموعات العلامات المرجعية",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$totalFolders مجموعات · $totalBookmarks علامة مرجعية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onCreateFolder,
                    colors = ButtonDefaults.buttonColors(containerColor = XBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مجموعة جديدة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "ميزة مجموعات العلامات المرجعية مستوحاة من ميزة X Premium لتنظيم تغريداتك المفضلة وسهولة الرجوع إليها في أي وقت.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyBookmarksView(
    hasSearch: Boolean,
    onClearSearch: () -> Unit,
    onAddFirstBookmark: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(XBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = null,
                tint = XBlue,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (hasSearch) {
            Text(
                text = "لم يتم العثور على نتائج مطابقة",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "جرب البحث بكلمات أخرى أو مسح نص البحث الحالي",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onClearSearch,
                colors = ButtonDefaults.buttonColors(containerColor = XBlue.copy(alpha = 0.14f), contentColor = XBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("مسح البحث", fontWeight = FontWeight.Bold)
            }
        } else {
            Text(
                text = "لا توجد علامات مرجعية في هذه المجموعة",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "يمكنك حفظ أي تغريدة مباشرة من تطبيق X عبر زر المشاركة (Share) واختيار هذا التطبيق، أو لصق الرابط يدوياً.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onAddFirstBookmark,
                colors = ButtonDefaults.buttonColors(containerColor = XBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("إضافة تغريدة الآن", fontWeight = FontWeight.Bold)
            }
        }
    }
}
