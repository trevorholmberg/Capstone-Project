package com.example.signlanguageapp


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.signlanguageapp.ui.theme.SignLanguageAppTheme
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.signlanguageapp.UserProfile.UserPreferenceManager.getSavedUserPreference
import com.example.signlanguageapp.UserProfile.UserPreferenceManager.saveUserPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * @author Matthew Agudelo
 * @author Matthew Talle
 * @author  Trevor Holmberg
 * @version 1.0
 * @date 2024-12-11
 * Main activity screen that is started when the app launches.
 */
class MainActivity : ComponentActivity() {
    private var keep : Boolean = true
    private val DELAY = 1250L//milliseconds

    private var theme = ThemeHelper.currTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().setKeepOnScreenCondition{keep}
        lifecycleScope.launch{
            delay(DELAY)
            keep = false
        }
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
                    MainMenu(
                        modifier = Modifier.padding(innerPadding),
                        theme = this.theme
                    )
                }
            }
        }
    }
}

@Composable
        /**
         * MainMenu composable function displays main menu UI for app
         *  Buttons Go for transition to Camera Screen
         *  Button for Settings to transition to setting screen.
         *  Button for about us to transition to the developers screen.
         * @param modifier A Modifier applied to the Box container.
         * It allows customization of the layout's size, padding, and other visual properties.
         * Default is Modifier.fillMaxSize(), making the Box occupy the entire available space.
         */
fun MainMenu(modifier: Modifier = Modifier, theme: Themes) {
    val largeWidth = 200
    val largeHeight = 50
    val padding1 = 250
    val padding2 = 400
    val padding3 = 325
    val padding4 = 480
    val padding5 = 560
    val smallestMod = 64
    val smallestPadding = 80
    val context = LocalContext.current
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


    Box ( //Use Box instead of Column
        modifier =  modifier
            .fillMaxSize()
            .background(bkgColor),
        contentAlignment = Alignment.Center,
    )
    {
        LaunchedEffect(Unit) {
            saveUserPreference(context, "Guest")
        }

        Image(
            painter = painterResource(id = R.drawable.asl),
            contentDescription = "",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(smallestPadding.dp)


        )
        Button(
            // start intent to CameraScreen
            onClick = {
                val cameraIntent = Intent(context, CameraScreen::class.java)
                context.startActivity(cameraIntent)

            },
            colors = ButtonDefaults.buttonColors(contentColor = Color.White),
            modifier = modifier
                .padding(top = padding1.dp)
                .align(Alignment.TopCenter)
                .width(largeWidth.dp)
                .height(largeHeight.dp)


        ) {
            Text(
                text = "Practice",
                style = MaterialTheme.typography.headlineMedium,
                color = buttonTxtColor)


        }
        Button(
            onClick = {
                val quizIntent = Intent(context, QuizScreen::class.java)
                context.startActivity(quizIntent)
            },
            colors = ButtonDefaults.buttonColors(contentColor = Color.White),
            modifier = modifier
                .padding(top = padding2.dp)
                .align(Alignment.TopCenter)
                .width(largeWidth.dp)
                .height(largeHeight.dp)
        ) {
            Text(
                text = "Take a quiz",
                style = MaterialTheme.typography.headlineMedium,
                color = buttonTxtColor)

        }
        Image(
            painter = painterResource(id = R.drawable.info),
            contentDescription = "",

            modifier = Modifier
                .size(smallestMod.dp)
                .align(Alignment.BottomEnd) // Start is Right Left is End
                .padding(smallestPadding.dp)
                .clickable {
                    // need context aka where we currently are its same as using this in java
                    val aboutusIntent = Intent(context,AboutUs::class.java)
                    context.startActivity(aboutusIntent)

                }
        )
        // start intent to setting screen
        Button( onClick = {
            val settingsIntent = Intent(context, SettingScreen::class.java)
            context.startActivity(settingsIntent)
        },
            colors = ButtonDefaults.buttonColors(contentColor = Color.White),
            modifier = modifier
                .padding(top = padding3.dp)
                .align(Alignment.TopCenter)
                .width(largeWidth.dp)
                .height(largeHeight.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = buttonTxtColor)
        }
        //Spacer(Modifier.requiredHeight(30.dp))//create space between buttons

        // start intent to stats screen
        Button( onClick = {
            val username = getSavedUserPreference(context)
            if (username.isNullOrEmpty() || username == "Guest") {
                Toast.makeText(context, "Please make a profile to view stats",
                    Toast.LENGTH_SHORT).show()
            } else {
                val statsIntent = Intent(context, StatScreen::class.java)
                context.startActivity(statsIntent)
            }
        },
            colors = ButtonDefaults.buttonColors(contentColor = Color.White),
            modifier = modifier
                .padding(top = padding4.dp)
                .align(Alignment.TopCenter)
                .width(largeWidth.dp)
                .height(largeHeight.dp)
        ) {
            Text(
                text = "Stats",
                style = MaterialTheme.typography.headlineMedium,
                color = buttonTxtColor)
        }
        Button( onClick = {
            val userProfileIntent = Intent(context, UserProfile::class.java)
            context.startActivity((userProfileIntent))
        },
            colors = ButtonDefaults.buttonColors(contentColor = Color.White),
            modifier = modifier
                .padding(top = padding5.dp)
                .align(Alignment.TopCenter)
                .width(largeWidth.dp)
                .height(largeHeight.dp)
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = buttonTxtColor)
        }


    }


}


