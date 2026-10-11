package io.vonley.mi.ui.screens.home.presentation.article

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.vonley.mi.Mi
import io.vonley.mi.R
import io.vonley.mi.models.Article

@DrawableRes
private fun iconFor(name: String?): Int? = when (name) {
    "question" -> R.mipmap.question
    "help" -> R.mipmap.help
    "team" -> R.mipmap.team
    else -> null
}

@Composable
fun ArticleCard(article: Article, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = article.link != null) {
                article.link?.let { runCatching { uriHandler.openUri(it) } }
            },
        colors = CardDefaults.cardColors(containerColor = article.background, contentColor = Color.White),
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = article.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = article.description, style = MaterialTheme.typography.bodyMedium)
        }
        iconFor(article.icon)?.let { icon ->
            Image(
                painter = painterResource(id = icon),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop,
                contentDescription = null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewArticleCard() {
    ArticleCard(article = Mi.ARTICLES[0])
}
