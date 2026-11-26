package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ThemeMode
import com.example.ui.MainTab
import com.example.ui.theme.XBlue

@Composable
fun TopAppBarSection(
    totalBookmarks: Int,
    totalAccounts: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    activeTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit
) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    val systemDark = isSystemInDarkTheme()
    val isCurrentlyDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }

    val searchPlaceholder = if (activeTab == MainTab.ACCOUNTS) {
        "ابحث بالاسم، المعرف @، النبذة، أو الوسم…"
    } else {
        "ابحث في محتوى التغريدة، اسم الكاتب، الوسم…"
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
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
                            .background(XBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "𝕏",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "مجموعات X",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(XBlue.copy(alpha = 0.14f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Premium",
                                    color = XBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = "$totalBookmarks علامة مرجعية · $totalAccounts حساب",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (isSearchExpanded) {
                            onSearchQueryChange("")
                        }
                        isSearchExpanded = !isSearchExpanded
                    }) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = "بحث",
                            tint = if (isSearchExpanded) XBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isCurrentlyDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = if (isCurrentlyDark) "الوضع النهاري" else "الوضع الليلي",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isSearchExpanded) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = {
                        Text(
                            text = searchPlaceholder,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = XBlue
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "مسح البحث"
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = XBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 10.dp)
                )
            }

            val selectedTabIndex = MainTab.entries.indexOf(activeTab)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = XBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 3.dp,
                        color = XBlue
                    )
                },
                divider = {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
                }
            ) {
                TopTabItem(
                    selected = activeTab == MainTab.BOOKMARKS,
                    label = "العلامات المرجعية",
                    icon = Icons.Filled.Bookmark,
                    onClick = { onTabSelected(MainTab.BOOKMARKS) }
                )
                TopTabItem(
                    selected = activeTab == MainTab.ACCOUNTS,
                    label = "الحسابات",
                    icon = Icons.Filled.Person,
                    onClick = { onTabSelected(MainTab.ACCOUNTS) }
                )
                TopTabItem(
                    selected = activeTab == MainTab.FOLDERS,
                    label = "المجموعات",
                    icon = Icons.Filled.Folder,
                    onClick = { onTabSelected(MainTab.FOLDERS) }
                )
                TopTabItem(
                    selected = activeTab == MainTab.SETTINGS,
                    label = "الإعدادات",
                    icon = Icons.Filled.Settings,
                    onClick = { onTabSelected(MainTab.SETTINGS) }
                )
            }
        }
    }
}

@Composable
private fun TopTabItem(
    selected: Boolean,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Tab(
        selected = selected,
        onClick = onClick,
        selectedContentColor = XBlue,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.height(54.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
