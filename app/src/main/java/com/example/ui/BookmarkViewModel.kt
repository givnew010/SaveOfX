package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AccountBookmarkEntity
import com.example.data.AppDatabase
import com.example.data.BackupManager
import com.example.data.BackupPayload
import com.example.data.BookmarkRepository
import com.example.data.FolderEntity
import com.example.data.FolderWithCount
import com.example.data.RestoreMode
import com.example.data.ThemeMode
import com.example.data.TweetBookmarkEntity
import com.example.util.AccountFetchResult
import com.example.util.AccountFetcher
import com.example.util.AccountParser
import com.example.util.ParsedAccount
import com.example.util.ParsedTweet
import com.example.util.TweetFetchResult
import com.example.util.TweetFetcher
import com.example.util.TweetParser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    BOOKMARKS,
    ACCOUNTS,
    FOLDERS,
    SETTINGS
}

class BookmarkViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val repository = BookmarkRepository(
        context = appContext,
        database = AppDatabase.getInstance(appContext)
    )

    val folders: StateFlow<List<FolderWithCount>> = repository.foldersWithCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allBookmarksFlow: StateFlow<List<TweetBookmarkEntity>> = repository.allBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allAccountsFlow: StateFlow<List<AccountBookmarkEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId: StateFlow<Long?> = _selectedFolderId.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    private val _selectedAccountFolderId = MutableStateFlow<Long?>(null)
    val selectedAccountFolderId: StateFlow<Long?> = _selectedAccountFolderId.asStateFlow()

    private val _showAccountFavoritesOnly = MutableStateFlow(false)
    val showAccountFavoritesOnly: StateFlow<Boolean> = _showAccountFavoritesOnly.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeTab = MutableStateFlow(MainTab.BOOKMARKS)
    val activeTab: StateFlow<MainTab> = _activeTab.asStateFlow()

    private val _themeMode = MutableStateFlow(repository.getThemeMode())
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _lastBackupAt = MutableStateFlow(repository.getLastBackupAt())
    val lastBackupAt: StateFlow<String?> = _lastBackupAt.asStateFlow()

    val totalBookmarksCount: StateFlow<Int> = allBookmarksFlow
        .combine(_searchQuery) { list, _ -> list.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalAccountsCount: StateFlow<Int> = allAccountsFlow
        .combine(_searchQuery) { list, _ -> list.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val filteredBookmarks: StateFlow<List<TweetBookmarkEntity>> = combine(
        allBookmarksFlow,
        _selectedFolderId,
        _showFavoritesOnly,
        _searchQuery
    ) { list, folderId, favOnly, query ->
        val trimmed = query.trim().lowercase()
        list.filter { b ->
            val matchFolder = folderId == null || b.folderId == folderId
            val matchFav = !favOnly || b.isFavorite
            val matchQuery = trimmed.isEmpty() ||
                b.content.lowercase().contains(trimmed) ||
                b.authorName.lowercase().contains(trimmed) ||
                b.authorHandle.lowercase().contains(trimmed) ||
                (b.userNote?.lowercase()?.contains(trimmed) == true) ||
                b.tags.lowercase().contains(trimmed)
            matchFolder && matchFav && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredAccounts: StateFlow<List<AccountBookmarkEntity>> = combine(
        allAccountsFlow,
        _selectedAccountFolderId,
        _showAccountFavoritesOnly,
        _searchQuery
    ) { list, folderId, favOnly, query ->
        val trimmed = query.trim().lowercase()
        list.filter { a ->
            val matchFolder = folderId == null || a.folderId == folderId
            val matchFav = !favOnly || a.isFavorite
            val matchQuery = trimmed.isEmpty() ||
                a.displayName.lowercase().contains(trimmed) ||
                a.handle.lowercase().contains(trimmed) ||
                a.bio.lowercase().contains(trimmed) ||
                (a.userNote?.lowercase()?.contains(trimmed) == true) ||
                a.tags.lowercase().contains(trimmed)
            matchFolder && matchFav && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Save Tweet Bottom Sheet state
    private val _isSaveSheetOpen = MutableStateFlow(false)
    val isSaveSheetOpen: StateFlow<Boolean> = _isSaveSheetOpen.asStateFlow()

    private val _pendingUrlInput = MutableStateFlow("")
    val pendingUrlInput: StateFlow<String> = _pendingUrlInput.asStateFlow()

    private val _pendingParsedTweet = MutableStateFlow<ParsedTweet?>(null)
    val pendingParsedTweet: StateFlow<ParsedTweet?> = _pendingParsedTweet.asStateFlow()

    private val _isFetchingTweet = MutableStateFlow(false)
    val isFetchingTweet: StateFlow<Boolean> = _isFetchingTweet.asStateFlow()

    private val _isOfflinePreview = MutableStateFlow(false)
    val isOfflinePreview: StateFlow<Boolean> = _isOfflinePreview.asStateFlow()

    private val _pendingSelectedFolderId = MutableStateFlow<Long?>(null)
    val pendingSelectedFolderId: StateFlow<Long?> = _pendingSelectedFolderId.asStateFlow()

    private val _pendingUserNote = MutableStateFlow("")
    val pendingUserNote: StateFlow<String> = _pendingUserNote.asStateFlow()

    private val _pendingTags = MutableStateFlow("")
    val pendingTags: StateFlow<String> = _pendingTags.asStateFlow()

    // Save Account Bottom Sheet state
    private val _isSaveAccountSheetOpen = MutableStateFlow(false)
    val isSaveAccountSheetOpen: StateFlow<Boolean> = _isSaveAccountSheetOpen.asStateFlow()

    private val _pendingAccountInput = MutableStateFlow("")
    val pendingAccountInput: StateFlow<String> = _pendingAccountInput.asStateFlow()

    private val _pendingParsedAccount = MutableStateFlow<ParsedAccount?>(null)
    val pendingParsedAccount: StateFlow<ParsedAccount?> = _pendingParsedAccount.asStateFlow()

    private val _isFetchingAccount = MutableStateFlow(false)
    val isFetchingAccount: StateFlow<Boolean> = _isFetchingAccount.asStateFlow()

    private val _isOfflineAccountPreview = MutableStateFlow(false)
    val isOfflineAccountPreview: StateFlow<Boolean> = _isOfflineAccountPreview.asStateFlow()

    private val _pendingAccountFolderId = MutableStateFlow<Long?>(null)
    val pendingAccountFolderId: StateFlow<Long?> = _pendingAccountFolderId.asStateFlow()

    private val _pendingAccountNote = MutableStateFlow("")
    val pendingAccountNote: StateFlow<String> = _pendingAccountNote.asStateFlow()

    private val _pendingAccountTags = MutableStateFlow("")
    val pendingAccountTags: StateFlow<String> = _pendingAccountTags.asStateFlow()

    // Refreshing cards
    private val _refreshingBookmarkIds = MutableStateFlow<Set<Long>>(emptySet())
    val refreshingBookmarkIds: StateFlow<Set<Long>> = _refreshingBookmarkIds.asStateFlow()

    private val _refreshingAccountIds = MutableStateFlow<Set<Long>>(emptySet())
    val refreshingAccountIds: StateFlow<Set<Long>> = _refreshingAccountIds.asStateFlow()

    // Dialogs & feedback
    private val _showCreateFolderDialog = MutableStateFlow(false)
    val showCreateFolderDialog: StateFlow<Boolean> = _showCreateFolderDialog.asStateFlow()

    private val _folderToEdit = MutableStateFlow<FolderEntity?>(null)
    val folderToEdit: StateFlow<FolderEntity?> = _folderToEdit.asStateFlow()

    private val _folderToDelete = MutableStateFlow<FolderWithCount?>(null)
    val folderToDelete: StateFlow<FolderWithCount?> = _folderToDelete.asStateFlow()

    private val _bookmarkToMove = MutableStateFlow<TweetBookmarkEntity?>(null)
    val bookmarkToMove: StateFlow<TweetBookmarkEntity?> = _bookmarkToMove.asStateFlow()

    private val _accountToMove = MutableStateFlow<AccountBookmarkEntity?>(null)
    val accountToMove: StateFlow<AccountBookmarkEntity?> = _accountToMove.asStateFlow()

    private val _userFeedbackMessage = MutableStateFlow<String?>(null)
    val userFeedbackMessage: StateFlow<String?> = _userFeedbackMessage.asStateFlow()

    private var previewFetchJob: Job? = null
    private var accountPreviewFetchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            refreshPendingBookmarks()
            refreshPendingAccounts()
        }
    }

    fun setActiveTab(tab: MainTab) {
        _activeTab.value = tab
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        repository.setThemeMode(mode)
    }

    fun selectFolder(folderId: Long?) {
        _selectedFolderId.value = folderId
        if (folderId != null) {
            _showFavoritesOnly.value = false
            _activeTab.value = MainTab.BOOKMARKS
        }
    }

    fun toggleFavoritesOnly() {
        val next = !_showFavoritesOnly.value
        _showFavoritesOnly.value = next
        if (next) {
            _selectedFolderId.value = null
            _activeTab.value = MainTab.BOOKMARKS
        }
    }

    fun selectAccountFolder(folderId: Long?) {
        _selectedAccountFolderId.value = folderId
        if (folderId != null) {
            _showAccountFavoritesOnly.value = false
            _activeTab.value = MainTab.ACCOUNTS
        }
    }

    fun toggleAccountFavoritesOnly() {
        val next = !_showAccountFavoritesOnly.value
        _showAccountFavoritesOnly.value = next
        if (next) {
            _selectedAccountFolderId.value = null
            _activeTab.value = MainTab.ACCOUNTS
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun showFeedback(message: String) {
        _userFeedbackMessage.value = message
    }

    fun clearFeedbackMessage() {
        _userFeedbackMessage.value = null
    }

    fun onReceiveSharedText(rawText: String) {
        val trimmed = rawText.trim()
        if (!TweetParser.isTweetUrl(trimmed) && AccountParser.isProfileUrl(trimmed)) {
            _activeTab.value = MainTab.ACCOUNTS
            openManualAccountSheet(trimmed)
            return
        }

        val parsed = TweetParser.parseSharedText(rawText)
        _pendingUrlInput.value = parsed.tweetUrl
        _pendingParsedTweet.value = parsed
        _pendingUserNote.value = ""
        _pendingTags.value = ""
        if (_pendingSelectedFolderId.value == null) {
            _pendingSelectedFolderId.value = folders.value.firstOrNull()?.folder?.id
        }
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
        if (_pendingSelectedFolderId.value == null) {
            _pendingSelectedFolderId.value = folders.value.firstOrNull()?.folder?.id
        }
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

        _pendingParsedTweet.value = TweetParser.parseSharedText(newUrl)
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
            if (!immediate) delay(400L)
            _isFetchingTweet.value = true
            _isOfflinePreview.value = false

            when (val result = TweetFetcher.fetchTweet(appContext, rawInput)) {
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
                    _isOfflinePreview.value = !TweetFetcher.isNetworkAvailable(appContext)
                }
            }
            _isFetchingTweet.value = false
        }
    }

    fun setPendingSelectedFolderId(folderId: Long) {
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
        if (rawUrl.isEmpty()) return

        val parsed = _pendingParsedTweet.value ?: TweetParser.parseSharedText(rawUrl)
        val folderId = _pendingSelectedFolderId.value
            ?: folders.value.firstOrNull()?.folder?.id
            ?: 1L
        val serializedMedia = TweetParser.serializeMediaItems(parsed.mediaItems)

        val entity = TweetBookmarkEntity(
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
            userNote = _pendingUserNote.value.trim().takeIf { it.isNotEmpty() },
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
            val insertedId = repository.insertBookmark(entity)
            val saved = entity.copy(id = insertedId)
            closeSaveSheet()
            showFeedback(
                if (saved.isContentFetched) {
                    "تم حفظ التغريدة ومحتواها في المجموعة بنجاح"
                } else {
                    "تم حفظ الرابط (سيتم جلب المحتوى عند توفر الإنترنت)"
                }
            )
            if (!saved.isContentFetched && TweetParser.isTweetUrl(saved.tweetUrl)) {
                refreshSingleBookmark(saved, silent = true)
            }
        }
    }

    // Account Bottom Sheet & CRUD
    fun openManualAccountSheet(initialInput: String = "") {
        val parsed = if (initialInput.isNotBlank()) AccountParser.parseAccountInput(initialInput) else null
        _pendingAccountInput.value = initialInput
        _pendingParsedAccount.value = parsed
        _isFetchingAccount.value = false
        _isOfflineAccountPreview.value = false
        _pendingAccountNote.value = ""
        _pendingAccountTags.value = ""
        if (_pendingAccountFolderId.value == null) {
            _pendingAccountFolderId.value = folders.value.firstOrNull()?.folder?.id
        }
        _isSaveAccountSheetOpen.value = true
        if (parsed != null) {
            fetchAccountPreview(initialInput, immediate = true)
        }
    }

    fun closeSaveAccountSheet() {
        accountPreviewFetchJob?.cancel()
        _isSaveAccountSheetOpen.value = false
        _pendingAccountInput.value = ""
        _pendingParsedAccount.value = null
        _isFetchingAccount.value = false
        _isOfflineAccountPreview.value = false
        _pendingAccountNote.value = ""
        _pendingAccountTags.value = ""
    }

    fun onAccountInputChanged(newInput: String) {
        _pendingAccountInput.value = newInput
        if (newInput.isBlank()) {
            accountPreviewFetchJob?.cancel()
            _pendingParsedAccount.value = null
            _isFetchingAccount.value = false
            _isOfflineAccountPreview.value = false
            return
        }

        val parsed = AccountParser.parseAccountInput(newInput)
        _pendingParsedAccount.value = parsed
        if (parsed != null) {
            fetchAccountPreview(newInput, immediate = false)
        } else {
            accountPreviewFetchJob?.cancel()
            _isFetchingAccount.value = false
            _isOfflineAccountPreview.value = false
        }
    }

    private fun fetchAccountPreview(rawInput: String, immediate: Boolean) {
        accountPreviewFetchJob?.cancel()
        accountPreviewFetchJob = viewModelScope.launch {
            if (!immediate) delay(400L)
            _isFetchingAccount.value = true
            _isOfflineAccountPreview.value = false

            when (val result = AccountFetcher.fetchAccount(appContext, rawInput)) {
                is AccountFetchResult.Success -> {
                    _pendingParsedAccount.value = result.account
                    _isOfflineAccountPreview.value = false
                }
                is AccountFetchResult.Offline -> {
                    _pendingParsedAccount.value = result.fallbackAccount
                    _isOfflineAccountPreview.value = true
                }
                is AccountFetchResult.Failed -> {
                    _pendingParsedAccount.value = result.fallbackAccount
                    _isOfflineAccountPreview.value = !TweetFetcher.isNetworkAvailable(appContext)
                }
            }
            _isFetchingAccount.value = false
        }
    }

    fun setPendingAccountFolderId(folderId: Long) {
        _pendingAccountFolderId.value = folderId
    }

    fun setPendingAccountNote(note: String) {
        _pendingAccountNote.value = note
    }

    fun setPendingAccountTags(tags: String) {
        _pendingAccountTags.value = tags
    }

    fun savePendingAccount() {
        val rawInput = _pendingAccountInput.value.trim()
        if (rawInput.isEmpty()) return

        val parsed = _pendingParsedAccount.value ?: AccountParser.parseAccountInput(rawInput) ?: return
        val folderId = _pendingAccountFolderId.value
            ?: folders.value.firstOrNull()?.folder?.id
            ?: 1L

        val entity = AccountBookmarkEntity(
            folderId = folderId,
            profileUrl = parsed.profileUrl,
            handle = parsed.handle,
            displayName = parsed.displayName,
            avatarUrl = parsed.avatarUrl,
            bio = parsed.bio,
            followersCount = parsed.followersCount,
            followingCount = parsed.followingCount,
            verified = parsed.verified,
            userNote = _pendingAccountNote.value.trim().takeIf { it.isNotEmpty() },
            tags = _pendingAccountTags.value.trim(),
            isFavorite = false,
            isProfileFetched = parsed.isProfileFetched
        )

        viewModelScope.launch {
            val insertedId = repository.insertAccount(entity)
            val saved = entity.copy(id = insertedId)
            closeSaveAccountSheet()
            showFeedback(
                if (saved.isProfileFetched) {
                    "تم حفظ الحساب وبياناته في المجموعة بنجاح"
                } else {
                    "تم حفظ الحساب في المجموعة بنجاح"
                }
            )
            if (!saved.isProfileFetched) {
                refreshSingleAccount(saved, silent = true)
            }
        }
    }

    fun refreshSingleAccount(account: AccountBookmarkEntity, silent: Boolean = false) {
        if (!TweetFetcher.isNetworkAvailable(appContext)) {
            if (!silent) showFeedback("لا يوجد اتصال بالإنترنت")
            return
        }

        viewModelScope.launch {
            _refreshingAccountIds.value = _refreshingAccountIds.value + account.id
            when (val result = AccountFetcher.fetchAccount(appContext, account.profileUrl)) {
                is AccountFetchResult.Success -> {
                    val parsed = result.account
                    val updated = account.copy(
                        profileUrl = parsed.profileUrl,
                        handle = parsed.handle,
                        displayName = parsed.displayName.ifBlank { account.displayName },
                        avatarUrl = parsed.avatarUrl ?: account.avatarUrl,
                        bio = parsed.bio,
                        followersCount = parsed.followersCount,
                        followingCount = parsed.followingCount,
                        verified = parsed.verified,
                        isProfileFetched = true
                    )
                    repository.updateAccount(updated)
                    if (!silent) showFeedback("تم تحديث بيانات الحساب بنجاح")
                }
                is AccountFetchResult.Offline -> {
                    if (!silent) showFeedback("لا يوجد اتصال بالإنترنت")
                }
                is AccountFetchResult.Failed -> {
                    if (!silent) showFeedback("تعذر جلب بيانات الحساب حالياً")
                }
            }
            _refreshingAccountIds.value = _refreshingAccountIds.value - account.id
        }
    }

    fun refreshPendingAccounts() {
        viewModelScope.launch {
            val unfetched = repository.getUnfetchedAccounts()
            if (unfetched.isEmpty()) return@launch
            if (!TweetFetcher.isNetworkAvailable(appContext)) return@launch
            for (account in unfetched) {
                refreshSingleAccount(account, silent = true)
            }
        }
    }

    fun toggleAccountFavorite(account: AccountBookmarkEntity) {
        viewModelScope.launch {
            repository.toggleAccountFavorite(account.id)
        }
    }

    fun openMoveAccountDialog(account: AccountBookmarkEntity) {
        _accountToMove.value = account
    }

    fun closeMoveAccountDialog() {
        _accountToMove.value = null
    }

    fun moveAccountToFolder(newFolderId: Long) {
        val target = _accountToMove.value ?: return
        viewModelScope.launch {
            repository.moveAccount(target.id, newFolderId)
            _accountToMove.value = null
            showFeedback("تم نقل الحساب إلى المجموعة الجديدة")
        }
    }

    fun deleteAccount(accountId: Long) {
        viewModelScope.launch {
            repository.deleteAccount(accountId)
            showFeedback("تم حذف الحساب من المجموعة")
        }
    }

    fun refreshSingleBookmark(bookmark: TweetBookmarkEntity, silent: Boolean = false) {
        if (!TweetParser.isTweetUrl(bookmark.tweetUrl)) {
            if (!silent) showFeedback("الرابط لا يشير إلى منشور X صالح")
            return
        }
        if (!TweetFetcher.isNetworkAvailable(appContext)) {
            if (!silent) showFeedback("لا يوجد اتصال بالإنترنت — يعرض الرابط فقط")
            return
        }

        viewModelScope.launch {
            _refreshingBookmarkIds.value = _refreshingBookmarkIds.value + bookmark.id
            when (val result = TweetFetcher.fetchTweet(appContext, bookmark.tweetUrl)) {
                is TweetFetchResult.Success -> {
                    val parsed = result.tweet
                    val updated = bookmark.copy(
                        tweetUrl = parsed.tweetUrl,
                        tweetId = parsed.tweetId.ifBlank { bookmark.tweetId },
                        authorName = parsed.authorName.ifBlank { bookmark.authorName },
                        authorHandle = parsed.authorHandle.ifBlank { bookmark.authorHandle },
                        authorAvatarUrl = parsed.authorAvatarUrl ?: bookmark.authorAvatarUrl,
                        content = parsed.content,
                        mediaUrl = parsed.mediaUrl,
                        mediaType = parsed.mediaType,
                        mediaJson = TweetParser.serializeMediaItems(parsed.mediaItems),
                        postedAt = parsed.postedAt.ifBlank { bookmark.postedAt },
                        likesCount = parsed.likesCount,
                        repostsCount = parsed.repostsCount,
                        quoteAuthorName = parsed.quoteAuthorName,
                        quoteAuthorHandle = parsed.quoteAuthorHandle,
                        quoteText = parsed.quoteText,
                        isContentFetched = parsed.isContentFetched
                    )
                    repository.updateBookmark(updated)
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

    fun refreshPendingBookmarks() {
        viewModelScope.launch {
            val unfetched = repository.getUnfetchedBookmarks()
            if (unfetched.isEmpty()) return@launch
            val hasInternet = TweetFetcher.isNetworkAvailable(appContext)
            for (bookmark in unfetched) {
                val cleanUrl = TweetParser.extractCleanUrl(bookmark.tweetUrl) ?: bookmark.tweetUrl
                val trimmed = bookmark.content.trim()
                val looksLikeGarbage = trimmed.startsWith("?") ||
                    trimmed.startsWith("&") ||
                    trimmed.startsWith("a=") ||
                    trimmed.startsWith("s=") ||
                    trimmed.startsWith("منشور من منصة")
                val cleaned = if (looksLikeGarbage || bookmark.tweetUrl != cleanUrl) {
                    bookmark.copy(tweetUrl = cleanUrl, content = cleanUrl).also {
                        repository.updateBookmark(it)
                    }
                } else {
                    bookmark
                }
                if (hasInternet && TweetParser.isTweetUrl(cleaned.tweetUrl)) {
                    refreshSingleBookmark(cleaned, silent = true)
                }
            }
        }
    }

    fun toggleFavorite(bookmark: TweetBookmarkEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(bookmark.id)
        }
    }

    fun openMoveBookmarkDialog(bookmark: TweetBookmarkEntity) {
        _bookmarkToMove.value = bookmark
    }

    fun closeMoveBookmarkDialog() {
        _bookmarkToMove.value = null
    }

    fun moveBookmarkToFolder(newFolderId: Long) {
        val target = _bookmarkToMove.value ?: return
        viewModelScope.launch {
            repository.moveBookmark(target.id, newFolderId)
            _bookmarkToMove.value = null
            showFeedback("تم نقل التغريدة إلى المجموعة الجديدة")
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            repository.deleteBookmark(bookmarkId)
            showFeedback("تم حذف العلامة المرجعية")
        }
    }

    fun openCreateFolderDialog() {
        _showCreateFolderDialog.value = true
    }

    fun closeCreateFolderDialog() {
        _showCreateFolderDialog.value = false
    }

    fun createFolder(name: String, emoji: String, colorHex: String, description: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val createdId = repository.insertFolder(
                FolderEntity(
                    name = trimmed,
                    iconEmoji = emoji.trim().ifEmpty { "📁" },
                    colorHex = colorHex.trim().ifEmpty { "#1D9BF0" },
                    description = description.trim(),
                    isDefault = false
                )
            )
            _showCreateFolderDialog.value = false
            _pendingSelectedFolderId.value = createdId
            _pendingAccountFolderId.value = createdId
            showFeedback("تم إنشاء المجموعة بنجاح")
        }
    }

    fun openEditFolderDialog(folder: FolderEntity) {
        _folderToEdit.value = folder
    }

    fun closeEditFolderDialog() {
        _folderToEdit.value = null
    }

    fun updateFolder(folder: FolderEntity, name: String, emoji: String, colorHex: String, description: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.updateFolder(
                folder.copy(
                    name = trimmed,
                    iconEmoji = emoji.trim().ifEmpty { folder.iconEmoji },
                    colorHex = colorHex.trim().ifEmpty { folder.colorHex },
                    description = description.trim()
                )
            )
            _folderToEdit.value = null
            showFeedback("تم تحديث بيانات المجموعة")
        }
    }

    fun promptDeleteFolder(item: FolderWithCount) {
        _folderToDelete.value = item
    }

    fun closeDeleteFolderDialog() {
        _folderToDelete.value = null
    }

    fun confirmDeleteFolder(moveToGeneral: Boolean) {
        val target = _folderToDelete.value ?: return
        viewModelScope.launch {
            repository.deleteFolder(target.folder.id, moveToGeneral)
            if (_selectedFolderId.value == target.folder.id) {
                _selectedFolderId.value = null
            }
            if (_selectedAccountFolderId.value == target.folder.id) {
                _selectedAccountFolderId.value = null
            }
            _folderToDelete.value = null
            showFeedback("تم حذف المجموعة بنجاح")
        }
    }

    suspend fun exportBackupJson(): String {
        val payload = repository.exportBackup()
        val json = BackupManager.serializePayload(payload)
        _lastBackupAt.value = repository.markBackupExported()
        return json
    }

    fun restoreBackup(payload: BackupPayload, mode: RestoreMode) {
        viewModelScope.launch {
            val result = repository.restoreBackup(payload, mode)
            _selectedFolderId.value = null
            _showFavoritesOnly.value = false
            _selectedAccountFolderId.value = null
            _showAccountFavoritesOnly.value = false
            val message = if (mode == RestoreMode.REPLACE) {
                "تمت استعادة ${result.foldersAdded} مجموعة و ${result.bookmarksAdded} علامة مرجعية و ${result.accountsAdded} حساب بنجاح"
            } else {
                "تم دمج ${result.foldersAdded} مجموعة جديدة و ${result.bookmarksAdded} علامة مرجعية جديدة و ${result.accountsAdded} حساب جديد"
            }
            showFeedback(message)
        }
    }
}
