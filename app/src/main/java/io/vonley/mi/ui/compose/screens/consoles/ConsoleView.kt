package io.vonley.mi.ui.compose.screens.consoles

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.vonley.mi.R
import io.vonley.mi.models.enums.Feature
import io.vonley.mi.models.enums.PlatformType
import io.vonley.mi.ui.main.console.domain.model.Console
import io.vonley.mi.ui.main.console.domain.model.color

@Composable
fun ConsolesView() {
    val consoles = arrayOf(
        Console(
            ip = "192.168.11.45",
            name = "PS4",
            type = PlatformType.PS4,
            features = arrayListOf(
                Feature.RPI, Feature.KLOG, Feature.GOLDENHEN, Feature.FTP
            ),
            lastKnownReachable = true,
            wifi = "Gaoooub",
            pinned = true
        ),
        Console(
            ip = "192.168.11.46",
            name = "PS3",
            type = PlatformType.PS3,
            features = arrayListOf(
                Feature.WEBMAN, Feature.PS3MAPI, Feature.CCAPI, Feature.FTP
            ),
            lastKnownReachable = true,
            wifi = "Gaoooub",
            pinned = true
        ),
        Console(
            ip = "192.168.11.47",
            name = "Unknown",
            type = PlatformType.UNKNOWN,
            features = arrayListOf(
                Feature.FTP
            ),
            lastKnownReachable = true,
            wifi = "Gaoooub",
            pinned = true
        )
    )
    Column() {

        consoles.forEach {
            ConsoleView(console = it)
        }
    }
}


@Preview
@Composable
fun PreviewConsoleView() {
    //ConsolesView()
    val consoles = arrayOf(
        Console(
            ip = "192.168.11.45",
            name = "PS4",
            type = PlatformType.PS4,
            features = arrayListOf(
                Feature.RPI, Feature.KLOG, Feature.GOLDENHEN, Feature.FTP
            ),
            lastKnownReachable = true,
            wifi = "Gaoooub",
            pinned = true
        ),
        Console(
            ip = "192.168.11.46",
            name = "PS3",
            type = PlatformType.PS3,
            features = arrayListOf(
                Feature.WEBMAN, Feature.PS3MAPI, Feature.CCAPI, Feature.FTP
            ),
            lastKnownReachable = true,
            wifi = "Gaoooub",
            pinned = true
        ),
        Console(
            ip = "192.168.11.47",
            name = "Unknown",
            type = PlatformType.UNKNOWN,
            features = arrayListOf(
                Feature.FTP
            ),
            lastKnownReachable = true,
            wifi = "Gaoooub",
            pinned = true
        )
    )
    Column {
        consoles.forEach {
            ConsoleView(console = it)
        }
    }
}

@Composable
fun ConsoleView(console: Console) {
    val id = when (console.type) {
        PlatformType.UNKNOWN -> R.drawable.icon_svg_wifi
        PlatformType.PS3 -> R.drawable.icon_svg_monitor
        PlatformType.PS4 -> R.drawable.icon_svg_settings
    }
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp, 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = console.color
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp)
        ) {

            Image(
                painter = painterResource(id = R.drawable.icon_svg_monitor),
                modifier = Modifier
                    .width(42.dp)
                    .height(42.dp)
                    .align(Alignment.CenterVertically),
                contentScale = ContentScale.Crop, colorFilter = ColorFilter.tint(Color.White),
                contentDescription = null // decorative element
            )


            Column(
                modifier = Modifier
                    .padding(24.dp, 0.dp)
            ) {
                Text(
                    text = console.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White,
                )
                Text(
                    text = console.ip,
                    color = Color.White,
                )
            }
        }
    }

}