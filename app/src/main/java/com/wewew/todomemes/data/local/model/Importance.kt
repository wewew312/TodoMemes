package com.wewew.todomemes.data.local.model

enum class Importance(val ruName: String) {
    LOW("😴Неважно"),
    NORMAL("🙏Обычно"),
    HIGH("❗Сверхважно");

    companion object {
        fun fromRuName(value: String?): Importance = when (value) {
            LOW.ruName -> LOW
            HIGH.ruName -> HIGH
            else -> NORMAL
        }
    }
}