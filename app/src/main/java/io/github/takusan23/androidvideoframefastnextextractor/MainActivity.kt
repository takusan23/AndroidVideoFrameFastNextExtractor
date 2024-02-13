package io.github.takusan23.androidvideoframefastnextextractor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.takusan23.androidvideoframefastnextextractor.ui.screen.FastFrameExtractScreen
import io.github.takusan23.androidvideoframefastnextextractor.ui.theme.AndroidVideoFrameFastNextExtractorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AndroidVideoFrameFastNextExtractorTheme {
//                VideoFrameBitmapExtractorScreen()
//                MediaMetadataRetrieverScreen()
//                BenchMarkScreen()
                FastFrameExtractScreen()
            }
        }
    }
}
