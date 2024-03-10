package io.vonley.mi.ui.screens.packages.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import dagger.hilt.android.AndroidEntryPoint
import io.vonley.mi.Mi
import io.vonley.mi.common.templates.textfield.CustomTextField
import io.vonley.mi.extensions.isLink
import io.vonley.mi.ui.screens.packages.data.local.entity.Package
import io.vonley.mi.ui.screens.packages.data.local.entity.PackageType
import io.vonley.mi.ui.screens.packages.data.local.entity.Repo


@AndroidEntryPoint
class PackageFragmentView : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                RepositoryView()
            }
        }
    }
}

@Composable
fun RepositoryView() {
    val vm = hiltViewModel<PackageViewModel>()
    val repoState by remember { vm.repoState }
    RepoViewState(
        state = repoState,
        onAddRepoClick = { link ->
            vm.addRepo(link)
        },
        onSearchChange = { search ->
            if (search.isNotEmpty() && !search.isLink()) {
                vm.searchRelevance(search)
            } else if (search.isEmpty()) {
                vm.getRepos()
            }
        }
    )
}

@Composable
fun RepoViewState(
    state: PackageState,
    onSearchChange: (String) -> Unit,
    onAddRepoClick: (String) -> Unit
) {
    val search = remember {
        mutableStateOf("")
    }
    Column {
        Row(
            modifier = Modifier.padding(16.dp, 0.dp)
        ) {
            CustomTextField(
                caption = "",
                placeholder = "Enter repo url or search by packages...",
                onSearchChange = onSearchChange
            )
        }
        if (search.value.isLink()) {
            AddRepo(link = search.value, onClick = onAddRepoClick)
        } else {
            when (state) {
                is PackageState.Loading -> {
                    LoadingRepo()
                }

                is PackageState.Success -> {
                    if (state.repos.isNotEmpty()) {
                        RepoListView(state.repos)
                    } else {
                        EmptyRepo()
                    }
                }

                is PackageState.Error -> {
                    ErrorRepo(state.error)
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
            containerColor = Mi.Color.TERTIARY
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
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = error,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun EmptyRepo() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "There's no repository...",
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun LoadingRepo() {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Loading...",
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun RepoListView(repos: List<Repo>) {
    repos.forEach {
        val expanded = remember { mutableStateOf(false) }
        RepoCard(repo = it) {
            expanded.value = !expanded.value
        }
        if (expanded.value) {
            ListRepoPackages(it.packages) { pkg ->

            }
        }
    }

}

@Composable
fun RepoCard(repo: Repo, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp, 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0, 0, 0, 0x50)
        ), onClick = onClick
    ) {
        val painter = rememberAsyncImagePainter(
            model = repo.banner, placeholder = painterResource(
                id = io.vonley.mi.R.mipmap.help
            )
        )
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
                modifier = Modifier
                    .padding(0.dp)
                    .fillMaxWidth()
                    .background(Color.Black.copy(0.4F))
            ) {
                Column(
                    modifier = Modifier
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

@Composable
fun ListRepoPackages(packages: Array<Package>, onPackageClick: (Package) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        items(packages) {
            RepoPackageItem(repoPackage = it, onPackageClick)
        }
    }
}

@Composable
fun RepoPackageItem(repoPackage: Package, onPackageClick: (Package) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(32.dp, 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0, 0, 0, 0x50)),
        onClick = {
            onPackageClick(repoPackage)
        }
    ) {

        val animated = ImageVector.vectorResource(io.vonley.mi.R.drawable.icon_svg_inbox)
        val painter = rememberAsyncImagePainter(
            model = repoPackage.icon,
            placeholder = rememberVectorPainter(animated)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp, 8.dp)
                .fillMaxWidth()
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(48.dp)
                    .width(48.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Column(
                modifier = Modifier
                    .padding(8.dp, 0.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = repoPackage.name,
                    modifier = Modifier.padding(0.dp, 0.dp),
                    style = typography.body1.copy(color = Color.White)
                )
                Text(
                    text = repoPackage.author,
                    modifier = Modifier.padding(0.dp, 0.dp),
                    style = typography.body2.copy(color = Color.White)
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewPackageListItem() {
    RepoPackageItem(
        repoPackage =
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
        )
    ) { pkg ->

    }
}

@Preview
@Composable
fun PreviewPackageList() {
    Column {
        ListRepoPackages(
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
        ) { pkg ->

        }
    }
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
    val state = PackageState.Success(
        repos = arrayListOf(
            elements, elements
        )
    )
    RepoViewState(state, {}, {})
}
