package io.github.takusan23.androidvideoframefastnextextractor.ui.screen

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.contentValuesOf
import coil.compose.AsyncImage
import io.github.takusan23.androidvideoframefastnextextractor.VideoFrameBitmapExtractor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis

/**
 * @param isUseVideoFrameBitmapExtractor 自前で作った[VideoFrameBitmapExtractor]を使って取り出す場合は true。[MediaMetadataRetriever]を使う場合は false。
 * @param startMs フレームの取り出しを開始する位置
 * @param stopMs フレームの取り出しを終了する位置
 * @param frameRate フレームレート。1fpsなら1秒に1枚。30fpsなら1秒に30枚
 */
private data class ExtractConfig(
    val isUseVideoFrameBitmapExtractor: Boolean = true,
    val startMs: Long = 0,
    val stopMs: Long = 3_000,
    val frameRate: Int = 15
)

/**
 * 取り出し結果
 * @param extractFrameCount フレーム数
 * @param totalTimeMs 取り出すのにかかった時間。なお、ファイルに保存する時間等も含まれているので注意。
 * @param extractFrameImageUriList 取り出したフレームの[Uri]
 */
private data class ExtractResult(
    val extractFrameCount: Int,
    val totalTimeMs: Long,
    val extractFrameImageUriList: List<Uri>
)

