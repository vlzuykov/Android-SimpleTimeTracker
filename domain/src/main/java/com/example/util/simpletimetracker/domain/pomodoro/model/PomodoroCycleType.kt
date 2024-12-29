package com.example.util.simpletimetracker.domain.pomodoro.model

sealed interface PomodoroCycleType {
    object Focus : PomodoroCycleType
    object Break : PomodoroCycleType
    object LongBreak : PomodoroCycleType
}