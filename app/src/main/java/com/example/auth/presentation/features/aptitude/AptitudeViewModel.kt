package com.example.auth.presentation.features.aptitude

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

// ─── Data Models ──────────────────────────────────────────────────────────────

enum class GameState { INTRO, PLAYING, GAME_OVER }

data class Question(
    val nodeA: String, // e.g., "5"
    val nodeB: String, // e.g., "3"
    val nodeC: String, // e.g., "?" (the target)
    val operator: String, // "+", "-", "×", "÷"
    val options: List<Int>,
    val correctOptionIndex: Int
)

data class AptitudeState(
    val gameState: GameState = GameState.INTRO,
    val score: Int = 0,
    val secondsRemaining: Int = 60,
    val currentQuestion: Question? = null,
    val combo: Int = 0,
    val errorFlash: Boolean = false,
    val correctFlash: Boolean = false
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class AptitudeViewModel : ViewModel() {

    private val _state = MutableStateFlow(AptitudeState())
    val state = _state.asStateFlow()

    private var timerJob: Job? = null

    fun startGame() {
        _state.update { 
            AptitudeState(
                gameState = GameState.PLAYING,
                score = 0,
                secondsRemaining = 60
            ) 
        }
        generateNextQuestion()
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_state.value.secondsRemaining > 0) {
                delay(1000)
                _state.update { it.copy(secondsRemaining = it.secondsRemaining - 1) }
            }
            // Time up!
            _state.update { it.copy(gameState = GameState.GAME_OVER) }
        }
    }

    private fun generateNextQuestion() {
        // As player scores more, numbers get bigger and operations change
        val difficulty = _state.value.score / 50 
        
        val ops = mutableListOf("+", "-")
        if (difficulty > 1) ops.add("×")
        if (difficulty > 3) ops.add("÷")

        val op = ops.random()
        var a = 0
        var b = 0
        var c = 0

        when (op) {
            "+" -> {
                a = Random.nextInt(1, 10 + difficulty * 10)
                b = Random.nextInt(1, 10 + difficulty * 10)
                c = a + b
            }
            "-" -> {
                a = Random.nextInt(5, 20 + difficulty * 10)
                b = Random.nextInt(1, a)
                c = a - b
            }
            "×" -> {
                a = Random.nextInt(2, 6 + difficulty * 2)
                b = Random.nextInt(2, 6 + difficulty * 2)
                c = a * b
            }
            "÷" -> {
                b = Random.nextInt(2, 6 + difficulty * 2)
                c = Random.nextInt(2, 6 + difficulty * 2)
                a = b * c
            }
        }

        // We can hide A, B, or C. For now, let's always hide C to keep it simple and fast-paced
        val correctAnswer = c
        val options = generateOptions(correctAnswer, op)

        _state.update {
            it.copy(
                currentQuestion = Question(
                    nodeA = a.toString(),
                    nodeB = b.toString(),
                    nodeC = "?",
                    operator = op,
                    options = options,
                    correctOptionIndex = options.indexOf(correctAnswer)
                ),
                errorFlash = false,
                correctFlash = false
            )
        }
    }

    private fun generateOptions(correct: Int, op: String): List<Int> {
        val opts = mutableSetOf(correct)
        while(opts.size < 4) {
            // Add plausible wrong answers
            val variance = Random.nextInt(1, 5)
            val fake = if (Random.nextBoolean()) correct + variance else correct - variance
            if (fake > 0) opts.add(fake)
        }
        return opts.toList().shuffled()
    }

    fun selectOption(index: Int) {
        val q = _state.value.currentQuestion ?: return
        if (_state.value.gameState != GameState.PLAYING) return

        if (index == q.correctOptionIndex) {
            // Correct
            val points = 10 + (_state.value.combo * 2)
            _state.update { 
                it.copy(
                    score = it.score + points,
                    combo = it.combo + 1,
                    correctFlash = true
                )
            }
            generateNextQuestion()
        } else {
            // Wrong
            _state.update { 
                it.copy(
                    score = (it.score - 5).coerceAtLeast(0),
                    combo = 0,
                    errorFlash = true
                ) 
            }
            // Automatically clear error flash after a moment so they can try again
            viewModelScope.launch {
                delay(300)
                _state.update { it.copy(errorFlash = false) }
            }
        }
    }

    fun quitGame() {
        timerJob?.cancel()
        _state.update { AptitudeState() } // Reset
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
