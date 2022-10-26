package io.vonley.mi.ui.compose.screens.ftp.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue

@Composable
fun FTPView() {
    val vm = hiltViewModel<FTPViewModel>()
    val ftpState by remember { vm.ftpState }
    FtpStateView(ftpState)
}

@Composable
fun FtpStateView(state: FTPState) {
    Column() {
        when(state) {
            is FTPState.Error -> ErrorView(state.error)
            FTPState.Loading -> LoadingView()
            is FTPState.Success -> ActualFTPView(state)
        }
    }
}

@Composable
fun ErrorView(error: String) {

}

@Composable
fun LoadingView() {

}

@Composable
fun ActualFTPView(state: FTPState.Success) {

}

@Preview
@Composable
fun PreviewFtpStateView() {
    FtpStateView(FTPState.Loading)
}
