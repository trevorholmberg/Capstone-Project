/**
 * This class defines the camera screen for the sign language app
 * @author Matthew Agudelo, Matthew Talle, Trevor Holmberg
 * @version Fall 2024
 */


package com.example.signlanguageapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.ImageCapture.OnImageCapturedCallback
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.signlanguageapp.ui.theme.SignLanguageAppTheme
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import java.util.Locale
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class CameraScreen : ComponentActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech


    /**
     * Called when the activity is starting. Sets up the UI content
     * and the custom theme defined for the application.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!hasPermissions()){
            ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS,0)
        }
        setContent(ThemeHelper.currTheme)
    }// end of onCreate

    override fun onResume() {
        super.onResume()
        setContent(ThemeHelper.currTheme)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun setContent(theme: Themes){
        setContent {
            SignLanguageAppTheme {
                val state = rememberBottomSheetScaffoldState()
                val scope = rememberCoroutineScope()

                val controller = remember {
                    // end camera when user closes or leaves screen
                    LifecycleCameraController(applicationContext).apply {
                        setEnabledUseCases(
                            // other options as what the camera does image, video etc
                            CameraController.IMAGE_CAPTURE
                        )
                    }
                } // needed to register the  video from camera
                //parameter for Photo orientation
                val viewModel = viewModel<MainViewModel>()
                val bitmaps by viewModel.bitmaps.collectAsState()
                val modelText = remember { mutableStateOf("") }

                // Initialize a TextToSpeech object that is run through this screen
                tts = TextToSpeech(this, this)
                val interpreter = InterpreterHelper(this);

                val bkgColor: Color;
                val txtColor: Color;
                val buttonTxtColor: Color;

                when (theme) {
                    Themes.LightTheme -> {bkgColor = Color.White;
                        buttonTxtColor = Color.LightGray;
                        txtColor = Color.Black}
                    Themes.DarkTheme -> {bkgColor= Color.Black
                        buttonTxtColor = Color.White
                        txtColor = Color.LightGray;}
                }

                BottomSheetScaffold(
                    scaffoldState = state,
                    sheetPeekHeight = 0.dp,
                    sheetContent = {
                        PhotoBottomPreview(
                            bitmaps = bitmaps,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                ) { padding ->
                    // Box for the phone size
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        // Inside the Box that has the camera another box than is a black screen
                        // that has  buttons and will give output and clear
                        // to get the 2/3 and 1/3 spit used weights
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Box for the camera feed
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(2f)
                            ) {
                                CameraPreview(
                                    controller = controller,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }// end of box for camera feed


                            // Box for output
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(bkgColor)
                            )

                            {
                                // text in the bottom box
                                Text(
                                    text = "output:",
                                    color = txtColor,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(16.dp)
                                )
                                //text under output
                                Text(
                                    text = modelText.value,
                                    color = txtColor,
                                    fontSize = 18.sp,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(start = 16.dp, top = 50.dp)
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomEnd),
                                    horizontalArrangement = Arrangement.End
                                ){
                                    //Button when pressed will take a photo
                                    Button(
                                        onClick = {

                                            // Takes camera photo
                                            interpreter.takePhoto(
                                                controller = controller,
                                                onPhotoTaken = viewModel::onTakePhoto
                                            )

                                            // Creates separate thread whenever camera takes a photo
                                            // Photo gets passed in to model for prediction -MT
                                            lifecycleScope.launch {
                                                modelText.value =
                                                    interpreter.takeModel(controller = controller)
                                            }
                                        },
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text(text = " Take Photo", color = buttonTxtColor)
                                    }

                                    //TTS button
                                    Button(
                                        onClick = {
                                            // Pass text from the model to the speakOut function
                                            speakOut(modelText.value)
                                        }
                                    ) {
                                        Text(text = "Speak", color = buttonTxtColor)
                                    }

                                    //Button to show what the photo looks like
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                state.bottomSheetState.expand()
                                            }
                                        }
                                    ) {
                                        Text(text = "Images", color = buttonTxtColor)
                                    }
                                }// end of row for buttons
                            } //end of box for output
                        }// end of entire column
                    }// End of box for camera feed
                } // end of padding
            }// end of theme
        }
    }


    /**
     * check for permissions for opening camera
     */
    private fun hasPermissions(): Boolean{
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it )  == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * request permission for the app to use camera if check is false
     */
    companion object {
        val CAMERAX_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

    /**
     * Take a string as a parameter then use the TTS object to have the phone speakers read the
     * string out loud for the user to hear.
     * @param text The text that will be read
     */
    private fun speakOut(text: String) {
        //Speak the text out loud, empty the queue, and have no special parameters
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }


    /**
     * Called when the activity is destroyed to properly shutdown the TTS object before runniug the
     * super that dose the final cleanup.
     */
    override fun onDestroy() {
        //stop the TTS object when activity is destroyed
        if(tts.isSpeaking){tts.stop()}
        // Properly shutdown the TTS object
        tts.shutdown()
        super.onDestroy()
    }


    /**
     * Ensure the TTS object was initialized successfully and set the language.
     * @param status Tells the function if there is an error initializing TTS
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // set the language for the tts to US English
            val result = tts.setLanguage(Locale.US)
            //Ensure language was set correctly
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Handle the error of improper language being set
                Log.e("TTS", "Language not supported or missing data")
            }
        } else {
            // Initialization failed
            Log.e("TTS", "Initialization failed")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    SignLanguageAppTheme {

    }
}