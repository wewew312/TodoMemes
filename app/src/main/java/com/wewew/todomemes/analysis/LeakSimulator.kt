package com.wewew.todomemes.analysis

import android.app.Activity
import android.os.Handler
import android.os.Looper
import org.slf4j.LoggerFactory

/**
 * УСЛОЖНЕНИЕ 2: Симуляция РЕАЛЬНЫХ утечек памяти для демонстрации LeakCanary
 *
 * ВНИМАНИЕ: Этот код содержит УМЫШЛЕННЫЕ утечки памяти для учебных целей!
 * НЕ используйте подобный код в продакшене!
 */
object LeakSimulator {
    private val logger = LoggerFactory.getLogger("LeakSimulator")

    // УТЕЧКА 1: Статическая ссылка на Activity
    // LeakCanary обнаружит это как "Activity leaked"
    private var leakedActivity: Activity? = null

    // УТЕЧКА 2: Список, который накапливает данные без очистки
    private val leakedDataList = mutableListOf<ByteArray>()

    // УТЕЧКА 3: Handler с долгоживущим Runnable
    private val handler = Handler(Looper.getMainLooper())
    private var leakyRunnable: Runnable? = null

    /**
     * Создает утечку Activity - классическая ошибка Android разработчиков.
     * Activity не сможет быть собрана GC пока leakedActivity != null
     */
    fun leakActivity(activity: Activity) {
        leakedActivity = activity
        logger.warn("LEAK CREATED: Activity stored in static field! Activity: ${activity.localClassName}")
    }

    /**
     * Создает утечку памяти через накопление данных.
     * Каждый вызов добавляет 1MB в память без возможности очистки.
     */
    fun leakMemory() {
        // Создаем массив на 1MB
        val chunk = ByteArray(1024 * 1024) // 1 MB
        leakedDataList.add(chunk)
        logger.warn("LEAK CREATED: Added 1MB to leaked list. Total leaked: ${leakedDataList.size}MB")
    }

    /**
     * Создает утечку через Handler - задача выполнится через 5 минут,
     * всё это время удерживая ссылку на Activity через Runnable.
     */
    fun leakWithHandler(activity: Activity) {
        leakyRunnable = Runnable {
            // Этот Runnable захватывает activity в замыкании
            logger.info("Handler executed for activity: ${activity.localClassName}")
        }
        // Задержка 5 минут - всё это время Activity не может быть собрана GC
        handler.postDelayed(leakyRunnable!!, 5 * 60 * 1000L)
        logger.warn("LEAK CREATED: Handler scheduled with 5 min delay, holding Activity reference")
    }

    /**
     * Очищает все утечки (для демонстрации правильного решения)
     */
    fun cleanup() {
        leakedActivity = null
        leakedDataList.clear()
        leakyRunnable?.let { handler.removeCallbacks(it) }
        leakyRunnable = null
        logger.info("CLEANUP: All leaks cleared!")
    }

    /**
     * Возвращает информацию о текущих утечках
     */
    fun getLeakInfo(): String {
        return buildString {
            appendLine("=== Leak Status ===")
            appendLine("Leaked Activity: ${leakedActivity?.localClassName ?: "none"}")
            appendLine("Leaked Memory: ${leakedDataList.size}MB")
            appendLine("Pending Handler: ${leakyRunnable != null}")
        }
    }
}