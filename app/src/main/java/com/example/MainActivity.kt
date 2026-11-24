package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BookmarkViewModel
import com.example.ui.MainScreen
import com.example.ui.theme.XFoldersTheme

class MainActivity : ComponentActivity() {
    private val viewModel: BookmarkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                XFoldersTheme(themeMode = themeMode) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPendingBookmarks()
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        if (intent.type != "text/plain") return
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)?.trim() ?: return
        if (sharedText.isNotEmpty()) {
            viewModel.onReceiveSharedText(sharedText)
        }
    }
}
