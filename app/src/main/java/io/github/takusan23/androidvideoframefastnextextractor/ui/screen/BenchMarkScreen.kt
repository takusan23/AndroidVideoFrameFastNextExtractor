package io.github.takusan23.androidvideoframefastnextextractor.ui.screen

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.takusan23.androidvideoframefastnextextractor.VideoFrameBitmapExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

// 0 から 3 秒まで、33 ずつ増やした数字の配列（30fps = 33ms なので）
private val BenchMarkFramePositionMsList = (0 until 3_000L step 33)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenchMarkScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val totalTimeVideoFrameBitmapExtractor = remember { mutableStateOf(0L) }
    val totalTimeMetadataRetriever = remember { mutableStateOf(0L) }

    fun startVideoFrameBitmapExtractorBenchMark(uri: Uri?) {
        uri ?: return
        scope.launch(Dispatchers.Default) {
            totalTimeVideoFrameBitmapExtractor.value = 0
            totalTimeVideoFrameBitmapExtractor.value = measureTimeMillis {
                VideoFrameBitmapExtractor().apply {
                    prepareDecoder(context, uri)
                    BenchMarkFramePositionMsList.forEach { framePositionMs ->
                        println("framePositionMs = $framePositionMs")
                        getVideoFrameBitmap(framePositionMs)
                    }
                }
            }
        }
    }

    fun startMediaMetadataRetrieverBenchMark(uri: Uri?) {
        uri ?: return
        scope.launch(Dispatchers.Default) {
            totalTimeMetadataRetriever.value = 0
            totalTimeMetadataRetriever.value = measureTimeMillis {
                MediaMetadataRetriever().apply {
                    setDataSource(context, uri)
                    BenchMarkFramePositionMsList.forEach { framePositionMs ->
                        println("framePositionMs = $framePositionMs")
                        getFrameAtTime(framePositionMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST)
                    }
                }
            }
        }
    }

    val videoPickerAndVideoFrameBitmapExtractor = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> startVideoFrameBitmapExtractorBenchMark(uri) }
    )

    val videoPickerAndMediaMetadataRetriever = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> startMediaMetadataRetrieverBenchMark(uri) }
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(text = "VideoFrameBitmapExtractor かかった時間 = ${totalTimeVideoFrameBitmapExtractor.value} ms")
        Text(text = "MediaMetadataRetriever かかった時間 = ${totalTimeMetadataRetriever.value} ms")

        Button(onClick = {
            videoPickerAndVideoFrameBitmapExtractor.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }) { Text(text = "VideoFrameBitmapExtractor ベンチマーク") }

        Button(onClick = {
            videoPickerAndMediaMetadataRetriever.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
        }) { Text(text = "MediaMetadataRetriever ベンチマーク") }

    }
}