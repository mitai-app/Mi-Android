package io.vonley.mi.ui.compose.templates.textfield

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextField(
    caption: String,
    placeholder: String,
    onSearchChange: (String) -> Unit,
    color: Color = Color(0xff76a9ff),
    background: Color = Color(0xffd8e6ff),
    maxLength: Int = 110
) {
    Column {
        var textState by remember { mutableStateOf("") }
        Text(
            text = caption,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            textAlign = TextAlign.Start,
            color = color
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = textState,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = background,
                cursorColor = Color.Black,
                disabledLabelColor = background,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            placeholder = {
                Text(text = placeholder, color = Color.Gray)
            },
            onValueChange = {
                if (it.length <= maxLength) {
                    textState = it
                    onSearchChange(textState)
                }
            },
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            trailingIcon = {
                if (textState.isNotEmpty()) {
                    IconButton(onClick = {
                        textState = ""
                        onSearchChange(textState)
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null
                        )
                    }
                }
            }
        )
        Text(
            text = "${textState.length} / $maxLength",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            textAlign = TextAlign.End,
            color = color
        )
    }
}


@Preview
@Composable
fun PreviewTextField() {
    CustomTextField(
        caption = "Caption",
        placeholder = "Enter repo url or search by packages...",
        onSearchChange = {

        })
}