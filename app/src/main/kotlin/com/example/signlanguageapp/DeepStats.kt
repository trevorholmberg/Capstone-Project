/**
 * This class defines the in-depth stats screen for the sign language app.
 * It shows detailed statistics for the user's performance across multiple categories.
 *
 * @author Trevor Holmberg
 * @version Spring 2025
 */

package com.example.signlanguageapp

import android.R
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.signlanguageapp.ui.theme.SignLanguageAppTheme

/**
 * Activity that displays detailed user statistics for different question types.
 */
class DeepStats : ComponentActivity() {

    // Database helper for retrieving user statistics
    private lateinit var databaseHelper: DatabaseHelper

    // Currently logged-in user's name
    private lateinit var user: String

    private var theme = ThemeHelper.currTheme

    /**
     * Called when the activity is starting.
     * Initializes the user information and sets up the UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = UserProfile.UserPreferenceManager.getSavedUserPreference(this) ?: "Guest"
        databaseHelper = DatabaseHelper(this)
        enableEdgeToEdge()
        setContent()
    }

    override fun onResume() {
        super.onResume()
        this.theme = ThemeHelper.currTheme
        setContent()
    }

    // Set the UI content using Jetpack Compose
    private fun setContent() {
        setContent {
            SignLanguageAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DeepStatsContent(
                        modifier = Modifier.padding(innerPadding),
                        theme = this.theme
                    )
                }
            }
        }
    }

    /**
     * A composable function that defines the content of the stats screen.
     * Displays progress indicators and numerical values for each type of question (Multiple Choice, Matching, Spelling).
     *
     * @param modifier Modifier used to adjust the layout and appearance of the content.
     */
    @Composable
    fun DeepStatsContent(modifier: Modifier = Modifier, theme: Themes) {

        val bkgColor: Color;
        val txtColor: Color;
        val buttonTxtColor: Color;

        when (theme) {
            Themes.LightTheme -> {bkgColor = Color.White;
                buttonTxtColor = Color.LightGray;
                txtColor = Color.Black}
            Themes.DarkTheme -> {bkgColor= Color.DarkGray
                buttonTxtColor = Color.White
                txtColor = Color.LightGray;}
        }


        // Indexes for each question type in the stats data
        val MC = 1       // Multiple Choice
        val MATCH = 2    // Matching
        val SPELL = 3    // Spelling

        // Indexes for correct and total answers
        val CORRECT = 0
        val TOTAL = 1

        // Fetch user statistics from the database
        val stats = databaseHelper.getUserStats(user)

        // Retrieve correct and total counts for each question type
        val correctMC = stats[MC][CORRECT]
        val totalMC = stats[MC][TOTAL]
        val correctMatch = stats[MATCH][CORRECT]
        val totalMatch = stats[MATCH][TOTAL]
        val correctSpell = stats[SPELL][CORRECT]
        val totalSpell = stats[SPELL][TOTAL]

        // Calculate progress values (between 0 and 1) for each category
        val progressMC = if (totalMC > 0)
                (correctMC.toFloat() / totalMC.toFloat()).coerceIn(0f, 1f) else 0f
        val progressMatch = if (totalMatch > 0)
                (correctMatch.toFloat() / totalMatch.toFloat()).coerceIn(0f, 1f) else 0f
        val progressSpell = if (totalSpell > 0)
                (correctSpell.toFloat() / totalSpell.toFloat()).coerceIn(0f, 1f) else 0f

        // Calculate percentage values for display
        val percentageMC = (progressMC * 100).toInt()
        val percentageMatch = (progressMatch * 100).toInt()
        val percentageSpell = (progressSpell * 100).toInt()
        val CircularIndicatorSize = 150.dp
        val CircularStrokeWidth = 12.dp
        val OuterPadding = 32.dp
        val SectionSpacing = 20.dp
        val TextSpacing = 12.dp
        val HeaderSpacing = 16.dp
        val SpaceHeight = 20.dp

        // Layout for the stats screen
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(bkgColor)
                .padding(OuterPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Display the user's name at the top
            Text(
                text = "$user's Stats",
                color = txtColor,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = HeaderSpacing)
            )

            Spacer(modifier = Modifier.height(HeaderSpacing))

            // Multiple Choice stats display
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(CircularIndicatorSize),
                    progress = progressMC,
                    strokeWidth = CircularStrokeWidth
                )
                Text(
                    text = "$percentageMC%",
                    color = txtColor,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }

            // Spacer to separate sections
            Spacer(modifier = Modifier.height(SpaceHeight))

            // Display numerical stats for Multiple Choice
            Text(
                text = "Correct Multiple Choice: $correctMC",
                color = txtColor,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Total Multiple Choice: $totalMC",
                color = txtColor,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(TextSpacing))

            // Matching stats display
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(CircularIndicatorSize),
                    progress = progressMatch,
                    strokeWidth = CircularStrokeWidth
                )
                Text(
                    text = "$percentageMatch%",
                    color = txtColor,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }

            //Create space so the text is under the ring
            Spacer(modifier = Modifier.height(SpaceHeight))

            // Display numerical stats for Matching
            Text(
                text = "Correct Matching: $correctMatch",
                color = txtColor,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Total Matching: $totalMatch",
                color = txtColor,
                style = MaterialTheme.typography.bodyLarge
            )

            //create space so the text and the button have space
            Spacer(modifier = Modifier.height(SpaceHeight))

            // Spelling stats display
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(CircularIndicatorSize),
                    progress = progressSpell,
                    strokeWidth = CircularStrokeWidth
                )
                Text(
                    text = "$percentageSpell%",
                    style = MaterialTheme.typography.headlineSmall,
                    color = txtColor,
                    textAlign = TextAlign.Center
                )
            }

            //create space so the text is under the ring
            Spacer(modifier = Modifier.height(SpaceHeight))

            // Display numerical stats for Spelling
            Text(
                text = "Correct Spelling: $correctSpell",
                color = txtColor,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Total Spelling: $totalSpell",
                style = MaterialTheme.typography.bodyLarge,
                color = txtColor
            )

            //create space so the text and the button have space
            Spacer(modifier = Modifier.height(SpaceHeight))
        }
    }
}