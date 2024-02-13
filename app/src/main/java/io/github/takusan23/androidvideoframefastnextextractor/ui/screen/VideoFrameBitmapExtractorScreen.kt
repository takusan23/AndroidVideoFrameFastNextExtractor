package io.github.takusan23.androidvideoframefastnextextractor.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import io.github.takusan23.androidvideoframefastnextextractor.VideoFrameBitmapExtractor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoFrameBitmapExtractorScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<ImageBitmap?>(null) }

    // フレームを取り出すやつと取り出した位置
    val currentPositionMs = remember { mutableStateOf(0L) }
    val videoFrameBitmapExtractor = remember { VideoFrameBitmapExtractor() }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri ?: return@rememberLauncherForActivityResult
            scope.launch {
                videoFrameBitmapExtractor.prepareDecoder(context, uri)
                currentPositionMs.value = 1000
                bitmap.value = videoFrameBitmapExtractor.getVideoFrameBitmap(currentPositionMs.value).asImageBitmap()
            }
        }
    )

    // 破棄時
    DisposableEffect(key1 = Unit) {
        onDispose { videoFrameBitmapExtractor.destroy() }
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

        Button(onClick = {
            videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }) { Text(text = "取り出す") }

        Button(onClick = {
            scope.launch {
                currentPositionMs.value += 16
                bitmap.value = videoFrameBitmapExtractor.getVideoFrameBitmap(currentPositionMs.value).asImageBitmap()
            }
        }) { Text(text = "16ms 進める") }

    }

}