/** 実際に動画からフレームを連続して取り出して保存する。 */
@Composable
fun VideoFrameExtractAndSaveScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val videoUri = remember { mutableStateOf<Uri?>(null) }
    val extractConfig = remember { mutableStateOf(ExtractConfig()) }
    val extractResult = remember { mutableStateOf<ExtractResult?>(null) }
    val isProgress = remember { mutableStateOf(false) }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> videoUri.value = uri }
    )

    /** 取り出す処理 */
    fun startExtract() {

        /** [Bitmap]を写真フォルダに保存する。 */
        suspend fun Bitmap.saveToPictureFolder(
            relativePath: String,
            name: String
        ): Uri = withContext(Dispatchers.IO) {
            // 端末の写真フォルダに保存
            val contentValues = contentValuesOf(
                MediaStore.MediaColumns.DISPLAY_NAME to name,
                MediaStore.MediaColumns.MIME_TYPE to "image/png",
                // 写真フォルダからの相対パス
                // DCIM と Pictures って何が違うんや...
                MediaStore.MediaColumns.RELATIVE_PATH to relativePath
            )
            // フレームを高速で取り出しても、保存処理で多分遅くなっちゃう
            val insertUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
            context.contentResolver.openOutputStream(insertUri)?.use { outputStream ->
                compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            return@withContext insertUri
        }

        scope.launch(Dispatchers.Default) {

            val uri = videoUri.value ?: return@launch
            val (isUseVideoFrameBitmapExtractor, startMs, stopMs, frameRate) = extractConfig.value
            val frameMs = 1_000L / frameRate
            val videoFramePositionMsList = (startMs until stopMs step frameMs)

            // 取り出したフレームの画像は、フォルダを新しく作ってそこにいれる
            val resultUriList = arrayListOf<Uri>()
            val mediaStoreRelativePath = "${Environment.DIRECTORY_PICTURES}/AndroidVideoFrameFastNextExtractor/${System.currentTimeMillis()}"

            extractResult.value = null
            isProgress.value = true

            val totalTimeMs = measureTimeMillis {
                if (isUseVideoFrameBitmapExtractor) {
                    // 自前の VideoFrameBitmapExtractor を使う
                    VideoFrameBitmapExtractor().apply {
                        prepareDecoder(context, uri)
                        for (positionMs in videoFramePositionMsList) {
                            // フレームを取り出す
                            val bitmap = getVideoFrameBitmap(seekToMs = positionMs)
                            // 写真フォルダに保存する
                            if (bitmap != null) {
                                resultUriList += bitmap.saveToPictureFolder(
                                    relativePath = mediaStoreRelativePath,
                                    name = "VideoFrame_${positionMs}_ms.png"
                                )
                            }
                        }
                        destroy()
                    }
                } else {
                    // MediaMetadataRetriever を使う
                    MediaMetadataRetriever().apply {
                        setDataSource(context, uri)
                        for (positionMs in videoFramePositionMsList) {
                            // フレームを取り出す
                            // 正確なフレームが欲しいので MediaMetadataRetriever.OPTION_CLOSEST
                            val bitmap = getFrameAtTime(positionMs * 1_000, MediaMetadataRetriever.OPTION_CLOSEST)!!
                            // 写真フォルダに保存する
                            resultUriList += bitmap.saveToPictureFolder(
                                relativePath = mediaStoreRelativePath,
                                name = "VideoFrame_${positionMs}_ms.png"
                            )
                        }
                        release()
                    }
                }


            }

            isProgress.value = false
            extractResult.value = ExtractResult(
                extractFrameCount = videoFramePositionMsList.count(),
                totalTimeMs = totalTimeMs,
                extractFrameImageUriList = resultUriList,
            )
        }
    }

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {

        item(span = { GridItemSpan(maxCurrentLineSpan) }, key = "input") {
            ExtractConfigInputUi(
                modifier = Modifier.fillMaxWidth(),
                extractConfig = extractConfig.value,
                onUpdate = { extractConfig.value = it },
                onVideoPickRequest = { videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
                onStartClick = { startExtract() }
            )
        }

        if (isProgress.value) {
            item(span = { GridItemSpan(maxCurrentLineSpan) }, key = "progress") {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (extractResult.value != null) {
            item(span = { GridItemSpan(maxCurrentLineSpan) }, key = "output") {
                ExtractResultUi(extractResult = extractResult.value!!)
            }
            items(
                items = extractResult.value!!.extractFrameImageUriList,
                key = { uri -> uri.toString() }
            ) { imageUri ->
                AsyncImage(model = imageUri, contentDescription = null)
            }
        }
    }
}

@Composable
private fun ExtractResultUi(
    modifier: Modifier = Modifier,
    extractResult: ExtractResult
) {
    Column(modifier = modifier) {
        Text(text = "取り出したフレーム数 = ${extractResult.extractFrameCount}")
        Text(text = "処理時間 = ${extractResult.totalTimeMs} ms")
        Text(text = "保存先 = ${Environment.DIRECTORY_PICTURES}/AndroidVideoFrameFastNextExtractor/")
    }
}

@Composable
private fun ExtractConfigInputUi(
    modifier: Modifier = Modifier,
    extractConfig: ExtractConfig,
    onUpdate: (ExtractConfig) -> Unit,
    onVideoPickRequest: () -> Unit,
    onStartClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {

        Button(onClick = onVideoPickRequest) {
            Text(text = "動画の選択")
        }

        Row {
            Text(
                modifier = Modifier.weight(1f),
                text = "VideoFrameBitmapExtractor を利用する。OFF なら MediaMetadataRetriever"
            )
            Switch(
                checked = extractConfig.isUseVideoFrameBitmapExtractor,
                onCheckedChange = { onUpdate(extractConfig.copy(isUseVideoFrameBitmapExtractor = it)) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                label = { Text(text = "開始位置 ms") },
                value = extractConfig.startMs.toString(),
                onValueChange = {
                    it.toLongOrNull()?.also { long ->
                        onUpdate(extractConfig.copy(startMs = long))
                    }
                }
            )
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                label = { Text(text = "終了位置 ms") },
                value = extractConfig.stopMs.toString(),
                onValueChange = {
                    it.toLongOrNull()?.also { long ->
                        onUpdate(extractConfig.copy(stopMs = long))
                    }
                }
            )
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                label = { Text(text = "フレームレート") },
                value = extractConfig.frameRate.toString(),
                onValueChange = {
                    it.toIntOrNull()?.also { int ->
                        onUpdate(extractConfig.copy(frameRate = int))
                    }
                }
            )
        }

        Button(onClick = onStartClick) {
            Text(text = "開始")
        }
    }

}