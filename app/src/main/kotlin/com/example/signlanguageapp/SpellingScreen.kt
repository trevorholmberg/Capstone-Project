/**
 * Class representing the spelling quiz screen for the application.
 * @author Matthew Talle
 */


package com.example.signlanguageapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.signlanguageapp.ui.theme.SignLanguageAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class SpellingScreen: ComponentActivity() {

    /** Instance of the Sqlite database */
    private lateinit var db: DatabaseHelper
    /** name of current user*/
    private lateinit var user: String
    /** Cursor pointing to the quiz data in database*/
    private lateinit var cursor: Cursor


    /**
     * Called when the activity is starting. Sets up the UI content
     * and the custom theme defined for the application.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState.
     */
    @SuppressLint("Range")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS, 0)
        }

        val interpreter = InterpreterHelper(this)

        db = DatabaseHelper.instanceOfDatabase(this)

        db.insert();

        user = UserProfile.UserPreferenceManager.getSavedUserPreference(this) ?: "Guest"

        cursor = db.getQuizData()!!

        cursor.moveToFirst()



        // UI creation
        setContent {
            SignLanguageAppTheme {
                val noHeight = 0
                val smallPadding = 16
                val mediumPadding = 50
                val extraSmallPadding = 8
                val smallWeight = 1f
                val largeWeight = 2f
                val smallFont = 18
                val stat1 = 1
                val stat2 = 0
                val state = rememberBottomSheetScaffoldState()

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

                DataStoreManager(this)

                BottomSheetScaffold(
                    scaffoldState = state,
                    sheetPeekHeight = noHeight.dp,
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
                                    .background(Color.Black)
                            )

                            {
                                // text in the bottom box
                                Text(
                                    text = cursor.getString(
                                        cursor.getColumnIndex(DatabaseHelper.QUESTION_FIELD)),
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(smallPadding.dp)
                                )
                                //text under output
                                Text(
                                    text = modelText.value,
                                    color = Color.White,
                                    fontSize = smallFont.sp,
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(start = smallPadding.dp, top = mediumPadding.dp)
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
                                                val input = interpreter.takeModel(
                                                    controller = controller)
                                                if (checkAnswer(input, modelText)){
                                                    // update correct stats
                                                    db.updateStats(
                                                        user, "Spelling", stat1, stat1)
                                                    db.updateStats(
                                                        user, "Total", stat1, stat1)
                                                }else{
                                                    // update incorrect stats
                                                    db.updateStats(
                                                        user, "Spelling", stat2, stat1)
                                                    db.updateStats(
                                                        user, "Total", stat2, stat1)
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .padding(end = extraSmallPadding.dp)
                                            .align(Alignment.Bottom)
                                    ) {
                                        Text(text = " Take Photo")
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
     * Function that checks a user's input against the correct answer then handles the cursor's
     * behavior relative to it.
     * @param input the user's input
     * @param modelText the correct text to be displayed
     */
    @SuppressLint("Range")
    private suspend fun checkAnswer(input: String, modelText: MutableState<String>): Boolean{
        // delay to put thread to sleep
        val longDelay: Long = 2000
        val shortDelay: Long = 1000

        val answerToCheck: CharArray = cursor.getString( // retrieves answer from database
            cursor.getColumnIndex(DatabaseHelper.ANSWER_FIELD)).toCharArray()

        if (input == answerToCheck[modelText.value.length].toString().uppercase(Locale.ROOT)){
            Toast.makeText(this, "Correct!!", Toast.LENGTH_SHORT).show()
            val toCompare = modelText.value.length
            modelText.value += input // increment each correct character answered to display

            if (answerToCheck.size == toCompare + 1){
                delay(longDelay)
                modelText.value = "next question in 3"
                delay(shortDelay)
                modelText.value = "next question in 2"
                delay(shortDelay)
                modelText.value = "next question in 1"
                delay(shortDelay)
                cursor.moveToNext()
                modelText.value = ""
            }

            return true

        } else {
            Toast.makeText(this, "Incorrect", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    /**
     * request permission for the app to use camera if check is false
     */
    companion object {
        private  val CAMERAX_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}