package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BookmarkRepository
import com.example.data.FolderEntity
import com.example.data.FolderWithCount
import com.example.data.TweetBookmarkEntity
import com.example.util.ParsedTweet
import com.example.util.TweetFetchResult
import com.example.util.TweetFetcher
import com.example.util.TweetParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    BOOKMARKS,
    FOLDERS
}

data class BookmarkUiState(
    val folders: List<FolderWithCount> = emptyList(),
    val bookmarks: List<TweetBookmarkEntity> = emptyList(),
    val selectedFolderId: Long? = null,
    val showFavoritesOnly: Boolean = false,
    val searchQuery: String = "",
    val activeTab: MainTab = MainTab.BOOKMARKS,
    val totalBookmarksCount: Int = 0,
    // Add/Save Sheet
    val isSaveSheetOpen: Boolean = false,
    val pendingUrlInput: String = "",
    val pendingParsedTweet: ParsedTweet? = null,
    val isFetchingTweet: Boolean = false,
    val isOfflinePreview: Boolean = false,
    val pendingSelectedFolderId: Long? = null,
    val pendingUserNote: String = "",
    val pendingTags: String = "",
    // Refreshing cards
    val refreshingBookmarkIds: Set<Long> = emptySet(),
    // Folder CRUD Dialogs
    val showCreateFolderDialog: Boolean = false,
    val folderToEdit: FolderEntity? = null,
    val folderToDelete: FolderWithCount? = null,
    val bookmarkToMove: TweetBookmarkEntity? = null,
    val userFeedbackMessage: String? = null
)

class BookmarkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookmarkRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = BookmarkRepository(db.folderDao(), db.bookmarkDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            refreshPendingBookmarks()
        }
    }

    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    private val _showFavoritesOnly = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")
    private val _activeTab = MutableStateFlow(MainTab.BOOKMARKS)

    private val _isSaveSheetOpen = MutableStateFlow(false)
    private val _pendingUrlInput = MutableStateFlow("")
    private val _pendingParsedTweet = MutableStateFlow<ParsedTweet?>(null)
    private val _isFetchingTweet = MutableStateFlow(false)
    private val _isOfflinePreview = MutableStateFlow(false)
    private val _pendingSelectedFolderId = MutableStateFlow<Long?>(null)
    private val _pendingUserNote = MutableStateFlow("")
    private val _pendingTags = MutableStateFlow("")

    private val _refreshingBookmarkIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _showCreateFolderDialog = MutableStateFlow(false)
    private val _folderToEdit = MutableStateFlow<FolderEntity?>(null)
    private val _folderToDelete = MutableStateFlow<FolderWithCount?>(null)
    private val _bookmarkToMove = MutableStateFlow<TweetBookmarkEntity?>(null)
    private val _userFeedbackMessage = MutableStateFlow<String?>(null)

    private var previewFetchJob: Job? = null

    val uiState: StateFlow<BookmarkUiState> = combine(
        repository.foldersWithCount,
        repository.allBookmarks,
        _selectedFolderId,
        _showFavoritesOnly,
        _searchQuery,
        _activeTab,
        _isSaveSheetOpen,
        _pendingUrlInput,
        _pendingParsedTweet,
        _isFetchingTweet,
        _isOfflinePreview,
        _pendingSelectedFolderId,
        _pendingUserNote,
        _pendingTags,
        _refreshingBookmarkIds,
        _showCreateFolderDialog,
        _folderToEdit,
        _folderToDelete,
        _bookmarkToMove,
        _userFeedbackMessage
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val folders = args[0] as List<FolderWithCount>
        @Suppress("UNCHECKED_CAST")
        val rawBookmarks = args[1] as List<TweetBookmarkEntity>
        val selectedFolderId = args[2] as? Long
        val showFavoritesOnly = args[3] as Boolean
        val query = (args[4] as String).trim()
        val activeTab = args[5] as MainTab

        val isSaveSheetOpen = args[6] as Boolean
        val pendingUrlInput = args[7] as String
        val pendingParsedTweet = args[8] as? ParsedTweet
        val isFetchingTweet = args[9] as Boolean
        val isOfflinePreview = args[10] as Boolean
        val pendingSelectedFolderId = args[11] as? Long
        val pendingUserNote = args[12] as String
        val pendingTags = args[13] as String

        @Suppress("UNCHECKED_CAST")
        val refreshingIds = args[14] as Set<Long>
        val showCreateFolderDialog = args[15] as Boolean
        val folderToEdit = args[16] as? FolderEntity
        val folderToDelete = args[17] as? FolderWithCount
        val bookmarkToMove = args[18] as? TweetBookmarkEntity
        val feedback = args[19] as? String

        val filteredBookmarks = rawBookmarks.filter { b ->
            val matchFolder = selectedFolderId == null || b.folderId == selectedFolderId
            val matchFav = !showFavoritesOnly || b.isFavorite
            val matchQuery = query.isEmpty() ||
                b.content.contains(query, ignoreCase = true) ||
                b.authorName.contains(query, ignoreCase = true) ||
                b.authorHandle.contains(query, ignoreCase = true) ||
                (b.userNote?.contains(query, ignoreCase = true) == true) ||
                b.tags.contains(query, ignoreCase = true)

            matchFolder && matchFav && matchQuery
        }

        BookmarkUiState(
            folders = folders,
            bookmarks = filteredBookmarks,
            selectedFolderId = selectedFolderId,
            showFavoritesOnly = showFavoritesOnly,
            searchQuery = query,
            activeTab = activeTab,
            totalBookmarksCount = rawBookmarks.size,
            isSaveSheetOpen = isSaveSheetOpen,
            pendingUrlInput = pendingUrlInput,
            pendingParsedTweet = pendingParsedTweet,
            isFetchingTweet = isFetchingTweet,
            isOfflinePreview = isOfflinePreview,
            pendingSelectedFolderId = pendingSelectedFolderId ?: folders.firstOrNull()?.folder?.id,
            pendingUserNote = pendingUserNote,
            pendingTags = pendingTags,
            refreshingBookmarkIds = refreshingIds,
            showCreateFolderDialog = showCreateFolderDialog,
            folderToEdit = folderToEdit,
            folderToDelete = folderToDelete,
            bookmarkToMove = bookmarkToMove,
            userFeedbackMessage = feedback
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BookmarkUiState()
    )

    fun setActiveTab(tab: MainTab) {
        _activeTab.value = tab
    }

    fun selectFolder(folderId: Long?) {
        _selectedFolderId.value = folderId
        if (folderId != null) {
            _showFavoritesOnly.value = false
            _activeTab.value = MainTab.BOOKMARKS
        }
    }

    fun toggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
        if (_showFavoritesOnly.value) {
            _selectedFolderId.value = null
            _activeTab.value = MainTab.BOOKMARKS
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Handles tweet shared via Android Intent ACTION_SEND.
     * Cleans URL immediately and fetches full tweet content (text, images, video) when online.
     */
    fun onReceiveSharedText(rawText: String) {
        val parsed = TweetParser.parseSharedText(rawText)
        _pendingUrlInput.value = parsed.tweetUrl
        _pendingParsedTweet.value = parsed
        _pendingUserNote.value = ""
        _pendingTags.value = ""
        _isSaveSheetOpen.value = true
        fetchPreviewForInput(rawText, immediate = true)
    }

    fun openManualAddSheet(initialUrl: String = "") {
        _pendingUrlInput.value = initialUrl
        _pendingParsedTweet.value = if (initialUrl.isNotBlank()) TweetParser.parseSharedText(initialUrl) else null
        _isFetchingTweet.value = false
        _isOfflinePreview.value = false
        _pendingUserNote.value = ""
        _pendingTags.value = ""
        _isSaveSheetOpen.value = true
        if (initialUrl.isNotBlank() && TweetParser.isTweetUrl(initialUrl)) {
            fetchPreviewForInput(initialUrl, immediate = true)
        }
    }

    fun closeSaveSheet() {
        previewFetchJob?.cancel()
        _isSaveSheetOpen.value = false
        _pendingParsedTweet.value = null
        _isFetchingTweet.value = false
        _isOfflinePreview.value = false
        _pendingUrlInput.value = ""
        _pendingUserNote.value = ""
        _pendingTags.value = ""
    }

    fun onUrlInputChanged(newUrl: String) {
        _pendingUrlInput.value = newUrl
        if (newUrl.isBlank()) {
            previewFetchJob?.cancel()
            _pendingParsedTweet.value = null
            _isFetchingTweet.value = false
            _isOfflinePreview.value = false
            return
        }

        val baseline = TweetParser.parseSharedText(newUrl)
        _pendingParsedTweet.value = baseline

        if (TweetParser.isTweetUrl(newUrl)) {
            fetchPreviewForInput(newUrl, immediate = false)
        } else {
            previewFetchJob?.cancel()
            _isFetchingTweet.value = false
            _isOfflinePreview.value = false
        }
    }

    private fun fetchPreviewForInput(rawInput: String, immediate: Boolean) {
        previewFetchJob?.cancel()
        previewFetchJob = viewModelScope.launch {
            if (!immediate) {
                delay(400)
            }
            _isFetchingTweet.value = true
            _isOfflinePreview.value = false

            when (val result = TweetFetcher.fetchTweet(getApplication(), rawInput)) {
                is TweetFetchResult.Success -> {
                    _pendingParsedTweet.value = result.tweet
                    _pendingUrlInput.value = result.tweet.tweetUrl
                    _isOfflinePreview.value = false
                }
                is TweetFetchResult.Offline -> {
                    _pendingParsedTweet.value = result.fallbackTweet
                    _pendingUrlInput.value = result.fallbackTweet.tweetUrl
                    _isOfflinePreview.value = true
                }
                is TweetFetchResult.Failed -> {
                    _pendingParsedTweet.value = result.fallbackTweet
                    _pendingUrlInput.value = result.fallbackTweet.tweetUrl
                    _isOfflinePreview.value = !TweetFetcher.isNetworkAvailable(getApplication())
                }
            }
            _isFetchingTweet.value = false
        }
    }

    fun setPendingFolderId(folderId: Long) {
        _pendingSelectedFolderId.value = folderId
    }

    fun setPendingUserNote(note: String) {
        _pendingUserNote.value = note
    }

    fun setPendingTags(tags: String) {
        _pendingTags.value = tags
    }

    fun savePendingBookmark() {
        val rawUrl = _pendingUrlInput.value.trim()
        if (rawUrl.isBlank()) return

        val parsed = _pendingParsedTweet.value ?: TweetParser.parseSharedText(rawUrl)
        val folderId = _pendingSelectedFolderId.value
            ?: uiState.value.folders.firstOrNull()?.folder?.id
            ?: 1L

        val serializedMedia = TweetParser.serializeMediaItems(parsed.mediaItems)

        val entityToSave = TweetBookmarkEntity(
            folderId = folderId,
            tweetUrl = parsed.tweetUrl,
            tweetId = parsed.tweetId,
            authorName = parsed.authorName,
            authorHandle = parsed.authorHandle,
            authorAvatarUrl = parsed.authorAvatarUrl,
            content = parsed.content,
            mediaUrl = parsed.mediaUrl,
            mediaType = parsed.mediaType,
            mediaJson = serializedMedia,
            postedAt = parsed.postedAt,
            userNote = _pendingUserNote.value.trim().ifEmpty { null },
            tags = _pendingTags.value.trim(),
            isFavorite = false,
            likesCount = parsed.likesCount,
            repostsCount = parsed.repostsCount,
            quoteAuthorName = parsed.quoteAuthorName,
            quoteAuthorHandle = parsed.quoteAuthorHandle,
            quoteText = parsed.quoteText,
            isContentFetched = parsed.isContentFetched
        )

        viewModelScope.launch {
            val insertedId = repository.insertBookmark(entityToSave)
            closeSaveSheet()
            _userFeedbackMessage.value = if (entityToSave.isContentFetched) {
                "تم حفظ التغريدة ومحتواها في المجلد بنجاح"
            } else {
                "تم حفظ الرابط (سيتم جلب المحتوى عند توفر الإنترنت)"
            }

            // If saved before fetch completed and network is available, fetch & update in background
            if (!entityToSave.isContentFetched && TweetParser.isTweetUrl(entityToSave.tweetUrl)) {
                val savedEntity = entityToSave.copy(id = insertedId)
                refreshSingleBookmark(savedEntity, silent = true)
            }
        }
    }

    /**
     * Automatically cleans up any legacy unfetched bookmarks and fetches full content when online.
     */
    fun refreshPendingBookmarks() {
        viewModelScope.launch {
            val unfetched = repository.getUnfetchedBookmarks()
            if (unfetched.isEmpty()) return@launch

            val hasInternet = TweetFetcher.isNetworkAvailable(getApplication())

            for (bookmark in unfetched) {
                // Clean any legacy query-param garbage content (like ?s=20&a=...) immediately
                val cleanUrl = TweetParser.extractCleanUrl(bookmark.tweetUrl) ?: bookmark.tweetUrl
                val trimmedContent = bookmark.content.trim()
                val looksLikeGarbage = trimmedContent.startsWith("?") ||
                    trimmedContent.startsWith("&") ||
                    trimmedContent.startsWith("a=") ||
                    trimmedContent.startsWith("s=") ||
                    trimmedContent.startsWith("منشور من منصة")

                val cleanedBookmark = if (looksLikeGarbage || bookmark.tweetUrl != cleanUrl) {
                    val fixed = bookmark.copy(tweetUrl = cleanUrl, content = cleanUrl)
                    repository.updateBookmark(fixed)
                    fixed
                } else {
                    bookmark
                }

                if (hasInternet && TweetParser.isTweetUrl(cleanedBookmark.tweetUrl)) {
                    refreshSingleBookmark(cleanedBookmark, silent = true)
                }
            }
        }
    }

    fun refreshSingleBookmark(bookmark: TweetBookmarkEntity, silent: Boolean = false) {
        if (!TweetParser.isTweetUrl(bookmark.tweetUrl)) {
            if (!silent) showFeedback("الرابط لا يشير إلى منشور X صالح")
            return
        }

        if (!TweetFetcher.isNetworkAvailable(getApplication())) {
            if (!silent) showFeedback("لا يوجد اتصال بالإنترنت — يعرض الرابط فقط")
            return
        }

        viewModelScope.launch {
            _refreshingBookmarkIds.value = _refreshingBookmarkIds.value + bookmark.id
            when (val result = TweetFetcher.fetchTweet(getApplication(), bookmark.tweetUrl)) {
                is TweetFetchResult.Success -> {
                    repository.updateBookmarkWithFetchedTweet(bookmark, result.tweet)
                    if (!silent) showFeedback("تم تحديث محتوى التغريدة بنجاح")
                }
                is TweetFetchResult.Offline -> {
                    if (!silent) showFeedback("لا يوجد اتصال بالإنترنت")
                }
                is TweetFetchResult.Failed -> {
                    if (!silent) showFeedback("تعذر جلب محتوى التغريدة حالياً")
                }
            }
            _refreshingBookmarkIds.value = _refreshingBookmarkIds.value - bookmark.id
        }
    }

    fun showFeedback(message: String) {
        _userFeedbackMessage.value = message
    }

    fun toggleFavorite(bookmark: TweetBookmarkEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(bookmark.id, bookmark.isFavorite)
        }
    }

    fun openMoveBookmarkDialog(bookmark: TweetBookmarkEntity) {
        _bookmarkToMove.value = bookmark
    }

    fun closeMoveBookmarkDialog() {
        _bookmarkToMove.value = null
    }

    fun moveBookmarkToFolder(newFolderId: Long) {
        val bookmark = _bookmarkToMove.value ?: return
        viewModelScope.launch {
            repository.moveBookmark(bookmark.id, newFolderId)
            closeMoveBookmarkDialog()
            _userFeedbackMessage.value = "تم نقل التغريدة إلى المجلد الجديد"
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmarkId)
            _userFeedbackMessage.value = "تم حذف العلامة المرجعية"
        }
    }

    // Folder Management
    fun openCreateFolderDialog() {
        _showCreateFolderDialog.value = true
    }

    fun closeCreateFolderDialog() {
        _showCreateFolderDialog.value = false
    }

    fun openEditFolderDialog(folder: FolderEntity) {
        _folderToEdit.value = folder
    }

    fun closeEditFolderDialog() {
        _folderToEdit.value = null
    }

    fun createFolder(name: String, emoji: String, colorHex: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newId = repository.insertFolder(
                FolderEntity(
                    name = name.trim(),
                    iconEmoji = emoji.ifBlank { "📁" },
                    colorHex = colorHex.ifBlank { "#1D9BF0" },
                    description = description.trim(),
                    isDefault = false
                )
            )
            closeCreateFolderDialog()
            _pendingSelectedFolderId.value = newId
            _userFeedbackMessage.value = "تم إنشاء المجلد بنجاح"
        }
    }

    fun updateFolder(folder: FolderEntity, name: String, emoji: String, colorHex: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.updateFolder(
                folder.copy(
                    name = name.trim(),
                    iconEmoji = emoji.ifBlank { folder.iconEmoji },
                    colorHex = colorHex.ifBlank { folder.colorHex },
                    description = description.trim()
                )
            )
            closeEditFolderDialog()
            _userFeedbackMessage.value = "تم تحديث بيانات المجلد"
        }
    }

    fun promptDeleteFolder(folderWithCount: FolderWithCount) {
        _folderToDelete.value = folderWithCount
    }

    fun closeDeleteFolderDialog() {
        _folderToDelete.value = null
    }

    fun confirmDeleteFolder(moveToGeneral: Boolean) {
        val folder = _folderToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteFolder(folder.folder.id, moveToGeneral)
            if (_selectedFolderId.value == folder.folder.id) {
                _selectedFolderId.value = null
            }
            closeDeleteFolderDialog()
            _userFeedbackMessage.value = "تم حذف المجلد بنجاح"
        }
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }
}
