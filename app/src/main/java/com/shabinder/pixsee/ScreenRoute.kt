package com.shabinder.pixsee

import kotlinx.serialization.Serializable

@Serializable
sealed interface ScreenRoute {

    @Serializable
    data object PermissionScreen : ScreenRoute

    @Serializable
    data object PhotoGridScreen : ScreenRoute

    @Serializable
    data class PhotoTagScreen(val photoUri: String) : ScreenRoute
}