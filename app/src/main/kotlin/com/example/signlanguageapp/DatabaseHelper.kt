/**
 * Helper class for interacting with the SQLite database.
 * @author Matthew Talle, Matthew Agudelo, Trevor Holmberg
 */

package com.example.signlanguageapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.security.MessageDigest

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context,
    DATABASE_NAME, null, DATABASE_VERSION) {

    /**
     * Companion object containing the static fields and methods
     * for the DatabaseHelper class.
     */
    companion object{

        private var databaseHelper: DatabaseHelper? = null // Singleton pattern
        private const val DATABASE_NAME: String = "quiz_data"
        private const val DATABASE_VERSION: Int = 2

        // Table names
        const val QUIZ_TABLE: String = "spelling_quiz"
        const val USER_TABLE: String = "USERS"

        // Field names for quiz table
        const val ID_FIELD: String = "id"
        const val QUESTION_FIELD: String = "question"
        const val ANSWER_FIELD: String = "answer"

        /**
         * Function implementation for the singleton pattern
         */
        fun instanceOfDatabase(context: Context) : DatabaseHelper {
            if (this.databaseHelper == null){
                this.databaseHelper = DatabaseHelper(context)
            }
            return this.databaseHelper!!
        }
    }

    /**
     * Called when the database is created for the first time and creates and initializes
     * the tables if they don't currently exist in the system.
     */
    override fun onCreate(db: SQLiteDatabase?) {

        val query: String = "CREATE TABLE IF NOT EXISTS " + QUIZ_TABLE + " (" + ID_FIELD +
                " INTEGER PRIMARY KEY, " + QUESTION_FIELD + " TEXT, " +
                ANSWER_FIELD + " TEXT);"
        val userTableQuery = "CREATE TABLE IF NOT EXISTS $USER_TABLE (" +
                "username TEXT UNIQUE, " +
                "password TEXT NOT NULL);"
        val userStatsTableQuery = "CREATE TABLE IF NOT EXISTS userStats (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT, " +
                "quizType TEXT, " +
                "correct INTEGER DEFAULT 0, " +
                "incorrect INTEGER DEFAULT 0, " +
                "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE);"

        // executes each query to create the tables
        db?.execSQL(userStatsTableQuery)
        db?.execSQL(query)
        db?.execSQL(userTableQuery)

    }

    /**
     * Drop all tables and create them again.
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $QUIZ_TABLE")
        db?.execSQL("DROP TABLE IF EXISTS $USER_TABLE")
        db?.execSQL("DROP TABLE IF EXISTS userStats")
        onCreate(db)
    }

    /**
     * Helper function to inset data into the quiz table.
     */
    private fun insertData(id: Int, question: String, answer: String){
        val values = ContentValues()

        values.put(ID_FIELD, id)
        values.put(QUESTION_FIELD, question)
        values.put(ANSWER_FIELD, answer)

        val db = this.writableDatabase

        // insert if they do not already exist
        db.insertWithOnConflict(QUIZ_TABLE,
            null, values, SQLiteDatabase.CONFLICT_IGNORE)

    }

    /**
     * Function to populate the quiz table with spelling prompts if
     * they don't already exist.
     */
    fun insert(){
        val spellPrompts = listOf(
            "Spell Cat" to "CAT",
            "Spell Dog" to "DOG",
            "Spell BIRD" to "BIRD",
            "Spell Cow" to "COW",
            "Spell Goat" to "GOAT",
            "Spell Dad" to "DAD",
            "Spell Mom" to "MOM",
            "Spell Door" to "DOOR",
            "Spell Eat" to "EAT",
            "Spell Nose" to "NOSE"
        )

        spellPrompts.forEachIndexed { index, (prompt, answer) ->
            this.insertData(index, prompt, answer)
        }
    }

    /**
     * Function to get all data from the quiz table.
     */
    fun getQuizData(): Cursor? {
        val db = this.readableDatabase;
        return db.rawQuery("SELECT * FROM $QUIZ_TABLE", null);
    }

    /**
     * Function to add a user to the database.
     */
    fun addUser(username: String, password: String): String? {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("username", username)
        values.put("password", hashPassword(password))
        val result = db.insert(USER_TABLE, null, values)
        return if (result == -1L) "Error adding user" else null

}
    fun deleteUser(username: String): Boolean {
        val db = writableDatabase
        db.delete(USER_TABLE, "username = ?", arrayOf(username))
        removeUserStats(username)
        return true
    }
    fun getAllUsers(): List<String> {
        val zero = 0
        val usersList = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT username FROM $USER_TABLE", null)

        if (cursor.moveToFirst()) {
            do {
                usersList.add(cursor.getString(zero))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return usersList
    }
    fun validateUser(username: String, password: String): Boolean {
        val countZero = 0
        val db = readableDatabase
        val query = "SELECT * FROM $USER_TABLE WHERE username = ? AND password = ?"
        val cursor = db.rawQuery(query, arrayOf(username, hashPassword(password)))

        val userExists = cursor.count > countZero
        cursor.close()
        return userExists
    }


    fun addUserStats(username: String, quizType: String) {
        val correctnum = 0
        val incorrectnum = 0
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("quizType", quizType)
            put("correct", correctnum)
            put("incorrect", incorrectnum)
        }
        db.insert("userStats", null, values)
    }
    fun updateStats(username: String, quizType: String, correct: Int, incorrect: Int) {
        val db = this.writableDatabase
        val query = "UPDATE userStats SET correct = correct + ?, " +
                "incorrect = incorrect + ? WHERE username = ? AND quizType = ?"
        db.execSQL(query, arrayOf(correct, incorrect, username, quizType))
    }
    fun getUserStats(username: String): List<List<Int>> {
        val db = this.readableDatabase
        val statsList = mutableListOf<List<Int>>()
        val cursor = db.rawQuery("SELECT quizType, correct, " +
                "incorrect FROM userStats WHERE username = ?", arrayOf(username))

        while (cursor.moveToNext()) {
            val correct = cursor.getInt(1)
            val incorrect = cursor.getInt(2)
            statsList.add(listOf(correct, incorrect))
        }
        cursor.close()
        return statsList
    }

    private fun removeUserStats(username: String){
        val db = this.writableDatabase
        db.delete("userStats", "username = ?", arrayOf(username))
    }

    fun resetStats(username: String) {
        val setCorrect = 0
        val setIncorrect = 0
        val db = this.writableDatabase
        val query = "UPDATE userStats SET correct = setCorrect, " +
                "incorrect = setIncorrect WHERE username = ?"
        db.execSQL(query, arrayOf(username))
    }

    fun getAllOverallStats(): List<Int>{
        val zero = 0
        val one = 1
        val users = getAllUsers()
        val overall = mutableListOf(zero,zero)
        for (user in users){
            val stats = getUserStats(user)
            overall[zero] = overall[zero] + stats[zero][zero]
            //Add user's correct to overall correct
            overall[one] = overall[one] + stats[zero][one]
            //Add user's attempts to overall attempts
        }
        return overall
    }
    fun userExists(username: String): Boolean {
        val one = 1
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT one FROM $USER_TABLE WHERE username = ?", arrayOf(username))
        val exists = cursor.moveToFirst()
        cursor.close()
        return exists
    }
    private fun hashPassword(password: String): String{
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString ("") {"%02x".format(it)}
    }






}