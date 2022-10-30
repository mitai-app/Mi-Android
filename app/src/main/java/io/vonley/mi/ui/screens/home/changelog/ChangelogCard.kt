package io.vonley.mi.ui.screens.home.changelog


import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import io.vonley.mi.models.*


@OptIn(ExperimentalUnitApi::class)
@Composable
fun ChangelogCardBase(title: String?, description: String, background: Color) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp, 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = background
        )
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp, 16.dp)
        ) {
            if (title != null) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = TextUnit(16F, TextUnitType.Sp),
                    color = Color.White,
                )
                Spacer(modifier = Modifier.padding(0.dp))
            }
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
fun ChangelogCard(log: MiCMDResponse) {
    var title: String? = null
    if (log.device != null) {
        title = "${log.device!!.device} (${log.device!!.version}): ${log.device!!.ip}"
    }
    ChangelogCardBase(title = title, description = log.response, background = log.data.cmd.bg)
}


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewLogCardList() {
    val device = Device("PS4","9.00", "192.168.11.45")
    val logs = arrayOf(
        MiCMDResponse("Initializing setup", MiResponse.Cmd(MIEnumCMD.INITIATED), device),
        MiCMDResponse("Starting Jailbreak", MiResponse.Cmd(MIEnumCMD.STARTED), device),
        MiCMDResponse("Press X to Continue", MiResponse.Cmd(MIEnumCMD.CONTINUE), device),
        MiCMDResponse("Mi is requesting the payload", MiResponse.Cmd(MIEnumCMD.PAYLOAD_REQUEST), device),
        MiCMDResponse("Payload Sent", MiResponse.Cmd(MIEnumCMD.PAYLOAD), device),
        MiCMDResponse("Pending.....", MiResponse.Cmd(MIEnumCMD.PENDING), device),
        MiCMDResponse("Jailbreak complete", MiResponse.Cmd(MIEnumCMD.SUCCESS), device),
        MiCMDResponse("Jailbreak failed", MiResponse.Cmd(MIEnumCMD.FAILED), device),


    )
    Column(modifier = Modifier.verticalScroll(state = ScrollState(0))) {
        logs.forEach {
            ChangelogCard(log = it)
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PreviewLogCard() {
    val device = Device("PS4","9.00", "192.168.11.45")
    ChangelogCard(log = MiCMDResponse("Jailbreak complete", MiResponse.Cmd(MIEnumCMD.SUCCESS), device))
}