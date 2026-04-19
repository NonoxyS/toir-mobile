package ru.mirea.toir.core.database.adapters

import app.cash.sqldelight.ColumnAdapter
import ru.mirea.toir.core.database.models.LocalEnum
import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries

internal class EnumColumnAdapter<E>(
    private val enumEntries: EnumEntries<E>
) : ColumnAdapter<E, String> where E : Enum<E>,
                                   E : LocalEnum {

    override fun decode(databaseValue: String): E {
        return enumEntries
            .firstOrNull { entry -> entry.localValue == databaseValue }
            ?: throw IllegalArgumentException(
                "DatabaseValue $databaseValue not found in ${enumEntries::class.simpleName}"
            )
    }

    override fun encode(value: E): String = value.localValue

    companion object Factory {
        inline fun <reified E> create(): EnumColumnAdapter<E>
            where E : Enum<E>,
                  E : LocalEnum = EnumColumnAdapter(enumEntries<E>())
    }
}
