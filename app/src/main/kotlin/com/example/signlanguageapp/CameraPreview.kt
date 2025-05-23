package com.example.signlanguageapp

import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView

/**
 * CameraPreview composable function that displays the camera preview using a `PreviewView`.
 *
 * This function binds the provided `LifecycleCameraController` to the lifecycle of the current
 * activity or fragment, ensuring proper management of camera resources.
 *
 * @param controller A `LifecycleCameraController` that handles camera operations. It is bound
 *                   to the lifecycle of the current owner.
 * @param modifier A Modifier applied to the `PreviewView`
 */
@Composable
fun CameraPreview(
    controller:LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    val lifeCycleOwner = LocalLifecycleOwner.current
   AndroidView(
       factory = {PreviewView(it).apply {
           this.controller = controller
           controller.bindToLifecycle(lifeCycleOwner)

       }
       },
       modifier = modifier
   )
}