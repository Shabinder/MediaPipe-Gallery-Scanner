package com.shabinder.pixsee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.shabinder.pixsee.ScreenRoute.PermissionScreen
import com.shabinder.pixsee.ScreenRoute.PhotoGridScreen
import com.shabinder.pixsee.ScreenRoute.PhotoTagScreen
import com.shabinder.pixsee.ui.screens.PermissionScreenUI
import com.shabinder.pixsee.ui.screens.PhotoGridScreenUI
import com.shabinder.pixsee.ui.screens.PhotoTagScreenUI
import com.shabinder.pixsee.ui.theme.PixseeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PixseeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    val navController = rememberNavController()

                    val startScreen = if (viewModel.isPermissionGranted()) {
                        PhotoGridScreen
                    } else {
                        PermissionScreen
                    }

                    NavHost(
                        navController = navController,
                        startDestination = startScreen,
                        modifier = Modifier.padding(padding)
                    ) {
                        composable<PermissionScreen> {
                            PermissionScreenUI(
                                onPermissionGranted = {
                                    navController.navigate(PhotoGridScreen) {
                                        popUpTo(PermissionScreen) {
                                            inclusive = true
                                        }
                                    }
                                }
                            )
                        }

                        composable<PhotoGridScreen> {
                            PhotoGridScreenUI(
                                viewModel = viewModel,
                                goToImage = {
                                    navController.navigate(PhotoTagScreen(it.photoData.uri))
                                }
                            )
                        }

                        composable<PhotoTagScreen> { backStackEntry ->
                            val data: PhotoTagScreen = backStackEntry.toRoute()
                            PhotoTagScreenUI(
                                uri = data.photoUri,
                                viewModel = viewModel,
                                onBack = navController::popBackStack
                            )
                        }
                    }
                }
            }
        }
    }
}
