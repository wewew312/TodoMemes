package com.wewew.todomemes.analysis

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.slf4j.LoggerFactory

/**
 * УСЛОЖНЕНИЕ 2: Симуляция проблем утечек памяти и производительности
 *
 * Этот файл демонстрирует типичные проблемы и их решения.
 * Код разделен на секции "ПРОБЛЕМА" и "РЕШЕНИЕ".
 */

// ============================================================================
// ПРОБЛЕМА 1: Утечка памяти через статическую ссылку на Context
// ============================================================================

/**
 * ПЛОХО: Хранение Context в companion object (синглтоне)
 * Это классическая утечка памяти - Activity не может быть собрана GC
 */
object LeakyContextHolder {
    private val logger = LoggerFactory.getLogger("LeakyContextHolder")

    // УТЕЧКА! Context (Activity) хранится статически
    private var leakedContext: Context? = null

    fun setContext(context: Context) {
        leakedContext = context
        logger.warn("MEMORY LEAK: Context stored in static field!")
    }

    fun doSomething() {
        // Использование контекста...
        leakedContext?.let {
            logger.debug("Using leaked context: ${it.packageName}")
        }
    }
}

/**
 * ХОРОШО: Использование ApplicationContext или WeakReference
 */
object SafeContextHolder {
    private val logger = LoggerFactory.getLogger("SafeContextHolder")

    // Безопасно: applicationContext живет столько же, сколько приложение
    private var appContext: Context? = null

    fun init(context: Context) {
        appContext = context.applicationContext
        logger.info("Safe: Using applicationContext")
    }

    fun doSomething() {
        appContext?.let {
            logger.debug("Using safe context: ${it.packageName}")
        }
    }
}

// ============================================================================
// ПРОБЛЕМА 2: Утечка памяти через анонимный inner class с Handler
// ============================================================================

/**
 * ПЛОХО: Анонимный Runnable держит ссылку на внешний класс
 */
class LeakyHandlerExample(private val context: Context) {
    private val logger = LoggerFactory.getLogger("LeakyHandlerExample")
    private val handler = Handler(Looper.getMainLooper())

    fun startLeakyTask() {
        // УТЕЧКА! Этот Runnable держит ссылку на LeakyHandlerExample,
        // который держит ссылку на Context
        handler.postDelayed({
            logger.warn("MEMORY LEAK: Anonymous runnable holding reference!")
            // Если Activity уничтожена, но Handler еще не выполнился,
            // Activity не будет собрана GC
        }, 60_000) // 60 секунд задержки
    }
}

/**
 * ХОРОШО: Явное удаление callbacks при уничтожении
 */
class SafeHandlerExample(context: Context) {
    private val logger = LoggerFactory.getLogger("SafeHandlerExample")
    private val handler = Handler(Looper.getMainLooper())
    private val appContext = context.applicationContext

    private val safeRunnable = Runnable {
        logger.info("Safe: Runnable executed")
    }

    fun startSafeTask() {
        handler.postDelayed(safeRunnable, 5_000)
    }

    fun cleanup() {
        // ВАЖНО: Удаляем все callbacks при уничтожении Activity
        handler.removeCallbacks(safeRunnable)
        handler.removeCallbacksAndMessages(null)
        logger.info("Safe: All callbacks removed")
    }
}

// ============================================================================
// ПРОБЛЕМА 3: Проблема производительности - блокирующие операции в UI потоке
// ============================================================================

/**
 * ПЛОХО: Синхронное чтение файла в UI потоке
 */
class BlockingFileReader(private val context: Context) {
    private val logger = LoggerFactory.getLogger("BlockingFileReader")

    fun readFileBlocking(): String {
        logger.warn("PERFORMANCE ISSUE: Reading file on UI thread!")
        // Это блокирует UI поток и вызывает ANR при больших файлах
        return context.filesDir.resolve("data.json").readText()
    }
}

/**
 * ХОРОШО: Асинхронное чтение через корутины
 */
class AsyncFileReader(private val context: Context) {
    private val logger = LoggerFactory.getLogger("AsyncFileReader")

    // Используем suspend функцию с Dispatchers.IO
    suspend fun readFileAsync(): String = kotlinx.coroutines.withContext(
        kotlinx.coroutines.Dispatchers.IO
    ) {
        logger.info("Good: Reading file on IO thread")
        context.filesDir.resolve("data.json").readText()
    }
}

// ============================================================================
// ПРОБЛЕМА 4: Избыточное создание объектов в цикле
// ============================================================================

/**
 * ПЛОХО: Создание StringBuilder в каждой итерации
 */
object InefficientStringBuilder {
    private val logger = LoggerFactory.getLogger("InefficientStringBuilder")

    fun buildStringBadly(items: List<String>): String {
        logger.warn("PERFORMANCE ISSUE: Creating objects in loop!")
        var result = ""
        for (item in items) {
            // Каждая конкатенация создает новый String объект
            result += item + ", "
        }
        return result
    }
}

/**
 * ХОРОШО: Использование StringBuilder
 */
object EfficientStringBuilder {
    private val logger = LoggerFactory.getLogger("EfficientStringBuilder")

    fun buildStringEfficiently(items: List<String>): String {
        logger.info("Good: Using StringBuilder")
        return buildString {
            items.forEachIndexed { index, item ->
                append(item)
                if (index < items.size - 1) append(", ")
            }
        }
    }
}

// ============================================================================
// КАК ДИАГНОСТИРОВАТЬ ЭТИ ПРОБЛЕМЫ
// ============================================================================

/**
 * Инструменты для диагностики:
 *
 * 1. Android Studio Profiler (Memory Profiler)
 *    - Показывает аллокации в реальном времени
 *    - Heap dump для анализа объектов в памяти
 *    - Отслеживание утечек
 *
 * 2. LeakCanary (добавить в build.gradle.kts):
 *    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
 *    - Автоматически обнаруживает утечки Activity, Fragment, View
 *    - Показывает цепочку ссылок, удерживающих объект
 *
 * 3. StrictMode для обнаружения блокирующих операций:
 *    StrictMode.setThreadPolicy(
 *        StrictMode.ThreadPolicy.Builder()
 *            .detectDiskReads()
 *            .detectDiskWrites()
 *            .penaltyLog()
 *            .build()
 *    )
 *
 * 4. Logcat с фильтром "StrictMode" для просмотра нарушений
 *
 * 5. CPU Profiler для анализа времени выполнения методов
 *    - Flame Chart показывает, где тратится время
 *    - Trace System для детального анализа
 */
object DiagnosticsGuide {
    const val GUIDE_VERSION = "1.0"
}