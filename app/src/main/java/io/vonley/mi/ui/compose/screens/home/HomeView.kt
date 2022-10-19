package io.vonley.mi.ui.compose.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.vonley.mi.Constants
import io.vonley.mi.ui.common.article.ArticleCard
import io.vonley.mi.ui.common.log.LogCard
import io.vonley.mi.ui.common.log.LogCardBase

@Composable
fun HomeView() {
    Column {
        ArticleList()
        LogList()
    }
}

@Composable
fun LogList() {
    Column(modifier = Modifier.verticalScroll(state = ScrollState(0))) {
        LogCardBase(title = "Connect to 192.168.11.248:8080", description = "Connect to this ip to start jailbreak process.", background = Constants.Color.QUATERNARY)
    }
}

@Composable
fun ArticleList() {
    Column(modifier = Modifier.verticalScroll(state = ScrollState(0))) {
        Constants.ARTICLES.forEach {
            ArticleCard(article = it)
        }
    }
}


@Preview
@Composable
fun PreviewHomeView() {
    HomeView()
}