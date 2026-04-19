import extensions.androidLibraryConfig

plugins { alias(libs.plugins.conventionPlugin.kmpFeatureSetup) }

androidLibraryConfig { namespace = "ru.mirea.toir.feature.photo.capture.api" }
