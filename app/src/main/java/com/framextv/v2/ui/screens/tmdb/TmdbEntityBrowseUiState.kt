package com.framextv.v2.ui.screens.tmdb

import com.framextv.v2.core.tmdb.TmdbEntityBrowseData

sealed interface TmdbEntityBrowseUiState {
    data object Loading : TmdbEntityBrowseUiState
    data class Error(val message: String) : TmdbEntityBrowseUiState
    data class Success(val data: TmdbEntityBrowseData) : TmdbEntityBrowseUiState
}
