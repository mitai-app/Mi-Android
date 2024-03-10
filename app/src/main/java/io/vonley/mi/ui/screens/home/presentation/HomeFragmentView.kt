package io.vonley.mi.ui.screens.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.vonley.mi.Mi
import io.vonley.mi.ui.screens.home.presentation.article.ArticleCard
import io.vonley.mi.ui.screens.home.presentation.log.LogCard
import io.vonley.mi.ui.screens.home.presentation.log.LogCardBase


@AndroidEntryPoint
class HomeFragmentView : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                HomeView()
            }
        }
    }
}

@Composable
fun HomeView() {
    val vm = hiltViewModel<HomeViewModel>()
    val logs = vm.state.collectAsState()
    ArticleList {
        LogList(logs)
    }
}

@Composable
fun LogList(state: State<HomeState>) {
    Text(
        text = "Logs",
        color = Color.Black,
        modifier = Modifier.padding(16.dp, 8.dp),
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Normal,
        fontSize = 20.sp
    )
    when (val curr = state.value) {
        HomeState.Empty -> {
            LogCardBase(
                title = "Connect to http://192.168.11.18:8080",
                description = "",
                background = Mi.Color.QUATERNARY
            )
        }

        is HomeState.Log -> {
            LazyColumn {
                items(curr.logs) {
                    LogCard(log = it)
                }
            }
        }
    }

}

@Composable
inline fun ArticleList(crossinline content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .verticalScroll(ScrollState(0))
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        Mi.ARTICLES.forEach {
            ArticleCard(article = it)
        }
        content()
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewHomeView() {
    HomeView()
}