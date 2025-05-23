/**
 * This class defines the main stats screen for the sign language app.
 * Displays overall user statistics and allows navigation to detailed stats.
 *
 * @author Trevor Holmberg
 * @version Spring 2025
 */

package com.example.signlanguageapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.signlanguageapp.ui.theme.SignLanguageAppTheme

/**
 * Activity responsible for displaying the user's overall statistics.
 * Also provides navigation to a deeper stats screen and functionality to
 * reset the user's stats.
 */
class StatScreen : ComponentActivity() {

    // Database helper for fetching and resetting user statistics
    private lateinit var databaseHelper: DatabaseHelper

    // Currently logged-in user's name
    private lateinit var user: String

    // Holds the current theme (Light/Dark)
    private var theme = ThemeHelper.currTheme

    /**
     * Called when the activity is starting.
     * Initializes database helper, retrieves user preference, and sets up the UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseHelper = DatabaseHelper(this)
        user = UserProfile.UserPreferenceManager.getSavedUserPreference(this) ?: "Guest"
        enableEdgeToEdge()
        setContent()
    }

    /**
     * Called when the activity becomes visible to the user again.
     * Ensures the theme is updated in case it was changed elsewhere.
     */
    override fun onResume() {
        super.onResume()
        this.theme = ThemeHelper.currTheme
        setContent()
    }

    /**
     * Helper function to set the content using Jetpack Compose.
     * Applies theming and padding to the main content.
     */
    private fun setContent() {
        setContent {
            SignLanguageAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    StatScreenContent(
                        modifier = Modifier.padding(innerPadding),
                        theme = this.theme
                    )
                }
            }
        }
    }

    /**
     * A composable function that defines the content of the stats screen.
     * Displays overall progress (user and all users), and allows resetting
     * stats or navigating to deeper stats.
     *
     * @param modifier Modifier used to adjust layout and spacing.
     * @param theme Current theme (Light or Dark) used for dynamic colors.
     */
    @Composable
    fun StatScreenContent(modifier: Modifier = Modifier, theme: Themes) {
        rememberCoroutineScope()
        val context = LocalContext.current

        // Index for overall stats in the stats array
        val OVERALL = 0

        // Indexes for correct answers and total attempts
        val CORRECT = 0
        val TOTAL = 1

        // Fetch overall user stats
        val stats = databaseHelper.getUserStats(user)
        val correct = stats[OVERALL][CORRECT]
        val total = stats[OVERALL][TOTAL]

        // Calculate user progress (0f to 1f) and percentage
        val progress = if (total > 0) correct.toFloat() / total.toFloat() else 0f
        val percentage = (progress * 100).toInt()

        // Fetch overall stats for all users
        val allUsers = databaseHelper.getAllOverallStats()
        val dbCorrect = allUsers[CORRECT]
        val dbTotal = allUsers[TOTAL]

        // Calculate all users' progress and percentage
        val dbProgress = if (dbTotal > 0) dbCorrect.toFloat() / dbTotal.toFloat() else 0f
        val dbPercentage = (dbProgress * 100).toInt()

        // Set background and text colors based on the current theme
        val bkgColor: Color
        val txtColor: Color
        val buttonTxtColor: Color
        val smallPadding = 16
        val smallSpace = 20
        val largeModifier = 150
        val smallStroke = 12

        when (theme) {
            Themes.LightTheme -> {
                bkgColor = Color.White
                buttonTxtColor = Color.LightGray
                txtColor = Color.Black
            }
            Themes.DarkTheme -> {
                bkgColor = Color.DarkGray
                buttonTxtColor = Color.White
                txtColor = Color.LightGray
            }
        }

        // Main content layout
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp)
                .background(bkgColor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with user's name
            Text(
                text = "$user's Stats",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = smallPadding.dp),
                color = txtColor
            )

            // User-specific progress indicator with percentage inside
            Box(contentAlignment = Alignment.Center) {
                // Invisible button for navigation to deeper stats when clicked
                Button(
                    onClick = {
                        val deepStats = Intent(context, DeepStats::class.java)
                        context.startActivity(deepStats)
                    },
                    modifier = Modifier.size(largeModifier.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) { /* Empty content to allow clickable ring */ }

                CircularProgressIndicator(
                    modifier = Modifier.size(largeModifier.dp),
                    progress = progress,
                    strokeWidth = smallStroke.dp
                )

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }

            // Spacer for separation
            Spacer(modifier = Modifier.height(smallSpace.dp))

            // Display numerical user stats
            Text(
                text = "Correct: $correct",
                style = MaterialTheme.typography.bodyLarge,
                color = txtColor
            )
            Text(
                text = "Total: $total",
                style = MaterialTheme.typography.bodyLarge,
                color = txtColor
            )

            Spacer(modifier = Modifier.height(smallPadding.dp))

            // Header for all users' statistics section
            Text(
                text = "All Users' Stats",
                style = MaterialTheme.typography.headlineMedium,
                color = txtColor,
                modifier = Modifier.padding(bottom = smallPadding.dp)
            )

            // All users' progress indicator with percentage inside
            Box(contentAlignment = Alignment.Center) {
                Button(
                    onClick = {

                    },
                    modifier = Modifier.size(largeModifier.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) { }
                CircularProgressIndicator(
                    modifier = Modifier.size(largeModifier.dp),
                    progress = dbProgress,
                    strokeWidth = smallStroke.dp
                )
                Text(
                    text = "$dbPercentage%",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(smallSpace.dp))

            // Display numerical stats for all users
            Text(
                text = "Correct: $dbCorrect",
                style = MaterialTheme.typography.bodyLarge,
                color = txtColor
            )
            Text(
                text = "Total: $dbTotal",
                style = MaterialTheme.typography.bodyLarge,
                color = txtColor
            )

            Spacer(modifier = Modifier.height(smallPadding.dp))

            // Button to reset the current user's stats
            Button(
                onClick = {
                    databaseHelper.resetStats(user)
                }
            ) {
                Text(text = "Reset this user", color = buttonTxtColor)
            }
        }
    }
}
