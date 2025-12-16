package com.wewew.todomemes

enum class Importance(val ruName: String) {
    LOW("неважная"),
    NORMAL("обычная"),
    HIGH("важная");

    companion object {
        fun fromRuName(value: String?): Importance = when (value) {
            LOW.ruName -> LOW
            HIGH.ruName -> HIGH
            else -> NORMAL
        }
    }
}