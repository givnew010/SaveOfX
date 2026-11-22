package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CreateFolderDialog
import com.example.ui.components.DeleteFolderDialog
import com.example.ui.components.EditFolderDialog
import com.example.ui.components.FolderCard
import com.example.ui.components.FolderFilterBar
import com.example.ui.components.MoveBookmarkDialog
import com.example.ui.components.SaveBookmarkBottomSheet
import com.example.ui.components.TopAppBarSection
import com.example.ui.components.TweetCard
import com.example.ui.theme.XBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: BookmarkViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle feedback notifications
    LaunchedEffect(uiState.userFeedbackMessage) {
        uiState.userFeedbackMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedbackMessage()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarSection(
                totalBookmarks = uiState.totalBookmarksCount,
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::setSearchQuery,
                activeTab = uiState.activeTab,
                onTabSelected = viewModel::setActiveTab
            )
        },
        floatingActionButton = {
            when (uiState.activeTab) {
                MainTab.BOOKMARKS -> {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openManualAddSheet() },
                        containerColor = XBlue,
                        contentColor = Color.White,
                        icon = {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        },
                        text = {
                            Text(
                                text = "حفظ منشور",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("fab_add_bookmark")
                    )
                }
                MainTab.FOLDERS -> {
                    FloatingActionButton(
                        onClick = { viewModel.openCreateFolderDialog() },
                        containerColor = XBlue,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("fab_add_folder")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "إنشاء مجلد جديد"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.activeTab) {
                MainTab.BOOKMARKS -> {
                    // Filter Chips Bar
                    FolderFilterBar(
                        folders = uiState.folders,
                        selectedFolderId = uiState.selectedFolderId,
                        showFavoritesOnly = uiState.showFavoritesOnly,
                        totalBookmarksCount = uiState.totalBookmarksCount,
                        onSelectAll = { viewModel.selectFolder(null) },
                        onToggleFavorites = { viewModel.toggleFavoritesOnly() },
                        onSelectFolder = { viewModel.selectFolder(it) },
                        onAddNewFolder = { viewModel.openCreateFolderDialog() }
                    )

                    // Bookmarks List or Empty State
                    if (uiState.bookmarks.isEmpty()) {
                        EmptyBookmarksView(
                            hasSearch = uiState.searchQuery.isNotEmpty(),
                            onClearSearch = { viewModel.setSearchQuery("") },
                            onAddFirstBookmark = { viewModel.openManualAddSheet() }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("bookmarks_lazy_column"),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.bookmarks,
                                key = { it.id }
                            ) { bookmark ->
                                val folder = uiState.folders.firstOrNull { it.folder.id == bookmark.folderId }?.folder
                                TweetCard(
                                    bookmark = bookmark,
                                    folder = folder,
                                    isRefreshing = bookmark.id in uiState.refreshingBookmarkIds,
                                    onRefreshContent = { viewModel.refreshSingleBookmark(bookmark) },
                                    onToggleFavorite = { viewModel.toggleFavorite(bookmark) },
                                    onMoveToFolder = { viewModel.openMoveBookmarkDialog(bookmark) },
                                    onDelete = { viewModel.deleteBookmark(bookmark.id) },
                                    onFeedback = { msg -> viewModel.showFeedback(msg) }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(64.dp))
                            }
                        }
                    }
                }

                MainTab.FOLDERS -> {
                    // Folders Manager View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("folders_lazy_column"),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Premium Banner Header
                        item {
                            FoldersManagerHeader(
                                totalFolders = uiState.folders.size,
                                totalBookmarks = uiState.totalBookmarksCount,
                                onCreateFolder = { viewModel.openCreateFolderDialog() }
                            )
                        }

                        items(
                            items = uiState.folders,
                            key = { it.folder.id }
                        ) { item ->
                            FolderCard(
                                item = item,
                                onClick = {
                                    viewModel.selectFolder(item.folder.id)
                                },
                                onEdit = {
                                    viewModel.openEditFolderDialog(item.folder)
                                },
                                onDelete = {
                                    viewModel.promptDeleteFolder(item)
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(64.dp))
                        }
                    }
                }
            }
        }
    }

    // Save Bookmark Bottom Sheet
    if (uiState.isSaveSheetOpen) {
        SaveBookmarkBottomSheet(
            sheetState = sheetState,
            urlInput = uiState.pendingUrlInput,
            onUrlChange = viewModel::onUrlInputChanged,
            parsedTweet = uiState.pendingParsedTweet,
            isFetchingTweet = uiState.isFetchingTweet,
            isOfflinePreview = uiState.isOfflinePreview,
            folders = uiState.folders,
            selectedFolderId = uiState.pendingSelectedFolderId,
            onSelectFolder = viewModel::setPendingFolderId,
            userNote = uiState.pendingUserNote,
            onUserNoteChange = viewModel::setPendingUserNote,
            tags = uiState.pendingTags,
            onTagsChange = viewModel::setPendingTags,
            onOpenCreateFolder = viewModel::openCreateFolderDialog,
            onSave = viewModel::savePendingBookmark,
            onDismiss = viewModel::closeSaveSheet
        )
    }

    // Dialogs
    if (uiState.showCreateFolderDialog) {
        CreateFolderDialog(
            onDismiss = viewModel::closeCreateFolderDialog,
            onConfirm = viewModel::createFolder
        )
    }

    uiState.folderToEdit?.let { folder ->
        EditFolderDialog(
            folder = folder,
            onDismiss = viewModel::closeEditFolderDialog,
            onConfirm = { name, emoji, color, desc ->
                viewModel.updateFolder(folder, name, emoji, color, desc)
            }
        )
    }

    uiState.folderToDelete?.let { folderItem ->
        DeleteFolderDialog(
            folderItem = folderItem,
            onDismiss = viewModel::closeDeleteFolderDialog,
            onConfirm = viewModel::confirmDeleteFolder
        )
    }

    uiState.bookmarkToMove?.let { bookmark ->
        MoveBookmarkDialog(
            bookmark = bookmark,
            folders = uiState.folders,
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 1.dp,
            brush = Brush.linearGradient(
                listOf(XBlue.copy(alpha = 0.5f), Color(0xFF8B5CF6).copy(alpha = 0.5f))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(XBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderSpecial,
                            contentDescription = null,
                            tint = XBlue
                        )
                    }

                    Column {
                        Text(
                            text = "مجموعات العلامات المرجعية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$totalFolders مجلدات • $totalBookmarks علامة مرجعية",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(XBlue)
                        .clickable { onCreateFolder() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "مجلد جديد",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "ميزة مجلدات العلامات المرجعية مستوحاة من ميزة X Premium لتنظيم تغريداتك المفضلة وسهولة الرجوع إليها في أي وقت.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(XBlue.copy(alpha = 0.12f))
                    .border(2.dp, XBlue.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = null,
                    tint = XBlue,
                    modifier = Modifier.size(40.dp)
                )
            }

            if (hasSearch) {
                Text(
                    text = "لم يتم العثور على نتائج مطابقة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "جرب البحث بكلمات أخرى أو مسح نص البحث الحالي",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(XBlue.copy(alpha = 0.15f))
                        .clickable { onClearSearch() }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "مسح البحث",
                        color = XBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Text(
                    text = "لا توجد علامات مرجعية في هذا المجلد",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "يمكنك حفظ أي تغريدة مباشرة من تطبيق 𝕏 عبر النقر على زر المشاركة واختيار \"X Folders\"، أو إضافة رابط التغريدة يدوياً.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(XBlue)
                        .clickable { onAddFirstBookmark() }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = "إضافة تغريدة الآن",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
