package io.github.takusan23.androidvideoframefastnextextractor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import io.github.takusan23.androidvideoframefastnextextractor.ui.screen.BenchMarkScreen
import io.github.takusan23.androidvideoframefastnextextractor.ui.screen.FastFrameExtractScreen
import io.github.takusan23.androidvideoframefastnextextractor.ui.screen.MediaMetadataRetrieverScreen
import io.github.takusan23.androidvideoframefastnextextractor.ui.screen.VideoFrameBitmapExtractorScreen
import io.github.takusan23.androidvideoframefastnextextractor.ui.theme.AndroidVideoFrameFastNextExtractorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AndroidVideoFrameFastNextExtractorTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val currentPage = remember { mutableStateOf(MainScreenPage.FastFrameExtractScreen) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = currentPage.value.name) },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            ScrollableTabRow(selectedTabIndex = MainScreenPage.entries.indexOf(currentPage.value)) {
                MainScreenPage.entries.forEach { page ->
                    Tab(
                        selected = page == currentPage.value,
                        onClick = { currentPage.value = page }
                    ) {
                        Text(
                            modifier = Modifier.padding(10.dp),
                            text = page.name
                        )
                    }
                }
            }

            when (currentPage.value) {
                MainScreenPage.VideoFrameBitmapExtractorScreen -> VideoFrameBitmapExtractorScreen()
                MainScreenPage.MediaMetadataRetrieverScreen -> MediaMetadataRetrieverScreen()
                MainScreenPage.BenchMarkScreen -> BenchMarkScreen()
                MainScreenPage.FastFrameExtractScreen -> FastFrameExtractScreen()
            }
        }
    }
}

enum class MainScreenPage {
    VideoFrameBitmapExtractorScreen,
    MediaMetadataRetrieverScreen,
    BenchMarkScreen,
    FastFrameExtractScreen
}
