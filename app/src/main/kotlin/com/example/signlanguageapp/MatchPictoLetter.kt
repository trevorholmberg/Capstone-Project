package com.example.signlanguageapp


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.dataStore
import com.example.signlanguageapp.UserProfile.UserPreferenceManager
import com.example.signlanguageapp.ui.theme.SignLanguageAppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader


class MatchPictoLetter : ComponentActivity() {
    private lateinit var user: String
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseHelper = DatabaseHelper(this)
        user = UserPreferenceManager.getSavedUserPreference(this) ?: "Guest"

        enableEdgeToEdge()
        setContent {
            SignLanguageAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MatchPicture(
                        modifier = Modifier.padding(innerPadding)
                    )

                }
            }
        }
    }

    /**
     * Composable function that displays a picture matching game.
     * A random image is shown which the user can drag into one of four answer boxes.
     * The correct answer must be matched based on the label of the image.
     *
     * Stats are updated using a database helper after each attempt.
     *
     * @param modifier Optional for styling and layout control.
     */
    @Composable
    fun MatchPicture(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var position by remember { mutableStateOf(Offset(0f, 0f)) }
        var options by remember { mutableStateOf(listOf<String>()) }
        var correctAnswer by remember { mutableStateOf("") }
        var resultMessage by remember { mutableStateOf("") }
        val boxPositions = remember { mutableStateListOf<Rect>() }
        val imageWidth = with(LocalDensity.current) { 150.dp.toPx() }
        val imageHeight = with(LocalDensity.current) { 150.dp.toPx() }
        val correctnum = 1
        val incorrectnumstat = 1
        val incorrectnumZ = 0
        val weightF = 1f
        val inflatenumF = 0f
        val optionSize = 4
        val mult = 2
        val largeFont = 24
        val largeOffset = 150
        val smallPading = 16

        // Use mutable state variables for stats
        var correct by remember { mutableIntStateOf(0) }
        var total by remember { mutableIntStateOf(0) }
        var correctMatch by remember { mutableIntStateOf(0) }
        var totalMatch by remember { mutableIntStateOf(0) }
        val takeOption = 3


        fun loadRandomImage() {
            val imageList = loadImageListFromAsset(context, "to_test/letter_file.txt")
            val randomImageWithExt = imageList.randomOrNull()
            val randomImage = randomImageWithExt?.substringBeforeLast(".")?.uppercase()

            if (randomImageWithExt != null && randomImage != null) {
                imageBitmap = loadBitmapFromAsset(context, "to_test/$randomImageWithExt")
                correctAnswer = randomImage
                val wrongAnswers =
                    imageList.filter {
                        it.substringBeforeLast(".").uppercase() != randomImage }
                        .shuffled()
                        .take(takeOption)
                        .map { it.substringBeforeLast(".").uppercase() }
                options = (wrongAnswers + randomImage).shuffled()
            }
        }

        LaunchedEffect(Unit) {
            loadRandomImage()
        }

        Box(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                for (row in incorrectnumZ..correctnum) {
                    Row(modifier = Modifier.weight(weightF)) {
                        for (col in incorrectnumZ..correctnum) {
                            val index = row * mult + col
                            Box(
                                modifier = Modifier
                                    .weight(weightF)
                                    .fillMaxSize()
                                    .background(Color.Gray)
                                    .border(col.dp, Color.Black)
                                    .onGloballyPositioned { coordinates ->
                                        //Bounds for box to check correctness
                                        val expandedBounds =
                                            coordinates.boundsInWindow().inflate(inflatenumF)
                                        if (index < boxPositions.size) {
                                            boxPositions[index] = expandedBounds
                                        } else {
                                            boxPositions.add(expandedBounds)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (options.size == optionSize) {
                                    Text(
                                        text = options[index],
                                        color = Color.White,
                                        fontSize = largeFont.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            imageBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Random Letter Image",
                    modifier = Modifier
                        .offset { IntOffset(position.x.toInt(), position.y.toInt()) }
                        .size(largeOffset.dp)
                        .pointerInput(Unit) {
                            detectDragGestures { _, pan ->
                                position = Offset(position.x + pan.x, position.y + pan.y)
                            }
                        }
                )
            }

            Text(
                text = resultMessage,
                color = Color.White,
                fontSize = largeFont.sp,
                modifier = Modifier.align(Alignment.TopCenter).padding(smallPading.dp)
            )

            Button(
                onClick = {
                    val imageRect = Rect(
                        position.x,
                        position.y,
                        position.x + imageWidth,
                        position.y + imageHeight
                    )
                    val correctIndex = options.indexOf(correctAnswer)
                    val correctBox = boxPositions.getOrNull(correctIndex)
                    val isCorrect = correctBox != null && correctBox.overlaps(imageRect)
                    val resultMessage = if (isCorrect) {
                        "Correct!"
                    } else {
                        "Incorrect answer is $correctAnswer"
                    }

                    if(isCorrect){
                        databaseHelper.updateStats(
                            user, "Matching", correctnum, incorrectnumstat)
                        databaseHelper.updateStats(
                            user, "Total", correctnum, incorrectnumstat)
                    }else{
                        databaseHelper.updateStats(
                            user, "Matching", incorrectnumZ, incorrectnumstat)
                        databaseHelper.updateStats(
                            user, "Total", incorrectnumZ, incorrectnumstat)
                    }




                    Toast.makeText(context, resultMessage.toString(), Toast.LENGTH_SHORT).show()
                    loadRandomImage()
                },
                modifier = Modifier.align(Alignment.BottomEnd).padding(smallPading.dp)
            ) {
                Text("Next Image")
            }
        }
    }
}




/**
 * Function to get a list of image names from assets.
 */
fun loadImageListFromAsset(context: Context, filePath: String): List<String> {
    return try {
        context.assets.open(filePath).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).readLines()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

/**
 * Function to load a bitmap from assets using the chosen letter file.
 */
fun loadBitmapFromAsset(context: Context, filePath: String): Bitmap? {
    return try {
        context.assets.open(filePath).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
