package ru.mirea.toir.common.mappers

interface Mapper<From, To> {
    fun map(item: From): To

    fun map(list: List<From>): List<To> = list.map { item -> map(item) }
}