package ru.mirea.toir.core.database.adapters

import app.cash.sqldelight.ColumnAdapter

class EnumColumnAdapter<E : Enum<E>>(
    private val decode: (String) -> E,
) : ColumnAdapter<E, String> {
    override fun decode(databaseValue: String): E = decode(databaseValue)
    override fun encode(value: E): String = value.name
}
