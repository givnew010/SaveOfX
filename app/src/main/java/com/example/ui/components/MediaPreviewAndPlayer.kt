package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import com.example.ui.theme.XBlue
import com.example.ui.theme.XDarkBackground
import com.example.util.TweetMediaItem

import java.util.Locale

private fun formatDuration(seconds: Double?): String? {
    if (seconds == null || seconds <= 0.0) return null
    val total = seconds.toInt()
    val mins = total / 60
    val secs = total % 60
    return String.format(Locale.US, "%d:%02d", mins, secs)
}

@Composable
fun TweetMediaGallery(
    mediaItems: List<TweetMediaItem>,
    tweetUrl: String
) {
    if (mediaItems.isEmpty()) return

    var activePhotoUrl by remember { mutableStateOf<String?>(null) }
    var activeVideoItem by remember { mutableStateOf<TweetMediaItem?>(null) }

    val photos = mediaItems.filter { it.type == "photo" }
    val videos = mediaItems.filter { it.type == "video" || it.type == "gif" }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        videos.forEach { video ->
            VideoThumbnailBox(
                item = video,
                onClick = { activeVideoItem = video }
            )
        }

        when (photos.size) {
            1 -> {
                PhotoBox(
                    url = photos[0].url,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp, max = 280.dp)
                        .aspectRatio(16f / 9f),
                    onClick = { activePhotoUrl = photos[0].url }
                )
            }
            2 -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    photos.forEach { photo ->
                        PhotoBox(
                            url = photo.url,
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            onClick = { activePhotoUrl = photo.url }
                        )
                    }
                }
            }
            in 3..Int.MAX_VALUE -> {
                val capped = photos.take(4)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    capped.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowItems.forEach { photo ->
                                PhotoBox(
                                    url = photo.url,
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(6f / 5f),
                                    onClick = { activePhotoUrl = photo.url }
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }

    activePhotoUrl?.let { photoUrl ->
        FullScreenImageDialog(
            imageUrl = photoUrl,
            onDismiss = { activePhotoUrl = null }
        )
    }

    activeVideoItem?.let { videoItem ->
        InAppVideoPlayerDialog(
            videoItem = videoItem,
            tweetUrl = tweetUrl,
            onDismiss = { activeVideoItem = null }
        )
    }
}

@Composable
private fun PhotoBox(
    url: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier
    ) {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = "صورة مرفقة بالتغريدة",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            error = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.BrokenImage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تعذر تحميل الصورة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

@Composable
private fun VideoThumbnailBox(
    item: TweetMediaItem,
    onClick: () -> Unit
) {
    val durationLabel = formatDuration(item.durationSeconds)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = XDarkBackground,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!item.thumbnailUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = "معاينة الفيديو",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Videocam,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.30f))
            )

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(XBlue.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "تشغيل الفيديو",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (item.type == "gif") "GIF" else "فيديو",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (durationLabel != null) {
                    Text(
                        text = durationLabel,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun FullScreenImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable(onClick = onDismiss)
        ) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = "عرض الصورة",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "إغلاق",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun InAppVideoPlayerDialog(
    videoItem: TweetMediaItem,
    tweetUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasPlaybackError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
        ) {
            if (!hasPlaybackError) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            val controller = MediaController(ctx)
                            controller.setAnchorView(this)
                            setMediaController(controller)
                            setVideoURI(Uri.parse(videoItem.url))
                            setOnPreparedListener { mp ->
                                mp.isLooping = videoItem.type == "gif"
                                start()
                            }
                            setOnErrorListener { _, _, _ ->
                                hasPlaybackError = true
                                true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                )
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Videocam,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "تعذر تشغيل هذا الفيديو مباشرة داخل التطبيق",
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = XBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.OpenInNew, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاهدة في X", fontWeight = FontWeight.Bold)
                    }
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "إغلاق الفيديو",
                    tint = Color.White
                )
            }
        }
    }
}
