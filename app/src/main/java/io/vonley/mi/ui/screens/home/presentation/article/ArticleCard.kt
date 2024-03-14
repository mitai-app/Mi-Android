package io.vonley.mi.ui.screens.home.presentation.article

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import io.vonley.mi.Mi
import io.vonley.mi.R
import io.vonley.mi.extensions.isLink
import io.vonley.mi.models.Article


@OptIn(ExperimentalUnitApi::class)
@Composable
fun ArticleCard(article: Article) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = article.background
        )
    ) {
        Column(
            modifier = Modifier
                .padding(Dp(24F))
        ) {
            Text(
                text = article.name,
                fontWeight = FontWeight.Bold,
                fontSize = TextUnit(18F, TextUnitType.Sp),
                color = Color.White,
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = article.description,
                color = Color.White,
            )
        }
        if (article.icon != null) {
            //help, question, team
            if (article.icon!!.isLink())
            {
                val id = R.mipmap.orb
                val painter = painterResource(id = id)
                Image(
                    painter = painter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .heightIn(240.dp, 240.dp),
                    contentScale = ContentScale.Crop,
                    contentDescription = null // decorative element
                )
            } else {
                val context = LocalContext.current
                val drawableId = remember(article.icon) {
                    context.resources.getIdentifier(
                        article.icon,
                        "mipmap",
                        context.packageName
                    )
                }
                Image(
                    painter = painterResource(id = drawableId),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .heightIn(240.dp, 240.dp),
                    contentScale = ContentScale.Crop,
                    contentDescription = null // decorative element
                )
            }
        }
    }
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewMessageCardList() {
    Column(modifier = Modifier.verticalScroll(state = ScrollState(0))) {
        Mi.ARTICLES.forEach {
            ArticleCard(article = it)
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewMessageCard() {
    ArticleCard(article = Mi.ARTICLES[0])
}