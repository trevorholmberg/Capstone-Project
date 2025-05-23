package com.example.signlanguageapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.signlanguageapp.ui.theme.SignLanguageAppTheme
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
// For user stats
import com.example.signlanguageapp.UserProfile.UserPreferenceManager

class MultipleChoice : ComponentActivity() {
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var user: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Need this to get user stats and
        databaseHelper = DatabaseHelper(this)
        user = UserPreferenceManager.getSavedUserPreference(this) ?: "Guest"

        enableEdgeToEdge()
        setContent {

            SignLanguageAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MultipleChoiceScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }


    /**
     * function to load a random letter from the letter_file.txt
     * it will display a random letter from the file to the screen for the user to
     * choose and will give feedback based on chosen answer.
     */
    @Composable
    fun MultipleChoiceScreen(
        modifier: Modifier = Modifier) {
        val fontSmall = 18.sp
        val fontLarge = 30.sp
        val correct = 1
        val incorrect = 1
        val noUpdate = 0
        val smallPadding = 16.dp
        val largePadding = 60.dp
        val largeSize = 250.dp
        val spaceHeight = 20.dp
        val scope = rememberCoroutineScope()
        val QuizType = "Multiple Choice"

        val context = LocalContext.current
        val imageList = remember {
            loadImageListFromAssets(context, "to_test/letter_file.txt") }


        var randomImage by remember { mutableStateOf(imageList.randomOrNull() ?: "") }
        var bitmap by remember {
            mutableStateOf(
                loadBitmapFromAssets(
                    context,
                    "to_test/$randomImage"
                )
            )
        }

        var options by remember {
            mutableStateOf(generateOptions(randomImage, imageList))
        }
        //new
        var answered by remember { mutableStateOf(false) }
        var feedbackMessage by remember { mutableStateOf("") }
        val correctAnswer = randomImage.removeSuffix(".jpg").uppercase()

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxSize()
        ) {
            Text(
                text = "Choose the correct answer",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = largePadding),
                style = TextStyle(
                    fontSize = fontLarge
                )
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Choose the correct answer",
                        modifier = Modifier.fillMaxWidth().padding(smallPadding).size(largeSize)
                    )
                } ?: Text("Image not found")

                Spacer(modifier = Modifier.height(spaceHeight))

                options.forEach { option ->
                    Button(
                        onClick = {
                            if (!answered) {
                                answered = true
                                scope.launch {
                                    if (option == correctAnswer) {
                                        feedbackMessage = "Correct answer!"
                                        databaseHelper.updateStats(
                                            user, QuizType, correct, incorrect)
                                        databaseHelper.updateStats(
                                            user, "Total", correct, incorrect)
                                    } else {
                                        feedbackMessage =
                                            "Incorrect Answer: You chose $option." +
                                                    " Correct answer is $correctAnswer"
                                        databaseHelper.updateStats(
                                            user, QuizType, noUpdate, incorrect)
                                        databaseHelper.updateStats(
                                            user, "Total", noUpdate, incorrect)

                                    }
                                }
                            }
                        },
                        enabled = !answered // disable buttons, so updates happens once
                    ) {
                        Text(option)
                    }
                }
                if (feedbackMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(smallPadding))
                    Text(
                        text = feedbackMessage,
                        fontSize = fontSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (feedbackMessage.startsWith("Correct"))
                            Color.Green else Color.Red

                    )

                }
            }
            Button(
                onClick = {
                    randomImage = imageList.randomOrNull() ?: ""
                    bitmap = loadBitmapFromAssets(context, "to_test/$randomImage")
                    options = generateOptions(randomImage, imageList)
                    answered = false
                    feedbackMessage = ""
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(smallPadding)
            ) {
                Text("Next")
            }
        }
    }
}

/**
 * Function to get a letter from the image list
 */
fun loadImageListFromAssets(context: Context, filePath: String): List<String> {
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
 * function to open the letter .jpg file from the
 * letter that was chosen in the loadImageListFromAssets
 */
fun loadBitmapFromAssets(context: Context, filePath: String): Bitmap? {
    return try {
        context.assets.open(filePath).use { inputStream ->
            BitmapFactory.decodeStream(inputStream)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * This function generates 4 answers, 3 are incorrect and 1 is the image displayed
 * afterwords the answers are shuffled so they are not in the same position
 */
fun generateOptions(correctAnswer: String, imageList: List<String>): List<String> {
    val toTake = 3
    val correctAnswerCut = correctAnswer.removeSuffix(".jpg").uppercase()
    val wrongAnswers = imageList.filter { it != correctAnswer }
        .shuffled()
        .take(toTake)
        .map { it.removeSuffix(".jpg").uppercase() }
    return (wrongAnswers + correctAnswerCut).shuffled()
}

