package com.example.test2wallpaper

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.test2wallpaper.ui.theme.Test2WallpaperTheme
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
                    MyApp(innerPadding)
                }
            }
        }
    }
}

@Composable
fun MyApp(innerPadding: PaddingValues) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { SafebooruScreen(innerPadding, navController) }
        composable("details/{url}") { it ->
            val url = it.arguments?.getString("url")
            if (!url.isNullOrEmpty()) {
                DetailScreen(url, navController)
            }
        }
    }
}

@Composable
fun DetailScreen(url: String, navController: NavHostController) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        AsyncImage(
            model = url,
            contentDescription = null,
            modifier = Modifier.clickable {
                navController.popBackStack()
            }
        )
    }
}

@Composable
fun GridImagesLayout(
    innerPadding: PaddingValues,
    posts: List<SafebooruPost>,
    navController: NavHostController
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(200.dp),
        verticalItemSpacing = 10.dp,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(innerPadding)
    ) {
        items(posts) { post ->
            NetworkImage(
                post,
                navController
            )
        }
    }
}


@Composable
fun NetworkImage(post: SafebooruPost, navController: NavHostController) {
    AsyncImage(
        model = post.preview_url,
        contentDescription = post.tags,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight()
            .clickable() {
                val encodedUrl = Uri.encode(post.file_url)
                navController.navigate("details/$encodedUrl")
            }
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
fun SafebooruScreen(innerPadding: PaddingValues, navController: NavHostController) {
    var posts by remember { mutableStateOf<List<SafebooruPost>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            posts = api.getPosts(limit = 10, tags = "sky")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    GridImagesLayout(innerPadding, posts, navController)
}