package io.github.takusan23.androidvideoframefastnextextractor.ui.screen

import android.media.MediaMetadataRetriever
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun MediaMetadataRetrieverScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<ImageBitmap?>(null) }

    val currentPositionMs = remember { mutableStateOf(0L) }
    val mediaMetadataRetriever = remember { MediaMetadataRetriever() }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            // MediaMetadataRetriever は File からも作れます
            uri ?: return@rememberLauncherForActivityResult
            mediaMetadataRetriever.setDataSource(context, uri)
            // 正確なフレームが欲しいので MediaMetadataRetriever.OPTION_CLOSEST
            currentPositionMs.value = 1000
            bitmap.value = mediaMetadataRetriever.getFrameAtTime(currentPositionMs.value * 1_000, MediaMetadataRetriever.OPTION_CLOSEST)?.asImageBitmap()
        }
    )

    // 破棄時
    DisposableEffect(key1 = Unit) {
        onDispose { mediaMetadataRetriever.release() }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        if (bitmap.value != null) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                bitmap = bitmap.value!!,
                contentDescription = null
            )
        }

        Text(text = "currentPositionMs = ${currentPositionMs.value}")

        Button(onClick = { videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }) {
            Text(text = "取り出す")
        }

        Button(onClick = {
            scope.launch {
                currentPositionMs.value += 16
                bitmap.value = mediaMetadataRetriever.getFrameAtTime(currentPositionMs.value * 1_000, MediaMetadataRetriever.OPTION_CLOSEST)?.asImageBitmap()
            }
        }) { Text(text = "16ms 進める") }

    }
}