package com.example.signlanguageapp
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for managing the list of bitmaps (photos) in the app.
 *
 * This ViewModel holds a StateFlow that represents the current list of photos as bitmaps
 * It exposes a read-only StateFlow so user can see the image as it is taken
 * while the `onTakePhoto` function allows adding new bitmaps to the list.
 */
class MainViewModel: ViewModel(){
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps = _bitmaps.asStateFlow()
    private var count = 0

    fun onTakePhoto(bitmap: Bitmap) {
            _bitmaps.value += bitmap
            Log.d("MainViewModel", "countss = $count")
            count++

        }

    }

