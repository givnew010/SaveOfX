package com.example.data

import android.content.Context
import androidx.room.withTransaction
import com.example.util.PersonWorkCodec
import com.example.util.PersonWorkItem
import kotlinx.coroutines.flow.Flow

class BookmarkRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val folderDao = database.folderDao()
    private val bookmarkDao = database.bookmarkDao()
    private val accountDao = database.accountDao()
    private val personDao = database.personDao()
    private val prefs by lazy {
        context.applicationContext.getSharedPreferences("xfolders_prefs", Context.MODE_PRIVATE)
    }

    val foldersWithCount: Flow<List<FolderWithCount>> = folderDao.observeFoldersWithCount()
    val allBookmarks: Flow<List<TweetBookmarkEntity>> = bookmarkDao.observeAllBookmarks()
    val allAccounts: Flow<List<AccountBookmarkEntity>> = accountDao.observeAllAccounts()
    val allPeople: Flow<List<PersonEntity>> = personDao.observeAllPeople()

    fun getThemeMode(): ThemeMode {
        return ThemeMode.fromKey(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.key))
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.key).apply()
    }

    fun getLastBackupAt(): String? {
        return prefs.getString(KEY_LAST_BACKUP_AT, null)
    }

    fun markBackupExported(): String {
        val iso = BackupManager.nowIsoTimestamp()
        prefs.edit().putString(KEY_LAST_BACKUP_AT, iso).apply()
        return iso
    }

    suspend fun seedInitialDataIfEmpty() {
        val now = System.currentTimeMillis()
        var seededTechId: Long? = null
        var seededIdeasId: Long? = null
        var seededDefaultId: Long? = null

        if (folderDao.getFoldersCount() == 0) {
            val generalId = folderDao.insertFolder(
                FolderEntity(
                    name = "العلامات المرجعية العامة",
                    iconEmoji = "📌",
                    colorHex = "#1D9BF0",
                    description = "المجموعة الافتراضية لجميع التغريدات والحسابات غير المصنفة",
                    createdAt = now,
                    isDefault = true
                )
            )
            seededDefaultId = generalId
            val techId = folderDao.insertFolder(
                FolderEntity(
                    name = "تقنية وبرمجة",
                    iconEmoji = "💻",
                    colorHex = "#3B82F6",
                    description = "شروحات برمجية، أدوات الذكاء الاصطناعي، ومصادر التطوير",
                    createdAt = now + 1,
                    isDefault = false
                )
            )
            seededTechId = techId
            val ideasId = folderDao.insertFolder(
                FolderEntity(
                    name = "أفكار وإلهام",
                    iconEmoji = "💡",
                    colorHex = "#F59E0B",
                    description = "اقتباسات مميزة، أفكار مشاريع، وتجارب ملهمة",
                    createdAt = now + 2,
                    isDefault = false
                )
            )
            seededIdeasId = ideasId
            val readLaterId = folderDao.insertFolder(
                FolderEntity(
                    name = "للقراءة والاطلاع",
                    iconEmoji = "📚",
                    colorHex = "#10B981",
                    description = "ثريدات طويلة ومقالات تستحق القراءة المتأنية",
                    createdAt = now + 3,
                    isDefault = false
                )
            )
            folderDao.insertFolder(
                FolderEntity(
                    name = "استثمار واقتصاد",
                    iconEmoji = "📈",
                    colorHex = "#8B5CF6",
                    description = "تحليلات مالية، استثمار، واستراتيجيات ريادة الأعمال",
                    createdAt = now + 4,
                    isDefault = false
                )
            )

            if (bookmarkDao.getBookmarksCount() == 0) {
                bookmarkDao.insertBookmark(
                    TweetBookmarkEntity(
                        folderId = techId,
                        tweetUrl = "https://x.com/AndroidDev/status/184512398471923011",
                        tweetId = "184512398471923011",
                        authorName = "Android Developers",
                        authorHandle = "@AndroidDev",
                        content = "Build beautiful, fluid user experiences with Jetpack Compose 🚀 Check out modern navigation and adaptive window size classes in our latest guide.",
                        postedAt = "قبل ساعتين",
                        savedAt = now - 1000,
                        tags = "android,compose,kotlin",
                        userNote = "مرجع مهم جداً لتصميم واجهات قابلة للتكيف على الأجهزة اللوحية والمطوية",
                        isFavorite = true,
                        likesCount = 1420,
                        repostsCount = 385,
                        isContentFetched = true
                    )
                )
                bookmarkDao.insertBookmark(
                    TweetBookmarkEntity(
                        folderId = ideasId,
                        tweetUrl = "https://x.com/Naval/status/1002103360646823936",
                        tweetId = "1002103360646823936",
                        authorName = "Naval",
                        authorHandle = "@naval",
                        content = "How to Get Rich (without getting lucky):\nSeek wealth, not money or status. Wealth is having assets that earn while you sleep. Money is how we transfer time and wealth.",
                        postedAt = "منذ 3 أيام",
                        savedAt = now - 2000,
                        tags = "mindset,wealth,wisdom",
                        userNote = "الثريد الشهير لنافال عن صناعة الثروة والرافعة المالية",
                        isFavorite = true,
                        likesCount = 289000,
                        repostsCount = 68400,
                        isContentFetched = true
                    )
                )
                bookmarkDao.insertBookmark(
                    TweetBookmarkEntity(
                        folderId = readLaterId,
                        tweetUrl = "https://x.com/TechArabic/status/18392019485729104",
                        tweetId = "18392019485729104",
                        authorName = "تقنية بالعربي",
                        authorHandle = "@TechArabic",
                        content = "ثريد شامل: أفضل 10 أدوات مجانية تعمل بالذكاء الاصطناعي لاختصار ساعات العمل اليومية وتلخيص المستندات وإعداد العروض التقديمية 🧵👇",
                        postedAt = "أمس",
                        savedAt = now - 3000,
                        tags = "ذكاء_اصطناعي,أدوات,انتاجية",
                        userNote = "أدوات مفيدة لتلخيص الاجتماعات وكتابة التقارير",
                        isFavorite = false,
                        likesCount = 3450,
                        repostsCount = 920,
                        isContentFetched = true
                    )
                )
            }
        }

        val alreadySeededAccounts = prefs.getBoolean(KEY_ACCOUNTS_SEEDED, false)
        if (!alreadySeededAccounts && accountDao.getAccountsCount() == 0) {
            val allFolders = folderDao.getAllFoldersOnce()
            val defaultId = seededDefaultId
                ?: allFolders.firstOrNull { it.isDefault }?.id
                ?: allFolders.firstOrNull()?.id
                ?: return
            val techFolderId = seededTechId
                ?: allFolders.firstOrNull { it.name.contains("تقنية") }?.id
                ?: defaultId
            val ideasFolderId = seededIdeasId
                ?: allFolders.firstOrNull { it.name.contains("أفكار") }?.id
                ?: defaultId

            accountDao.insertAccounts(
                listOf(
                    AccountBookmarkEntity(
                        folderId = techFolderId,
                        profileUrl = "https://x.com/AndroidDev",
                        handle = "@AndroidDev",
                        displayName = "Android Developers",
                        bio = "News and announcements for Android developers. Building apps with Jetpack Compose, Kotlin, and Material Design.",
                        followersCount = 2150000L,
                        followingCount = 210L,
                        verified = true,
                        userNote = "الحساب الرسمي لمتابعة تحديثات أندرويد و Jetpack Compose",
                        tags = "android,kotlin,compose",
                        isFavorite = true,
                        savedAt = now - 1000L,
                        isProfileFetched = true
                    ),
                    AccountBookmarkEntity(
                        folderId = ideasFolderId,
                        profileUrl = "https://x.com/naval",
                        handle = "@naval",
                        displayName = "Naval",
                        bio = "Co-founder of AngelList. Reading, writing, and thinking about wealth creation, startups, and philosophy.",
                        followersCount = 2480000L,
                        followingCount = 120L,
                        verified = true,
                        userNote = "تأملات مركزة في ريادة الأعمال وبناء الثروة",
                        tags = "فكر,ريادة_أعمال,استثمار",
                        isFavorite = true,
                        savedAt = now - 2000L,
                        isProfileFetched = true
                    ),
                    AccountBookmarkEntity(
                        folderId = techFolderId,
                        profileUrl = "https://x.com/OpenAI",
                        handle = "@OpenAI",
                        displayName = "OpenAI",
                        bio = "OpenAI’s mission is to ensure that artificial general intelligence benefits all of humanity.",
                        followersCount = 3900000L,
                        followingCount = 45L,
                        verified = true,
                        userNote = "متابعة إطلاقات ونماذج الذكاء الاصطناعي أولاً بأول",
                        tags = "ذكاء_اصطناعي,أبحاث,تقنية",
                        isFavorite = false,
                        savedAt = now - 3000L,
                        isProfileFetched = true
                    )
                )
            )
            prefs.edit().putBoolean(KEY_ACCOUNTS_SEEDED, true).apply()
        } else if (accountDao.getAccountsCount() > 0 && !alreadySeededAccounts) {
            prefs.edit().putBoolean(KEY_ACCOUNTS_SEEDED, true).apply()
        }

        val alreadySeededPeople = prefs.getBoolean(KEY_PEOPLE_SEEDED, false)
        if (!alreadySeededPeople && personDao.getPeopleCount() == 0) {
            val allFolders = folderDao.getAllFoldersOnce()
            val defaultId = seededDefaultId
                ?: allFolders.firstOrNull { it.isDefault }?.id
                ?: allFolders.firstOrNull()?.id
                ?: return
            val ideasFolderId = seededIdeasId
                ?: allFolders.firstOrNull { it.name.contains("أفكار") }?.id
                ?: defaultId

            personDao.insertPeople(
                listOf(
                    PersonEntity(
                        folderId = ideasFolderId,
                        name = "كريستوفر نولان",
                        role = "مخرج وكاتب سيناريو",
                        bio = "مخرج بريطاني أمريكي اشتهر بأفلامه ذات السرد غير الخطي والمؤثرات العملية مثل Inception و Interstellar و Oppenheimer.",
                        userNote = "مرجع لأفضل الأعمال السينمائية والمقابلات الإخراجية",
                        tags = "سينما,إخراج,أفلام",
                        worksJson = PersonWorkCodec.serializeWorks(
                            listOf(
                                PersonWorkItem(
                                    id = "seed_work_1",
                                    type = "link",
                                    title = "صفحة الأعمال على IMDb",
                                    url = "https://www.imdb.com/name/nm0634240/",
                                    note = "قائمة الأفلام والجوائز كاملة",
                                    addedAt = now - 900L
                                ),
                                PersonWorkItem(
                                    id = "seed_work_2",
                                    type = "video",
                                    title = "مقابلة حول صناعة Interstellar",
                                    url = "https://www.youtube.com/watch?v=zSWdZVtXT7E",
                                    note = "مقطع تعريفي بالعمل",
                                    addedAt = now - 800L
                                )
                            )
                        ),
                        isFavorite = true,
                        savedAt = now - 1000L
                    ),
                    PersonEntity(
                        folderId = ideasFolderId,
                        name = "أحمد حلمي",
                        role = "ممثل ومنتج",
                        bio = "ممثل مصري بارز قدم العديد من الأعمال الكوميدية والدرامية الناجحة في السينما العربية.",
                        userNote = "متابعة أحدث الأفلام والمشاريع الفنية",
                        tags = "ممثل,سينما_عربية,كوميديا",
                        worksJson = PersonWorkCodec.serializeWorks(
                            listOf(
                                PersonWorkItem(
                                    id = "seed_work_3",
                                    type = "link",
                                    title = "السيرة والأعمال الكاملة",
                                    url = "https://elcinema.com/person/1039784/",
                                    note = "بيانات الأفلام والمسلسلات",
                                    addedAt = now - 1900L
                                )
                            )
                        ),
                        isFavorite = false,
                        savedAt = now - 2000L
                    )
                )
            )
            prefs.edit().putBoolean(KEY_PEOPLE_SEEDED, true).apply()
        } else if (personDao.getPeopleCount() > 0 && !alreadySeededPeople) {
            prefs.edit().putBoolean(KEY_PEOPLE_SEEDED, true).apply()
        }
    }

    suspend fun insertFolder(folder: FolderEntity): Long = folderDao.insertFolder(folder)

    suspend fun updateFolder(folder: FolderEntity) = folderDao.updateFolder(folder)

    suspend fun deleteFolder(folderId: Long, moveToGeneral: Boolean) {
        if (moveToGeneral) {
            val defaultFolder = folderDao.getDefaultFolder()
            if (defaultFolder != null && defaultFolder.id != folderId) {
                bookmarkDao.moveAllBookmarksBetweenFolders(folderId, defaultFolder.id)
                accountDao.moveAllAccountsBetweenFolders(folderId, defaultFolder.id)
                personDao.moveAllPeopleBetweenFolders(folderId, defaultFolder.id)
            }
        } else {
            bookmarkDao.deleteBookmarksByFolder(folderId)
            accountDao.deleteAccountsByFolder(folderId)
            personDao.deletePeopleByFolder(folderId)
        }
        folderDao.deleteFolderById(folderId)
    }

    suspend fun insertBookmark(bookmark: TweetBookmarkEntity): Long = bookmarkDao.insertBookmark(bookmark)

    suspend fun updateBookmark(bookmark: TweetBookmarkEntity) = bookmarkDao.updateBookmark(bookmark)

    suspend fun getUnfetchedBookmarks(): List<TweetBookmarkEntity> = bookmarkDao.getUnfetchedBookmarks()

    suspend fun deleteBookmark(bookmarkId: Long) = bookmarkDao.deleteBookmarkById(bookmarkId)

    suspend fun toggleFavorite(bookmarkId: Long) = bookmarkDao.toggleFavorite(bookmarkId)

    suspend fun moveBookmark(bookmarkId: Long, newFolderId: Long) =
        bookmarkDao.moveBookmark(bookmarkId, newFolderId)

    suspend fun insertAccount(account: AccountBookmarkEntity): Long = accountDao.insertAccount(account)

    suspend fun updateAccount(account: AccountBookmarkEntity) = accountDao.updateAccount(account)

    suspend fun getUnfetchedAccounts(): List<AccountBookmarkEntity> = accountDao.getUnfetchedAccounts()

    suspend fun deleteAccount(accountId: Long) = accountDao.deleteAccountById(accountId)

    suspend fun toggleAccountFavorite(accountId: Long) = accountDao.toggleFavorite(accountId)

    suspend fun moveAccount(accountId: Long, newFolderId: Long) =
        accountDao.moveAccount(accountId, newFolderId)

    suspend fun insertPerson(person: PersonEntity): Long {
        prefs.edit().putBoolean(KEY_PEOPLE_SEEDED, true).apply()
        return personDao.insertPerson(person)
    }

    suspend fun updatePerson(person: PersonEntity) = personDao.updatePerson(person)

    suspend fun deletePerson(personId: Long) = personDao.deletePersonById(personId)

    suspend fun togglePersonFavorite(personId: Long) = personDao.toggleFavorite(personId)

    suspend fun movePerson(personId: Long, newFolderId: Long) =
        personDao.movePerson(personId, newFolderId)

    suspend fun addWorkToPerson(person: PersonEntity, work: PersonWorkItem) {
        val currentWorks = PersonWorkCodec.parseWorks(person.worksJson)
        val updatedWorks = listOf(work) + currentWorks
        personDao.updatePerson(person.copy(worksJson = PersonWorkCodec.serializeWorks(updatedWorks)))
    }

    suspend fun removeWorkFromPerson(person: PersonEntity, workId: String) {
        val currentWorks = PersonWorkCodec.parseWorks(person.worksJson)
        val updatedWorks = currentWorks.filterNot { it.id == workId }
        personDao.updatePerson(person.copy(worksJson = PersonWorkCodec.serializeWorks(updatedWorks)))
    }

    suspend fun exportBackup(): BackupPayload {
        val folders = folderDao.getAllFoldersOnce()
        val bookmarks = bookmarkDao.getAllBookmarksOnce()
        val accounts = accountDao.getAllAccountsOnce()
        val people = personDao.getAllPeopleOnce()
        return BackupManager.buildPayload(folders, bookmarks, accounts, people)
    }

    suspend fun restoreBackup(payload: BackupPayload, mode: RestoreMode): RestoreSummary {
        return database.withTransaction {
            val existingFolders = folderDao.getAllFoldersOnce()
            val existingBookmarks = bookmarkDao.getAllBookmarksOnce()
            val existingAccounts = accountDao.getAllAccountsOnce()
            val existingPeople = personDao.getAllPeopleOnce()
            val plan = BackupManager.planRestore(
                payload = payload,
                mode = mode,
                existingFolders = existingFolders,
                existingBookmarks = existingBookmarks,
                existingAccounts = existingAccounts,
                existingPeople = existingPeople
            )

            if (plan.isReplace) {
                personDao.deleteAllPeople()
                accountDao.deleteAllAccounts()
                bookmarkDao.deleteAllBookmarks()
                folderDao.deleteAllFolders()
                folderDao.insertFolders(plan.foldersToSave)
                bookmarkDao.insertBookmarks(plan.bookmarksToSave)
                accountDao.insertAccounts(plan.accountsToSave)
                personDao.insertPeople(plan.peopleToSave)
                prefs.edit().putBoolean(KEY_PEOPLE_SEEDED, true).apply()
            } else {
                if (plan.foldersToSave.isNotEmpty()) {
                    folderDao.insertFolders(plan.foldersToSave)
                }
                if (plan.bookmarksToSave.isNotEmpty()) {
                    bookmarkDao.insertBookmarks(plan.bookmarksToSave)
                }
                if (plan.accountsToSave.isNotEmpty()) {
                    accountDao.insertAccounts(plan.accountsToSave)
                }
                if (plan.peopleToSave.isNotEmpty()) {
                    personDao.insertPeople(plan.peopleToSave)
                    prefs.edit().putBoolean(KEY_PEOPLE_SEEDED, true).apply()
                }
            }

            RestoreSummary(
                foldersAdded = plan.foldersAddedCount,
                bookmarksAdded = plan.bookmarksAddedCount,
                accountsAdded = plan.accountsAddedCount,
                peopleAdded = plan.peopleAddedCount
            )
        }
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LAST_BACKUP_AT = "last_backup_at"
        private const val KEY_ACCOUNTS_SEEDED = "accounts_seeded"
        private const val KEY_PEOPLE_SEEDED = "people_seeded"
    }
}
