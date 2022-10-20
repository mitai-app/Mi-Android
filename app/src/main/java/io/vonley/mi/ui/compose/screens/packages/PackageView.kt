package io.vonley.mi.ui.compose.screens.packages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.annotations.SerializedName


data class RepositoryModel(
    val title: String,
    val author: String,
    val banner: String,
    val description: String,
    val packages: Array<PackageModel>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RepositoryModel) return false

        if (title != other.title) return false
        if (author != other.author) return false
        if (banner != other.banner) return false
        if (description != other.description) return false
        if (!packages.contentEquals(other.packages)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + banner.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + packages.contentHashCode()
        return result
    }
}

data class PackageModel(
    val name: String,
    val author: String,
    val version: String,
    val type: PackageType,
    val icon: String,
    val link: String,
    val dl: Map<String, String>
)

enum class PackageType {
    @SerializedName("app")
    APP,

    @SerializedName("tool")
    TOOL,

    @SerializedName("plugin")
    PLUGIN
}

// "https://raw.githubusercontent.com/mitai-app/versioning/main/packages.json"

@Composable
fun RepositoryView() {
    val repos = arrayListOf(
        RepositoryModel(
            title = "Repository",
            author = "Mi",
            banner = "https://images.unsplash.com/photo-1559706690-1311487b0b51",
            description = "Common PS4 payloads",
            packages = arrayOf(
                PackageModel(
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
                PackageModel(
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
    )
    Column {
        repos.forEach {
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
                    Text(text = it.title)
                    Text(text = it.author)
                    Text(text = it.description)
                }
            }
        }
    }

}


@Preview
@Composable
fun PreviewPackageView() {
    RepositoryView()
}