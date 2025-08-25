package com.example.test2wallpaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.test2wallpaper.ui.theme.Test2WallpaperTheme
import images
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Test2WallpaperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SafebooruScreen(innerPadding)
                }
            }
        }
    }
}

@Composable
fun GridImagesLayout(innerPadding: PaddingValues, posts: List<SafebooruPost>) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(200.dp),
        verticalItemSpacing = 10.dp,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(innerPadding)
    ) {
        items(posts) { post ->
            NetworkImage(
                post.preview_url,
                post.tags
            )
        }
    }
}


@Composable
fun NetworkImage(url: String, contentDescription: String?) {
    AsyncImage(
        model = url,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight()
    )
}

data class SafebooruPost(
    val id: Int,
    val file_url: String,
    val preview_url: String,
    val tags: String
)

interface SafebooruApi {
    @GET("index.php?page=dapi&s=post&q=index&json=1")
    suspend fun getPosts(
        @Query("limit") limit: Int = 20,
        @Query("tags") tags: String? = null
    ): List<SafebooruPost>
}

val api: SafebooruApi = Retrofit.Builder()
    .baseUrl("https://safebooru.org/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(SafebooruApi::class.java)


@Composable
fun SafebooruScreen(innerPadding: PaddingValues) {
    var posts by remember { mutableStateOf<List<SafebooruPost>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                posts = api.getPosts(limit = 100, tags = "sky")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    GridImagesLayout(innerPadding, posts)
}