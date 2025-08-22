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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.test2wallpaper.ui.theme.Test2WallpaperTheme
import images

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Test2WallpaperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GridImagesLayout(innerPadding)
                }
            }
        }
    }
}

@Composable
fun GridImagesLayout(innerPadding: PaddingValues) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(200.dp),
        verticalItemSpacing = 10.dp,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(innerPadding)
    ) {
        items(images) { image ->
            NetworkImage(
                image[0],
                image[1]
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