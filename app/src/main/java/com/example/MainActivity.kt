package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.repository.LandslideRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.ui.LandslideApp
import com.example.ui.LandslideViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  private val viewModel: LandslideViewModel by viewModels {
    object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val userPrefs = UserPreferencesRepository(applicationContext)
        val repo = LandslideRepository()
        return LandslideViewModel(repo, userPrefs) as T
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val isDarkTheme by viewModel.isDarkTheme.collectAsState()
      MyApplicationTheme(darkTheme = isDarkTheme) {
        LandslideApp(viewModel = viewModel)
      }
    }
  }
}

