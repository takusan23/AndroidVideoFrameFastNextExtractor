package io.github.takusan23.androidvideoframefastnextextractor

import android.media.MediaMetadataRetriever
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.system.measureTimeMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaMetadataRetrieverScreen() {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val bitmap = remember { mutableStateOf<ImageBitmap?>(null) }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            // MediaMetadataRetriever は File からも作れます
            uri ?: return@rememberLauncherForActivityResult

            val time = measureTimeMillis {
                val mediaMetadataRetriever = MediaMetadataRetriever().apply {
                    setDataSource(context, uri)
                }
                // Bitmap を取り出す
                // 引数の単位は Ms ではなく Us です
                mediaMetadataRetriever.getFrameAtTime(1_000_000L, MediaMetadataRetriever.OPTION_CLOSEST)?.asImageBitmap()
                mediaMetadataRetriever.getFrameAtTime(1_100_000L, MediaMetadataRetriever.OPTION_CLOSEST)?.asImageBitmap()
                mediaMetadataRetriever.getFrameAtTime(1_200_000L, MediaMetadataRetriever.OPTION_CLOSEST)?.asImageBitmap()
                // もう使わないなら
                mediaMetadataRetriever.release()
            }
            println("time = $time ms")
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "MediaMetadataRetrieverScreen") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            if (bitmap.value != null) {
                Image(
                    modifier = Modifier.fillMaxWidth(),
                    bitmap = bitmap.value!!,
                    contentDescription = null
                )
            }

            Button(onClick = { videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) }) {
                Text(text = "取り出す")
            }

        }
    }
}