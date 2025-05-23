/**
 * This class handles persistent storage of user stats
 * so that progress is not lost when the app is closed.
 * It uses Android's DataStore to manage the data.
 *
 * @author Trevor Holmberg
 * @version Spring 2025
 */

package com.example.signlanguageapp

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Create a DataStore instance tied to the Context
private val Context.dataStore by preferencesDataStore(name = "user_stats")

/**
 * Manager class for interacting with the DataStore.
 * Supports saving, updating, and resetting user statistics
 * for different types of quizzes (Multiple Choice, Matching, Spelling).
 */
class DataStoreManager(private val context: Context) {

    companion object {
        // Keys for saving and retrieving integer values
        val TOTAL_CORRECT = intPreferencesKey("correct")
        val TOTAL_ATTEMPT = intPreferencesKey("total")
        val MC_CORRECT = intPreferencesKey("MCcorrect")
        val MC_ATTEMPT = intPreferencesKey("MCtotal")
        val MATCH_CORRECT = intPreferencesKey("MatchCorrect")
        val MATCH_ATTEMPT = intPreferencesKey("MatchTotal")
        val SPELL_CORRECT = intPreferencesKey("SpellCorrect")
        val SPELL_ATTEMPT = intPreferencesKey("SpellTotal")
    }

    // Flows for observing changes to each statistic
    val correctFlow: Flow<Int> = context.dataStore.data.map { it[TOTAL_CORRECT] ?: 0 }
    val attemptFlow: Flow<Int> = context.dataStore.data.map { it[TOTAL_ATTEMPT] ?: 0 }
    val mcCorrectFlow: Flow<Int> = context.dataStore.data.map { it[MC_CORRECT] ?: 0 }
    val mcAttemptFlow: Flow<Int> = context.dataStore.data.map { it[MC_ATTEMPT] ?: 0 }
    val matchCorrectFlow: Flow<Int> = context.dataStore.data.map { it[MATCH_CORRECT] ?: 0 }
    val matchAttemptFlow: Flow<Int> = context.dataStore.data.map { it[MATCH_ATTEMPT] ?: 0 }
    val spellCorrectFlow: Flow<Int> = context.dataStore.data.map { it[SPELL_CORRECT] ?: 0 }
    val spellAttemptFlow: Flow<Int> = context.dataStore.data.map { it[SPELL_ATTEMPT] ?: 0 }

    /**
     * Resets all stored statistics to zero.
     * Typically used when a user resets their progress.
     */
    suspend fun reset() {
        context.dataStore.edit { prefs ->
            prefs[TOTAL_CORRECT] = 0
            prefs[TOTAL_ATTEMPT] = 0
            prefs[MC_CORRECT] = 0
            prefs[MC_ATTEMPT] = 0
            prefs[MATCH_CORRECT] = 0
            prefs[MATCH_ATTEMPT] = 0
            prefs[SPELL_CORRECT] = 0
            prefs[SPELL_ATTEMPT] = 0
        }
    }

    /**
     * Updates user statistics after answering a Multiple Choice question.
     *
     * @param correct Total number of correct answers across all categories.
     * @param total Total number of attempted questions across all categories.
     * @param correctMC Number of correct answers specifically in Multiple Choice.
     * @param totalMC Total Multiple Choice questions attempted.
     */
    suspend fun updateStatsMC(correct: Int, total: Int, correctMC: Int, totalMC: Int) {
        context.dataStore.edit { prefs ->
            prefs[TOTAL_CORRECT] = correct
            prefs[TOTAL_ATTEMPT] = total
            prefs[MC_CORRECT] = correctMC
            prefs[MC_ATTEMPT] = totalMC
        }
    }

    /**
     * Updates user statistics after answering a Spelling question.
     *
     * @param correct Total number of correct answers across all categories.
     * @param total Total number of attempted questions across all categories.
     * @param correctSpell Number of correct answers specifically in Spelling.
     * @param totalSpell Total Spelling questions attempted.
     */
    suspend fun updateStatsSpell(correct: Int, total: Int, correctSpell: Int, totalSpell: Int) {
        context.dataStore.edit { prefs ->
            prefs[TOTAL_CORRECT] = correct
            prefs[TOTAL_ATTEMPT] = total
            prefs[SPELL_CORRECT] = correctSpell
            prefs[SPELL_ATTEMPT] = totalSpell
        }
    }

    /**
     * Updates user statistics after answering a Matching question.
     *
     * @param correct Total number of correct answers across all categories.
     * @param total Total number of attempted questions across all categories.
     * @param correctMatch Number of correct answers specifically in Matching.
     * @param totalMatch Total Matching questions attempted.
     */
    suspend fun updateStatsMatch(correct: Int, total: Int, correctMatch: Int, totalMatch: Int) {
        context.dataStore.edit { prefs ->
            prefs[TOTAL_CORRECT] = correct
            prefs[TOTAL_ATTEMPT] = total
            prefs[MATCH_CORRECT] = correctMatch
            prefs[MATCH_ATTEMPT] = totalMatch
        }
    }
}
