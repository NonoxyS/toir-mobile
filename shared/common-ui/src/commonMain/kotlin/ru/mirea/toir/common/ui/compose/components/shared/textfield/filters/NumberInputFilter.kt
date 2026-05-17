package ru.mirea.toir.common.ui.compose.components.shared.textfield.filters

object NumberInputFilter : InputFilter {
    override fun apply(raw: String): String {
        val noWhitespace = raw.filterNot { it.isWhitespace() }
        val sb = StringBuilder()
        var sepSeen = false
        for ((index, c) in noWhitespace.withIndex()) {
            when {
                c == '-' && index == 0 -> sb.append(c)
                c.isDigit() -> sb.append(c)
                (c == '.' || c == ',') && !sepSeen -> {
                    sb.append(c)
                    sepSeen = true
                }
            }
        }
        return sb.toString()
    }
}
