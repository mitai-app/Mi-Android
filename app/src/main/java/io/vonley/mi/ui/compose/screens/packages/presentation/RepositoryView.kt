package io.vonley.mi.ui.compose.screens.packages.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import io.vonley.mi.Constants
import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.Package
import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.PackageType
import io.vonley.mi.ui.compose.screens.packages.data.remote.dto.Repo


@Composable
fun RepositoryView() {
    val vm = hiltViewModel<RepoViewModel>()
    var repoState by remember { mutableStateOf(RepoState(true)) }
    vm.newThisMonth.observeForever { state ->
        repoState = state
    }
    RepoViewState(state = repoState)
}

@Composable
fun RepoViewState(state: RepoState) {
    Column(modifier = Modifier.verticalScroll(ScrollState(0))) {
        if (state.loading) {
            LoadingRepo()
        } else {
            if (state.repos.isNotEmpty()) {
                RepoListView(state.repos)
            } else {
                EmptyRepo()
            }
        }
    }
}

@Composable
fun EmptyRepo() {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp, 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "There's no repository...")
        }
    }
}

@Composable
fun LoadingRepo() {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp, 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Gray
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Loadin")
        }
    }
}

@Composable
fun RepoListView(repos: List<Repo>) {
    repos.forEach {
        RepoCard(it)
    }
}

@Composable
fun RepoCard(repo: Repo) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp, 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Constants.Color.TERTIARY
        )
    ) {
        Column {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = repo.title,
                modifier = Modifier.padding(16.dp, 0.dp)
            )
            Text(
                text = repo.author,
                modifier = Modifier.padding(16.dp, 0.dp)
            )
            Text(
                text = repo.description,
                modifier = Modifier.padding(16.dp, 0.dp)
            )
            Image(
                painter= rememberAsyncImagePainter(model = repo.banner),
                contentDescription=null,
                contentScale=ContentScale.Crop,
                modifier= Modifier
                    .fillMaxWidth()
                    .height(128.dp)
            )
        }
    }
}

@Preview
@Composable
fun PreviewLoadingState() {
    RepoViewState(RepoState(true, emptyList(), ""))
}

@Preview
@Composable
fun PreviewEmpty() {
    RepoViewState(RepoState(false, emptyList(), ""))
}

@Preview
@Composable
fun PreviewRepoView() {
    val elements = Repo(
        link = "https://raw.githubusercontent.com/mitai-app/versioning/main/packages.json",
        title = "Repository",
        author = "Mi",
        banner = "https://images.unsplash.com/photo-1559706690-1311487b0b51",
        description = "Common PS4 payloads",
        packages = arrayOf(
            Package(
                name = "GoldHen",
                author = "SiSTRo",
                version = "2.2.4",
                type = PackageType.PLUGIN,
                icon = "https://avatars.githubusercontent.com/u/91367123?s=100&v=4",
                link = "https://github.com/GoldHEN/GoldHEN",
                dl = mapOf(
                    "v900" to "https://github.com/GoldHEN/GoldHEN/blob/19d768eef604b5df16f4be87755c9877c70a0b55/goldhen_2.2.4_900.bin?raw=true",
                    "v755" to "https://github.com/GoldHEN/GoldHEN/blob/19d768eef604b5df16f4be87755c9877c70a0b55/goldhen_2.2.4_755.bin?raw=true",
                    "v702" to "https://github.com/GoldHEN/GoldHEN/blob/19d768eef604b5df16f4be87755c9877c70a0b55/goldhen_2.2.4_702.bin?raw=true",
                    "v672" to "https://github.com/GoldHEN/GoldHEN/blob/19d768eef604b5df16f4be87755c9877c70a0b55/goldhen_2.2.4_672.bin?raw=true",
                    "v505" to "https://github.com/GoldHEN/GoldHEN/blob/19d768eef604b5df16f4be87755c9877c70a0b55/goldhen_2.2.4_505.bin?raw=true"
                )
            ),
            Package(
                name = "Orbis Toolbox",
                author = "OSM-Made",
                version = "1.0.1190-alpha",
                type = PackageType.TOOL,
                icon = "https://avatars.githubusercontent.com/u/67383397?v=4",
                link = "https://github.com/OSM-Made/Orbis-Toolbox",
                dl = mapOf(
                    "v900" to "https://github.com/OSM-Made/Orbis-Toolbox/releases/download/1.0.1190/Orbis-Toolbox-900.bin",
                    "v755" to "https://github.com/OSM-Made/Orbis-Toolbox/releases/download/1.0.1190/Orbis-Toolbox-755.bin",
                    "v702" to "https://github.com/OSM-Made/Orbis-Toolbox/releases/download/1.0.1190/Orbis-Toolbox-702.bin",
                    "v672" to "https://github.com/OSM-Made/Orbis-Toolbox/releases/download/1.0.1190/Orbis-Toolbox-672.bin",
                    "v505" to "https://github.com/OSM-Made/Orbis-Toolbox/releases/download/1.0.1190/Orbis-Toolbox-505.bin"
                )
            )
        )
    )
    val state = RepoState(
        false, repos = arrayListOf(
            elements, elements
        ), ""
    )
    RepoViewState(state)
}