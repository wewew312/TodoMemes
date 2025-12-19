package com.wewew.todomemes.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// добавил priority параметр который NOT NULL
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE todos ADD COLUMN priority INTEGER NOT NULL DEFAULT 0"
        )
    }
}

// переименовал text -> title
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE todos_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                isDone INTEGER NOT NULL,
                priority INTEGER NOT NULL
            )
        """.trimIndent())

        db.execSQL("""
            INSERT INTO todos_new (id, title, isDone, priority)
            SELECT id, text, isDone, priority FROM todos
        """.trimIndent())

        db.execSQL("DROP TABLE todos")
        db.execSQL("ALTER TABLE todos_new RENAME TO todos")
    }
}
