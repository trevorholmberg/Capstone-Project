/**
 * Helper class that handles all the machine learning model related tasks
 * such as interpreting the user input
 * @author Matthew Talle
 */

package com.example.signlanguageapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class InterpreterHelper(val context: Context) {

    val model: Module = LiteModuleLoader.load(assetFilePath("letter_classifier.ptl"));

    /**
     * This helper function receives an index pertaining to the max value of a float array
     * representation of a tensor and then converts it to ascii representation
     * @author MT
     */
    private fun predictLetter(index: Int): String? {
        val offset = 65 + index
        return when {
            index < 26 -> offset.toChar().toString()
            index == 26 -> "delete"
            index == 27 -> "empty"
            index == 28 -> "space"
            else -> null
        }
    }

    /**
     * Takes a photo using the provided LifecycleCameraController and processes the captured image.
     *
     * This function initiates the camera capture process and handles the success or failure of
     * the photo capture. When the photo is successfully captured,
     * it rotates the image according
     * to the device's orientation
     * passes the rotated image as a `Bitmap` to the provided callback.
     *
     * @param controller A `LifecycleCameraController` responsible for handling camera operations.
     * @param onPhotoTaken A callback that is called with the rotated
     *        Bitmap` once the photo is successfully taken.
     */
    fun takePhoto(controller: LifecycleCameraController,
                          onPhotoTaken: (Bitmap) -> Unit) {
        controller.takePicture(
            ContextCompat.getMainExecutor(context.applicationContext),
            object : OnImageCapturedCallback(){
                override fun onCaptureSuccess(image: ImageProxy) {
                    super.onCaptureSuccess(image)
                    //Orient Photo
                    val matrix = Matrix().apply{
                        postRotate(image.imageInfo.rotationDegrees.toFloat())
                    }
                    val rotatedBitmap = Bitmap.createBitmap(
                        image.toBitmap(),
                        0,
                        0,
                        image.width,
                        image.height,
                        matrix,
                        true)
                    onPhotoTaken(rotatedBitmap)
                    image.close()//added to fix 4 image limit - Trevor
                }
                override fun onError(exception: ImageCaptureException) {
                    super.onError(exception)
                    Log.e("Camera", "Could not take photo: ", exception)
                }
            }
        )
    }

    /**
     * Captures an image using the camera, processes it, and
     * passes it to a machine learning model for prediction.
     *
     * This function uses the `LifecycleCameraController` to take a photo,
     * processes the photo by rotating,
     * resizing, and normalizing it into a format suitable for the model.
     * It then passes the processed image as a tensor to the model
     * and retrieves the predicted letter based on the model’s output.
     * The result is returned as a string of the predicted letter.
     *
     * @param controller The `LifecycleCameraController` responsible for handling the camera and
     * capturing the image.
     * @return A  String representing the predicted letter based on the captured image.
     */
    suspend fun takeModel(controller: LifecycleCameraController): String =
        suspendCoroutine { continuation ->
            controller.takePicture(
                ContextCompat.getMainExecutor(context.applicationContext),
                object : OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        super.onCaptureSuccess(image)
                        //Orient Photo
                        val matrix = Matrix().apply{
                            postRotate(image.imageInfo.rotationDegrees.toFloat())
                        }
                        val rotatedBitmap = Bitmap.createBitmap(
                            image.toBitmap(),
                            0,
                            0,
                            image.width,
                            image.height,
                            matrix,
                            true)

                        // Resize the bitmap to match model input size -MT
                        val modelInputWidth = 128
                        val modelInputHeight = 128
                        val resizedBitmap = Bitmap.createScaledBitmap(
                            rotatedBitmap, modelInputWidth, modelInputHeight, true)

                        // Convert bitmap to a normalized float array -MT
                        val inputTensor = bitmapToFloat32Tensor(resizedBitmap)

                        // Pass the tensor to the model -MT
                        val input = IValue.from(inputTensor)

                        // Convert into Tensor then into float array representation -MT
                        val outputArray = model.forward(input).toTensor().dataAsFloatArray

                        // retrieve max value in the output
                        // array which is the score of accuracy for prediction -MT
                        val maxValue = outputArray.maxOrNull();

                        // retrieve the index of the max value
                        // which corresponds to the letter predicted -MT
                        val maxIndex = outputArray.indexOfFirst { it == maxValue }

                        // Log the output (for debugging purposes) -MT
                        Log.d("Predicted letter: ", predictLetter(maxIndex).toString())

                        image.close()

                        // Convert prediction into its classification name -MT
                        val letter = predictLetter(maxIndex).toString()

                        // return classification -MT
                        continuation.resume(letter)
                    }
                    override fun onError(exception: ImageCaptureException) {
                        super.onError(exception)
                        Log.e("Camera", "Could not take photo: ", exception)
                    }

                }
            )
        }

    /**
     * Helper function to convert a Bitmap to a PyTorch Float32 Tensor
     *
     * This essentially does what the transform library's toTensor function does but I had to write
     * it out manually as I do not have access to that library from here
     * @author MT
     */
    private fun bitmapToFloat32Tensor(bitmap: Bitmap): Tensor {
        val width = bitmap.width
        val height = bitmap.height

        val floatArray = FloatArray(3 * width * height)
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Normalize the pixel values and rearrange into [channel, height, width]
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = pixels[y * width + x]
                val r = ((pixel shr 16) and 0xFF) / 255.0f // Red channel
                val g = ((pixel shr 8) and 0xFF) / 255.0f  // Green channel
                val b = (pixel and 0xFF) / 255.0f          // Blue channel

                val baseIndex = y * width + x
                floatArray[baseIndex] = r // Channel 0
                floatArray[baseIndex + width * height] = g // Channel 1
                floatArray[baseIndex + 2 * width * height] = b // Channel 2
            }
        }
        // Return a Tensor with shape [1, 3, height, width]
        return Tensor.fromBlob(floatArray, longArrayOf(1, 3, height.toLong(), width.toLong()))
    }

    /**
     *  Retrieves the absolute file path for a given asset by copying it from the app's assets
     *  directory to the device's internal file storage if it does not already exist.
     *  @param assetName A string that holds the path to the file
     *  @author MT
     */
    private fun assetFilePath(assetName: String): String {
        // Create a reference to the target file in internal storage.
        val file = File(context.filesDir, assetName)

        // Check if the file already exists in internal storage. If not retrieve from bundled
        // assets folder and write to internal storage
        if (!file.exists()) {
            context.assets.open(assetName).use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(4 * 1024)
                    var read: Int
                    while (inputStream.read(buffer).also { read = it } != -1) {
                        outputStream.write(buffer, 0, read)
                    }
                }
            }
        }
        return file.absolutePath
    }



}