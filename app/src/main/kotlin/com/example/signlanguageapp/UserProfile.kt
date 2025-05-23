package com.example.signlanguageapp
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.signlanguageapp.ui.theme.SignLanguageAppTheme

class UserProfile : ComponentActivity() {
    private lateinit var databaseHelper: DatabaseHelper
    private var theme = ThemeHelper.currTheme

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Initialize database
        databaseHelper = DatabaseHelper(this)
        setContent()
    }

    override fun onResume() {
        super.onResume()
        this.theme = ThemeHelper.currTheme
        setContent()
    }

    private fun setContent(){
        val lastUser =UserPreferenceManager.getSavedUserPreference(this) ?: "Guest"
        setContent {
            SignLanguageAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UserMenu(
                        modifier = Modifier.padding(innerPadding),
                        databaseHelper = databaseHelper,
                        currentUsername = lastUser,
                        onUserSelected = { selectedUser ->
                            UserPreferenceManager.saveUserPreference(this, selectedUser)

                        },
                        onLogout = {
                            UserPreferenceManager.saveUserPreference(this, "Guest")
                            finish()
                        },
                        theme = this.theme
                    )
                }
            }
        }
    }
    /**
    * Composable function for managing user accounts in the application.
    *
    * This menu allows users to:
    * - Create a new profile with username and password
    * - Sign in to an existing account
    * - Sign out or delete a profile
    *
    * User information is stored and validated using the provided DatabaseHelper.
    *
    * @param modifier Modifier for customizing layout and styling.
    * @param databaseHelper The helper class for database operations like validation.
    * @param currentUsername The current logged-in user's username.
    * @param onUserSelected Callback when a user successfully signs in or creates an account.
    * @param onLogout Callback when a user deletes their profile or signs out.
    * @param theme Current UI theme to adjust background and text colors.
    */
    @Composable
    fun UserMenu(
        modifier: Modifier = Modifier,
        databaseHelper: DatabaseHelper,
        currentUsername: String,
        onUserSelected: (String) -> Unit,
        onLogout: () -> Unit,
        theme: Themes
    ) {


        var username by remember { mutableStateOf(currentUsername) }
        var newUsername by remember { mutableStateOf("") }
        val context = LocalContext.current
        val usersList = remember { mutableStateListOf<String>() }
        var expanded by remember { mutableStateOf(false) }
        var selectedUser by remember { mutableStateOf("") }
        var saveList:  List<String> = emptyList()
        var password by remember { mutableStateOf("") }
        val height = 50.dp
        val width = 200.dp
        val size = 24.sp
        val space = 10.dp
        LaunchedEffect(Unit) {
            username = UserPreferenceManager.getSavedUserPreference(context) ?: "Guest"
        }

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

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(bkgColor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Welcome $username", fontSize = size, fontWeight = FontWeight.Bold,
                color = txtColor)

            Spacer(modifier = Modifier.height(20.dp))

            //Text box to change user name
            OutlinedTextField(
                value = newUsername,
                onValueChange = { newUsername = it },
                label = { Text("New Username") }
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation() // hide input
            )
            // button adding new users, check for dupes also
            Button(
                onClick = {
                    if (newUsername.isNotEmpty() && password.isNotEmpty()) {
                        if (databaseHelper.userExists(newUsername)) {
                            Toast.makeText(context,
                                "User already exists! Please choose a different username.",
                                Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        else {
                            username = newUsername
                            val result = databaseHelper.addUser(newUsername,password)
                            usersList.add(newUsername)
                            saveList = databaseHelper.getAllUsers()
                            if (result == null) { //meaning user was added successfully
                                databaseHelper.addUserStats(newUsername, "Total")
                                databaseHelper.addUserStats(newUsername, "Multiple Choice")
                                databaseHelper.addUserStats(newUsername, "Matching")
                                databaseHelper.addUserStats(newUsername, "Spelling")
                                UserPreferenceManager.saveUserPreference(context, newUsername)
                                Toast.makeText(context, "Username created!",
                                    Toast.LENGTH_SHORT).show()
                            }else
                                Toast.makeText(context, "Error creating user",
                                    Toast.LENGTH_SHORT).show()

                            newUsername = ""
                            password = ""


                        }
                    }
                },
                modifier = modifier
                    .width(width)
                    .height(height)
            ) {
                Text("Create User", color = buttonTxtColor)
            }

            Spacer(modifier = Modifier.height(space))

            // Delete user account and go back to main activity
            Button(
                onClick = {
                    val success = databaseHelper.deleteUser(username)
                    if (success) {
                        Toast.makeText(context, "User deleted!", Toast.LENGTH_SHORT).show()
                        onLogout()
                    } else {
                        Toast.makeText(context, "Delete failed!", Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(Color.Red),
                modifier = modifier
                    .width(200.dp)
                    .height(50.dp)

            ) {
                Text("Delete Profile")
            }
            Box {
                Button(onClick = {
                    if (newUsername.isNotEmpty() && password.isNotEmpty()) {
                        val isValidUser = databaseHelper.validateUser(newUsername, password)
                        if (isValidUser) {
                            username = newUsername
                            UserPreferenceManager.saveUserPreference(context,username)
                            Toast.makeText(context,
                                "Signed in as $username", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast.makeText(
                                context,
                                "Error: Username or Password is incorrect",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                        else{
                            Toast.makeText(context,
                                "Please fill in both fields",Toast.LENGTH_SHORT).show()
                        }

                },
                    modifier = modifier
                        .width(width)
                        .height(height)) {
                    Text("Sign In", color = buttonTxtColor)
                }

                }
            Button(onClick = {
                signOutUser(context)
                username = "Guest"
                Toast.makeText(context, "Signed out", Toast.LENGTH_SHORT).show()
            },
                modifier = modifier
                    .width(width)
                    .height(height))
            {
                Text("Sign Out", color = buttonTxtColor)
            }
        }


    }
    /**
     * Signs out the current user by resetting the saved user preference to "Guest".
     * @param context The context used to access shared preferences.
     */
    fun signOutUser(context: Context) {
        UserPreferenceManager.saveUserPreference(context, "Guest")
    }

    /**
     * Singleton object that manages saving and retrieving the last logged-in user
     * from SharedPreferences.
     */
    object UserPreferenceManager {
        /**
         * Gets the saved username of the last logged-in user.
         *
         * @param context The context from which to access SharedPreferences.
         * @return The saved username if present, or `null` if no user was saved.
         */
        fun getSavedUserPreference(context: Context): String? {
            val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            return sharedPref.getString("lastUser", null)
        }
        /**
         * Saves the username of the current user to SharedPreferences.
         *
         * @param context The context from which to access SharedPreferences.
         * @param username The username to be saved as the last logged-in user.
         */
        fun saveUserPreference(context: Context, username: String) {
            val sharedPref = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString("lastUser", username)
                apply()
            }
        }
    }


}
