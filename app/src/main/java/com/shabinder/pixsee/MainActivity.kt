package com.shabinder.pixsee

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.shabinder.pixsee.ui.theme.PixseeTheme
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PixseeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        // TODO
                    }
                }
            }
        }
    }
}
