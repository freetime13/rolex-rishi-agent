package com.rishi.aiagent

sealed class ParsedCommand {
    data class Call(val contactQuery: String) : ParsedCommand()
    data class Torch(val enable: Boolean) : ParsedCommand()
    data class Brightness(val action: BrightnessAction) : ParsedCommand()
    data class Volume(val action: VolumeAction) : ParsedCommand()
    object None : ParsedCommand()
}

sealed class BrightnessAction {
    object Increase : BrightnessAction()
    object Decrease : BrightnessAction()
    object Max : BrightnessAction()
    object Min : BrightnessAction()
    data class Percent(val value: Int) : BrightnessAction()
}

sealed class VolumeAction {
    object Increase : VolumeAction()
    object Decrease : VolumeAction()
    data class Percent(val value: Int) : VolumeAction()
}

class CommandParser {

    fun parse(rawInput: String): ParsedCommand {
        val text = rawInput.lowercase().trim()

        // CALL INTENTS
        if (text.contains("call") || text.contains("phone lagao") || text.contains("ko call karo")) {
            val contact = extractContactName(text)
            if (contact.isNotEmpty()) {
                return ParsedCommand.Call(contact)
            }
        }

        // TORCH INTENTS
        if (text.contains("torch") || text.contains("flashlight")) {
            return if (text.contains("off") || text.contains("band") || text.contains("close")) {
                ParsedCommand.Torch(false)
            } else {
                ParsedCommand.Torch(true)
            }
        }

        // BRIGHTNESS INTENTS
        if (text.contains("brightness") || text.contains("roshni")) {
            val percentMatch = Regex("(\\d+)\\s*(percent|%)").find(text)
            if (percentMatch != null) {
                val value = percentMatch.groupValues[1].toIntOrNull() ?: 50
                return ParsedCommand.Brightness(BrightnessAction.Percent(value))
            }
            return when {
                text.contains("badha") || text.contains("increase") || text.contains("up") ->
                    ParsedCommand.Brightness(BrightnessAction.Increase)
                text.contains("kam") || text.contains("decrease") || text.contains("down") ->
                    ParsedCommand.Brightness(BrightnessAction.Decrease)
                text.contains("full") || text.contains("maximum") || text.contains("max") ->
                    ParsedCommand.Brightness(BrightnessAction.Max)
                text.contains("minimum") || text.contains("min") || text.contains("zero") ->
                    ParsedCommand.Brightness(BrightnessAction.Min)
                else -> ParsedCommand.Brightness(BrightnessAction.Increase)
            }
        }

        // VOLUME INTENTS
        if (text.contains("volume") || text.contains("awaz")) {
            val percentMatch = Regex("(\\d+)\\s*(percent|%)").find(text)
            if (percentMatch != null) {
                val value = percentMatch.groupValues[1].toIntOrNull() ?: 50
                return ParsedCommand.Volume(VolumeAction.Percent(value))
            }
            return when {
                text.contains("badha") || text.contains("increase") || text.contains("up") ->
                    ParsedCommand.Volume(VolumeAction.Increase)
                text.contains("kam") || text.contains("decrease") || text.contains("down") ->
                    ParsedCommand.Volume(VolumeAction.Decrease)
                else -> ParsedCommand.Volume(VolumeAction.Increase)
            }
        }

        return ParsedCommand.None
    }

    private fun extractContactName(input: String): String {
        return input
            .replace("rolex", "")
            .replace("ko call karo", "")
            .replace("ko phone lagao", "")
            .replace("call karo", "")
            .replace("call", "")
            .replace("phone lagao", "")
            .trim()
    }
}
