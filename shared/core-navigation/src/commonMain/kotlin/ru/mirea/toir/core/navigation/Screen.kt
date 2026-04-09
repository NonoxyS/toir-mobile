package ru.mirea.toir.core.navigation

import kotlinx.serialization.Serializable

interface Screen

@Serializable
data object DemoFirstRoute : Screen

@Serializable
data object DemoSecondRoute : Screen
