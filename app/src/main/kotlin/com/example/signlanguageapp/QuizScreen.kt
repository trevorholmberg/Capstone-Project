package com.example.signlanguageapp

import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.signlanguageapp.ui.theme.SignLanguageAppTheme
import androidx.compose.ui.platform.LocalContext



class QuizScreen: ComponentActivity() {
    /** points to the current theme of the app*/
    private var theme = ThemeHelper.currTheme

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent(this.theme)

    }

    /**
     * If theme is changed, update the theme and recreate the activity
     */
    override fun onResume() {
        super.onResume()
        this.theme = ThemeHelper.currTheme
        setContent(this.theme)
    }

    /**
     * Sets the content of the activity.
     */
    private fun setContent(theme: Themes){
        setContent {
            SignLanguageAppTheme {
                val context = LocalContext.current
                val question = remember { mutableStateOf("") }
                val smallPadding = 16
                val smallFont = 18
                val topPadding = 50
                val largeWidth= 200
                val smallHeight = 50
                val customPading = 325
                val customTopPadding = 400
                val customTopPad2 = 250


                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val bkgColor: Color;
                    val buttonTxtColor: Color;

                    when (theme) {
                        Themes.LightTheme -> {bkgColor = Color.White;
                            buttonTxtColor = Color.LightGray; }
                        Themes.DarkTheme -> {bkgColor= Color.DarkGray
                            buttonTxtColor = Color.White}
                    }

                    Box( //Use Box instead of Column
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(bkgColor),
                    ) {
                        Text(
                            text = question.value,
                            color = Color.Magenta,
                            fontSize = smallFont.sp,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(start = smallPadding.dp, top = topPadding.dp)
                        )
                        Button(
                            onClick = {
                                val spellIntent = Intent(context, SpellingScreen::class.java);
                                context.startActivity(spellIntent);
                            },
                            colors = ButtonDefaults.buttonColors(contentColor = Color.White),
                            modifier = Modifier
                                .padding(top = customTopPad2.dp)
                                .align(Alignment.TopCenter)
                                .width(largeWidth.dp)
                                .height(smallHeight.dp)
                        ) { Text("Take spelling quiz", color = buttonTxtColor) }
                        Button(
                            onClick = {
                                val mcIntent = Intent(context, MultipleChoice::class.java)
                                context.startActivity(mcIntent)
                            },
                            colors = ButtonDefaults.buttonColors(contentColor = Color.White),
                            modifier = Modifier
                                .padding(top = customPading.dp)
                                .align(Alignment.TopCenter)
                                .width(largeWidth.dp)
                                .height(smallHeight.dp)
                        ) {
                            Text("Take Multiple Choice", color = buttonTxtColor)
                        }
                        Button(
                            onClick = {
                                val matchIntent = Intent(context, MatchPictoLetter::class.java)
                                context.startActivity(matchIntent)
                            },
                            colors = ButtonDefaults.buttonColors(contentColor = Color.White),
                            modifier = Modifier
                                .padding(top = customTopPadding.dp)
                                .align(Alignment.TopCenter)
                                .width(largeWidth.dp)
                                .height(smallHeight.dp)
                        )
                        {
                            Text("Take Match Quiz", color = buttonTxtColor)
                        }
                    }
                }
            }
        }
    }

}

/**
 * A composable function to preview the settings screen content in an Android Studio preview.
 */
@Preview(showBackground = true)
@Composable
fun QuizScreenPreview() {
    SignLanguageAppTheme {

    }
}


