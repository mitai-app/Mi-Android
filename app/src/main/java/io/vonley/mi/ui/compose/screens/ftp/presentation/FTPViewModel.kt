package io.vonley.mi.ui.compose.screens.ftp.presentation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.vonley.mi.di.annotations.SharedPreferenceStorage
import io.vonley.mi.utils.SharedPreferenceManager
import javax.inject.Inject


@HiltViewModel
class FTPViewModel @Inject constructor(
    @SharedPreferenceStorage val manager: SharedPreferenceManager
) : ViewModel() {
    
}