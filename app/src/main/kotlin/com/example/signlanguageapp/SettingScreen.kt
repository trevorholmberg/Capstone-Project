/**
 * This class defines the settings screen for the sign language app
 * @author Matthew Talle
 * @version Fall 2024
 */

package com.example.signlanguageapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.signlanguageapp.ui.theme.Purple80
import com.example.signlanguageapp.ui.theme.SignLanguageAppTheme



class SettingScreen : ComponentActivity() {


    /**
     * Called when the activity is starting. Sets up the UI content
     * and the custom theme defined for the application.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent()
    }

    /**
     * Sets the content of the activity.
     */
    private fun setContent() {
        setContent {
            SignLanguageAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SettingScreenContent(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    /**
     * Restarts the activity
     */
    private fun restart(){
        this.recreate()
    }
    /**
     * A composable function that defines the content of the settings screen.
     * Displays a title and a button to toggle dark mode.
     *
     * @param modifier used to adjust the layout and appearance of the content.
     */
    @Composable
    fun SettingScreenContent(modifier: Modifier = Modifier) {
        val bottomPadding = 16
        val bkgColor: Color;
        val txtColor: Color;
        val buttonTxt: String;
        val buttonTxtColor: Color;
        val largePadding = 128

        when (ThemeHelper.currTheme) {
            Themes.LightTheme -> {bkgColor = Color.White;
                txtColor = Color.Black;
                buttonTxt = "Dark Mode";
                buttonTxtColor = Color.LightGray;}
            Themes.DarkTheme -> {bkgColor= Color.DarkGray
                txtColor = Color.LightGray;
                buttonTxt = "Light Mode";
                buttonTxtColor = Color.White;  }
        }
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(bkgColor)// Fills the entire available space.
                .padding(largePadding.dp), // Adds padding around the content.
            horizontalAlignment = Alignment.CenterHorizontally // Centers the content horizontally

        ) {

            Text(
                text = "Settings",  //Displays the title of the screen.
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = bottomPadding.dp),
                color = txtColor
            )
            Button(onClick = {
                if (ThemeHelper.currTheme == Themes.LightTheme){
                    ThemeHelper.setDark()
                } else {
                    ThemeHelper.setLight()
                }
                restart(); //resets the activity to apply the new theme
            },
                modifier = Modifier.padding(bottom = bottomPadding.dp)
            )
            {
                Text(
                    text = buttonTxt, // Button label
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Justify,
                    color = buttonTxtColor
                )
            }
        }
    }

}

