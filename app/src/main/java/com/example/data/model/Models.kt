package com.example.data.model

enum class MessageSender {
    USER, ASSISTANT
}

enum class WidgetType {
    NONE, WEATHER, TIMER, SMART_DEVICE, ROUTINE_BRIEFING
}

data class AssistantMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val widgetType: WidgetType = WidgetType.NONE,
    val actionData: String? = null,
    val isVoiceInput: Boolean = false
)

enum class DeviceType {
    LIGHT, THERMOSTAT, SPEAKER, PLUG
}

data class SmartDevice(
    val id: String,
    val name: String,
    val type: DeviceType,
    val room: String,
    val isOn: Boolean,
    val value: Int = 50, // Brightness (0-100), Temp (18-30 or 60-85), Volume (0-100)
    val statusText: String = ""
)

data class ReminderItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val timeLabel: String,
    val isCompleted: Boolean = false
)

data class RoutineItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val command: String,
    val actionsCount: Int,
    val steps: List<String>
)

data class WeatherInfo(
    val location: String = "الرياض (Riyadh)",
    val temperature: String = "٢٦° م",
    val condition: String = "مشمس ولطيف",
    val highLow: String = "العظمى: ٢٩° • الصغرى: ١٩°",
    val humidity: String = "٤٢٪",
    val wind: String = "١٢ كم/س",
    val airQuality: String = "ممتاز (٢٤)"
)
