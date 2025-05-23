package com.example.signlanguageapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.signlanguageapp.ui.theme.SignLanguageAppTheme

/**
 * Screen that shows information about the developers
 */
class AboutUs : ComponentActivity() {
    private var theme = ThemeHelper.currTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent()
    }
    override fun onResume() {
        super.onResume()
        this.theme = ThemeHelper.currTheme
        setContent()
    }

    private fun setContent(){
        setContent {
            SignLanguageAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AboutUsContent(
                        modifier = Modifier.padding(innerPadding),
                        theme = this.theme
                    )
                }
            }
        }
    }
}

//ALL IN
@Composable
        /**
         * Function that displays the image, text
         * the course this project is for
         * and the names of the developers.
         */
fun AboutUsContent(modifier: Modifier = Modifier, theme: Themes) {

    val bkgColor: Color;
    val txtColor: Color;

    when (theme) {
        Themes.LightTheme -> {bkgColor = Color.White;
            txtColor = Color.Black}
        Themes.DarkTheme -> {bkgColor= Color.DarkGray
            txtColor = Color.LightGray;}
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(bkgColor),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Image at the top
        Image(
            painter = painterResource(id = R.drawable.about_us),
            contentDescription = "App Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(500.dp)
                .padding(bottom = 16.dp)
        )

        // Heading
        Text(
            text = "About Us",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp),
            color = txtColor
        )
        Text(
            text = "Capstone",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding( bottom = 8.dp),
            color = txtColor)



        // Description text
        Text(
            text = "Authors: Matthew A, Matthew T, Trevor H.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Justify,
            color = txtColor
        )
    }
}

