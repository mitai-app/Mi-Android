package io.vonley.mi.ui.compose.screens.packages.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import io.vonley.mi.Constants
import io.vonley.mi.extensions.isLink
import io.vonley.mi.ui.compose.screens.packages.data.local.entity.Package
import io.vonley.mi.ui.compose.screens.packages.data.local.entity.PackageType
import io.vonley.mi.ui.compose.screens.packages.data.local.entity.Repo


@Composable
fun RepositoryView() {
    val vm = hiltViewModel<RepoViewModel>()
    val repoState by remember { vm.repoState }
    RepoViewState(
        state = repoState,
        onAddRepoClick = { link ->
            vm.addRepo(link)
        },
        onSearchChange = { search ->
            if (search.isNotEmpty() && !search.isLink()) {
                vm.searchRelevance(search)
            } else if (search.isEmpty()){
                vm.getRepos()
            }
        }
    )
}

@Composable
fun RepoViewState(state: RepoState, onSearchChange: (String) -> Unit,  onAddRepoClick: (String) -> Unit) {
    val search = remember {
        mutableStateOf("")
    }
    Column {
        Row() {
            TextField(
                value = search.value,
                visualTransformation=VisualTransformation.None,
                modifier = Modifier
                    .padding(16.dp, 16.dp, 16.dp, 8.dp)
                    .fillMaxWidth(),
                colors = TextFieldDefaults
                    .textFieldColors(
                        backgroundColor = Color.White,
                        textColor = Color.Black
                    ),
                onValueChange = {
                    search.value = it
                    onSearchChange(it)
                },
                placeholder = {
                    Text("Enter repo url or search by packages...", color = Color.Gray)
                },
                maxLines = 1,
                shape = RoundedCornerShape(12.dp)
            )
        }
        if (search.value.isLink()) {
            AddRepo(link = search.value, onClick = onAddRepoClick)
        } else {
            Column(modifier = Modifier.verticalScroll(ScrollState(0))) {
                when (state) {
                    is RepoState.Loading -> {
                        LoadingRepo()
                    }
                    is RepoState.Success -> {
                        if (state.repos.isNotEmpty()) {
                            RepoListView(state.repos)
                        } else {
                            EmptyRepo()
                        }
                    }
                    is RepoState.Error -> {
                        ErrorRepo(state.error)
                    }
                }
            }
        }
    }
}

@Composable
fun AddRepo(link: String, onClick: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick(link) })
            .height(100.dp)
            .padding(16.dp, 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Constants.Color.TERTIARY
        )
    ) {
        Column {
            Spacer(modifier = Modifier.padding(8.dp))
            Text(
                text = "Add Repository",
                modifier = Modifier.padding(16.dp, 0.dp)
            )
        }
    }
}

@Composable
fun ErrorRepo(error: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp, 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Red
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = error)
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
            containerColor = Color(0,0,0, 0x50)
        )
    ) {
        val painter = rememberAsyncImagePainter(model = repo.banner)
        Box(contentAlignment = Alignment.BottomStart) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )
            Column(
                modifier= Modifier
                    .padding(0.dp)
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.4F))
            ) {
                Column(
                    modifier= Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = repo.title,
                        modifier = Modifier.padding(0.dp, 0.dp),
                        style = typography.h6.copy(color = Color.White)
                    )
                    Text(
                        text = repo.author,
                        modifier = Modifier.padding(0.dp, 0.dp),
                        style = typography.body2.copy(color = Color.White)
                    )
                    Text(
                        text = repo.description,
                        modifier = Modifier.padding(0.dp, 0.dp),
                        style = typography.caption.copy(color = Color.White)
                    )
                }

            }
        }
    }
}

@Preview
@Composable
fun PreviewLoadingState() {
    RepoViewState(RepoState.Loading , {}, {})
}

@Preview
@Composable
fun PreviewEmpty() {
    RepoViewState(RepoState.Success(), {}, {})
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
    val state = RepoState.Success(
        repos = arrayListOf(
            elements, elements
        )
    )
    RepoViewState(state, {}, {})
}