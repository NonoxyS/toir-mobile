package dev.nonoxy.kmmtemplate.common.resources

import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.desc.ResourceFormattedStringDesc

interface StringConverter {

    fun convert(stringResource: StringResource): String
    fun convert(resourceFormattedString: ResourceFormattedStringDesc): String
}