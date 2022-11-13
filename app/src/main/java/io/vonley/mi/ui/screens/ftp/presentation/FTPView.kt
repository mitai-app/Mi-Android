package io.vonley.mi.ui.screens.ftp.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.vonley.mi.ui.ComposableFun
import io.vonley.mi.ui.screens.ftp.domain.repository.FTPEvent
import org.apache.commons.net.ftp.FTPFile

@Composable
fun FTPView() {
    val vm = hiltViewModel<FTPViewModel>()
    val ftpState by remember { vm.ftpState }
    vm.connect()
    FtpStateView(ftpState, onDirClick = { dir ->
        vm.navigateTo(dir)
    }, onFileClick = { file ->

    })
}

@Composable
private fun FtpStateView(state: FTPState, onDirClick: (FTPFile) -> Unit, onFileClick: (FTPFile) -> Unit) {
    Column() {
        DirFTPView(files = state.files, onDirClick, onFileClick) {
            Column {
                when (state) {
                    is FTPState.Error -> ErrorView(state)
                    is FTPState.Loading -> LoadingView(state)
                    is FTPState.Success -> SuccessFTPView(state)
                }
            }
        }
    }
}

@Composable
private fun ErrorView(state: FTPState.Error) {
    when (state.error) {
        is FTPEvent.Delete -> { }
        is FTPEvent.Download -> { }
        FTPEvent.None -> { }
        is FTPEvent.Rename -> { }
        is FTPEvent.Replace -> { }
        is FTPEvent.Upload -> { }
        is FTPEvent.WorkingDir -> { }
    }
}

@Composable
private fun LoadingView(state: FTPState.Loading) {
    when (state.event) {
        is FTPEvent.Delete -> { }
        is FTPEvent.Download -> { }
        FTPEvent.None -> { }
        is FTPEvent.Rename -> { }
        is FTPEvent.Replace -> { }
        is FTPEvent.Upload -> { }
        is FTPEvent.WorkingDir -> { }
    }
}

@Composable
private fun SuccessFTPView(state: FTPState.Success) {
    when (state.event) {
        is FTPEvent.Delete -> { }
        is FTPEvent.Download -> { }
        FTPEvent.None -> { }
        is FTPEvent.Rename -> { }
        is FTPEvent.Replace -> { }
        is FTPEvent.Upload -> { }
        is FTPEvent.WorkingDir -> { }
    }
}

@Composable
private fun DirFTPView(files: List<FTPFile>,
               onDirClick: (FTPFile) -> Unit,
               onFileClick: (FTPFile) -> Unit,
               composableFun: ComposableFun
) {
    Column() {
        composableFun()
        LazyColumn(contentPadding = PaddingValues(8.dp)) {
            items(
                items = files,
                itemContent = {
                    if (it.isDirectory) {
                        DirView(it, onDirClick)
                    } else {
                        FileView(it, onFileClick)
                    }
                }
            )
        }
    }
}


@Composable
private fun FTPContentImage(ftpFile: FTPFile) {
    val id: Int = if (ftpFile.isDirectory) {
        io.vonley.mi.R.drawable.icon_svg_folder
    } else {
        io.vonley.mi.R.drawable.icon_svg_file
    }
    val painter = painterResource(id = id)
    Image(
        painter = painter,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .padding(8.dp, 4.dp)
            .size(36.dp)
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DirView(dir: FTPFile, onDirClick: (FTPFile) -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth(),
        elevation = 0.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(corner = CornerSize(8.dp)),
        onClick = {
            onDirClick(dir)
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FTPContentImage(ftpFile = dir)
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)
            ) {
                Text(text = dir.name, style = typography.body1)
                Text(text = dir.size.toString(), style = typography.caption)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FileView(file: FTPFile, onFileClick: (FTPFile) -> Unit) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth(),
        elevation = 0.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(corner = CornerSize(8.dp)),
        onClick = {
            onFileClick(file)
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically)  {
            FTPContentImage(ftpFile = file)
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)
            ) {
                Text(text = file.name, style = typography.body1)
                Text(text = file.size.toString(), style = typography.caption)
            }
        }
    }
}

@Preview
@Composable
fun PreviewFtpStateView() {

    val items = arrayListOf(
        FTPFile().apply {
            this.name = "Directory Example"
            this.size = 100000
            this.type = FTPFile.DIRECTORY_TYPE
            this.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, true)
        },
        FTPFile().apply {
            this.name = "File Example"
            this.size = 100000
            this.type = FTPFile.FILE_TYPE
            this.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, true)
        },
        FTPFile().apply {
            this.name = "Symbolic Link Example"
            this.size = 100000
            this.type = FTPFile.SYMBOLIC_LINK_TYPE
            this.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, true)
        },
        FTPFile().apply {
            this.name = "Unknown Example"
            this.size = 100000
            this.type = FTPFile.UNKNOWN_TYPE
            this.setPermission(FTPFile.WORLD_ACCESS, FTPFile.READ_PERMISSION, true)
        },
    )
    FtpStateView(FTPState.Loading(items, FTPEvent.None), onFileClick = {

    }, onDirClick = {

    })
}
