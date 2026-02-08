package com.example.application

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
fun App(newsViewModel: NewsViewModel = viewModel()) {
    NewsGrid(newsViewModel = newsViewModel)
}

@Composable
fun NewsGrid(newsViewModel: NewsViewModel) {
    val displayedNews by remember { derivedStateOf { newsViewModel.displayedNews } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(top=15.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NewsQuarter(
                    news = displayedNews.getOrNull(0),
                    onLikeClick = { newsViewModel.likeNews(0) },
                    modifier = Modifier.weight(1f)
                )
                NewsQuarter(
                    news = displayedNews.getOrNull(1),
                    onLikeClick = { newsViewModel.likeNews(1) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NewsQuarter(
                    news = displayedNews.getOrNull(2),
                    onLikeClick = { newsViewModel.likeNews(2) },
                    modifier = Modifier.weight(1f)
                )
                NewsQuarter(
                    news = displayedNews.getOrNull(3),
                    onLikeClick = { newsViewModel.likeNews(3) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NewsQuarter(
    news: News?,
    onLikeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                if (news != null) {
                    LazyColumn {
                        item {
                            Text(
                                text = news.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = news.content,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onLikeClick() }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👍 ",
                        fontSize = 20.sp
                    )
                    Text(
                        text = "${news?.likes ?: 0}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}