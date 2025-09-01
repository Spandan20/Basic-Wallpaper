package com.example.test2wallpaper

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
        composable("home") { DanbooruScreen(innerPadding, navController) }
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
    posts: List<DanbooruPost>,
    navController: NavHostController,
    loadNextPage: () -> Unit,
) {
    Column {
        Button(
            {
                loadNextPage()
            },
            modifier = Modifier.padding(innerPadding)
        ) {
            Text("Load More")
        }
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

}


@Composable
fun NetworkImage(post: DanbooruPost, navController: NavHostController) {
    AsyncImage(
        model = post.large_file_url,
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

data class DanbooruPost(
    val id: Int,
    val file_url: String,
    val large_file_url: String,
    val tags: String
)

interface DanbooruApi {
    @GET("posts.json")
    suspend fun getPosts(
        @Query("page") page: Int = 0,
        @Query("tags") tags: String? = null
    ): List<DanbooruPost>
}

val api: DanbooruApi = Retrofit.Builder()
    .baseUrl("https://danbooru.donmai.us/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(DanbooruApi::class.java)


@Composable
fun DanbooruScreen(innerPadding: PaddingValues, navController: NavHostController, page: Int = 0) {
    var posts by remember { mutableStateOf<List<DanbooruPost>>(emptyList()) }
    var page by rememberSaveable { mutableIntStateOf(0) }

    fun loadNextPage() {
        page++
    }

    LaunchedEffect(page) {
        try {
            posts += api.getPosts(page = page,tags = "guest_art")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    GridImagesLayout(innerPadding, posts, navController, { loadNextPage() })
}