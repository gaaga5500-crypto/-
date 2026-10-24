package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.gemini.GeminiRepository
import com.example.data.model.AssistantMessage
import com.example.data.model.DeviceType
import com.example.data.model.MessageSender
import com.example.data.model.ReminderItem
import com.example.data.model.RoutineItem
import com.example.data.model.SmartDevice
import com.example.data.model.WeatherInfo
import com.example.data.model.WidgetType
import com.example.data.voice.SpeechManager
import com.example.data.voice.TtsManager
import com.example.ui.components.AssistantVisualState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val geminiRepository = GeminiRepository()

    private val _currentLanguage = MutableStateFlow("ar") // "ar" or "en"
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _messages = MutableStateFlow<List<AssistantMessage>>(
        listOf(
            AssistantMessage(
                sender = MessageSender.ASSISTANT,
                text = "مرحباً! كيف يمكنني مساعدتك اليوم؟ يمكنك التحدث عبر الميكروفون، أو كتابة أي سؤال، أو تشغيل أجهزة المنزل والروتينات اليومية!",
                widgetType = WidgetType.NONE
            )
        )
    )
    val messages: StateFlow<List<AssistantMessage>> = _messages.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val _statusNotice = MutableStateFlow<String?>(null)
    val statusNotice: StateFlow<String?> = _statusNotice.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    // Smart Home Devices with Arabic titles and rooms
    private val _smartDevices = MutableStateFlow<List<SmartDevice>>(
        listOf(
            SmartDevice(
                id = "light_living_room",
                name = "إضاءة غرفة المعيشة",
                type = DeviceType.LIGHT,
                room = "غرفة المعيشة",
                isOn = true,
                value = 85
            ),
            SmartDevice(
                id = "thermostat_main",
                name = "مكيف المنزل Nest",
                type = DeviceType.THERMOSTAT,
                room = "الممر الرئيسي",
                isOn = true,
                value = 24
            ),
            SmartDevice(
                id = "speaker_office",
                name = "مكبر الصوت الذكي",
                type = DeviceType.SPEAKER,
                room = "المكتب",
                isOn = false,
                value = 50
            ),
            SmartDevice(
                id = "plug_kitchen",
                name = "صانعة القهوة الذكية",
                type = DeviceType.PLUG,
                room = "المطبخ",
                isOn = false,
                value = 0
            )
        )
    )
    val smartDevices: StateFlow<List<SmartDevice>> = _smartDevices.asStateFlow()

    // Interactive Timer State
    private val _timerTotalSeconds = MutableStateFlow(300)
    val timerTotalSeconds: StateFlow<Int> = _timerTotalSeconds.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(300)
    val timerRemainingSeconds: StateFlow<Int> = _timerRemainingSeconds.asStateFlow()

    private val _timerIsRunning = MutableStateFlow(false)
    val timerIsRunning: StateFlow<Boolean> = _timerIsRunning.asStateFlow()

    private val _timerActiveInWidget = MutableStateFlow(false)
    val timerActiveInWidget: StateFlow<Boolean> = _timerActiveInWidget.asStateFlow()

    private var timerJob: Job? = null

    // Reminders in Arabic
    private val _reminders = MutableStateFlow<List<ReminderItem>>(
        listOf(
            ReminderItem(title = "الاجتماع الصباحي لمراجعة مهام الفريق", timeLabel = "10:00 ص", isCompleted = false),
            ReminderItem(title = "شراء الحاجيات: حليب وتفاح وقهوة مختصة", timeLabel = "2:30 م", isCompleted = false),
            ReminderItem(title = "قراءة ملخص أحدث تقنيات الذكاء الاصطناعي", timeLabel = "6:00 م", isCompleted = true)
        )
    )
    val reminders: StateFlow<List<ReminderItem>> = _reminders.asStateFlow()

    // Google Assistant Routines in Arabic
    private val _routines = MutableStateFlow<List<RoutineItem>>(
        listOf(
            RoutineItem(
                id = "routine_morning",
                title = "صباح الخير",
                subtitle = "الطقس، جدول اليوم، وتشغيل الإضاءة والقهوة",
                command = "صباح الخير",
                actionsCount = 3,
                steps = listOf(
                    "تشغيل إضاءة غرفة المعيشة بنسبة 80%",
                    "تشغيل صانعة القهوة في المطبخ",
                    "قراءة صوتية لحالة الطقس وجدول المهام"
                )
            ),
            RoutineItem(
                id = "routine_bedtime",
                title = "وقت النوم",
                subtitle = "تعتيم الإضاءة، ضبط التكييف ووضع الهدوء",
                command = "روتين وقت النوم",
                actionsCount = 4,
                steps = listOf(
                    "تخفيض الإضاءة إلى 15%",
                    "ضبط التكييف على درجة حرارة مريحة 24°",
                    "إطفاء صانعة القهوة",
                    "تشغيل أصوات الاسترخاء الهادئة"
                )
            ),
            RoutineItem(
                id = "routine_work",
                title = "التركيز والعمل",
                subtitle = "إضاءة ساطعة، مؤقت 25 دقيقة وموسيقى هادئة",
                command = "بدء روتين التركيز والعمل",
                actionsCount = 3,
                steps = listOf(
                    "ضبط الإضاءة على الأبيض الناصع 100%",
                    "بدء مؤقت تركيز لمدة 25 دقيقة",
                    "تشغيل قائمة صوتيات الدراسة على مكبر الصوت"
                )
            ),
            RoutineItem(
                id = "routine_leaving",
                title = "مغادرة المنزل",
                subtitle = "إطفاء كافة الأجهزة وضبط التكييف الاقتصادي",
                command = "روتين مغادرة المنزل",
                actionsCount = 3,
                steps = listOf(
                    "إطفاء كافة المصابيح والمقابس",
                    "ضبط التكييف على الوضع الاقتصادي",
                    "التأكد من أمان الأجهزة المنزلية"
                )
            )
        )
    )
    val routines: StateFlow<List<RoutineItem>> = _routines.asStateFlow()

    val weatherInfo = WeatherInfo()

    // Voice Managers configured with Arabic speech recognition
    private val speechManager = SpeechManager(
        context = application,
        onPartialResult = { partial ->
            _partialTranscript.value = partial
        },
        onFinalResult = { text ->
            _partialTranscript.value = ""
            sendMessage(text, isVoice = true)
        },
        onListeningStateChanged = { listening ->
            _isListening.value = listening
        },
        onErrorOccurred = { error ->
            _statusNotice.value = error
            viewModelScope.launch {
                delay(4000)
                _statusNotice.value = null
            }
        }
    )

    private val ttsManager = TtsManager(
        context = application,
        onSpeakingStateChanged = { speaking ->
            _isSpeaking.value = speaking
        }
    )

    fun toggleLanguage() {
        val next = if (_currentLanguage.value == "ar") "en" else "ar"
        _currentLanguage.value = next
        speechManager.currentLanguageTag = if (next == "ar") "ar-SA" else "en-US"
        val feedback = if (next == "ar") "تم تفعيل اللغة العربية لمساعد Google" else "Switched to English mode"
        ttsManager.speak(feedback)
    }

    fun getVisualState(): AssistantVisualState {
        return when {
            _isListening.value -> AssistantVisualState.LISTENING
            _isThinking.value -> AssistantVisualState.THINKING
            _isSpeaking.value -> AssistantVisualState.SPEAKING
            else -> AssistantVisualState.IDLE
        }
    }

    fun startListening() {
        ttsManager.stop()
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun toggleMute() {
        val next = !_isMuted.value
        _isMuted.value = next
        ttsManager.isMuted = next
        if (next) {
            ttsManager.stop()
        }
    }

    fun speakText(text: String) {
        ttsManager.speak(text)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun sendMessage(prompt: String, isVoice: Boolean = false) {
        val cleanPrompt = prompt.trim()
        if (cleanPrompt.isEmpty()) return

        val userMessage = AssistantMessage(
            sender = MessageSender.USER,
            text = cleanPrompt,
            isVoiceInput = isVoice
        )
        _messages.value = _messages.value + userMessage
        _isThinking.value = true

        viewModelScope.launch {
            val history = _messages.value.takeLast(6).map {
                (if (it.sender == MessageSender.USER) "user" else "assistant") to it.text
            }

            val result = geminiRepository.generateResponse(cleanPrompt, history)
            _isThinking.value = false

            val responseText = result.getOrElse {
                if (_currentLanguage.value == "ar") "أنا هنا لمساعدتك! ما الذي ترغب في فعله تالياً؟"
                else "I'm here to help! What would you like to do next?"
            }

            val widgetType = determineWidgetType(responseText, cleanPrompt)
            handleActionTags(responseText)

            val assistantMessage = AssistantMessage(
                sender = MessageSender.ASSISTANT,
                text = responseText,
                widgetType = widgetType
            )
            _messages.value = _messages.value + assistantMessage

            // Speak response aloud in Arabic TTS
            ttsManager.speak(responseText)
        }
    }

    private fun determineWidgetType(responseText: String, prompt: String): WidgetType {
        val lower = (responseText + " " + prompt).lowercase()
        return when {
            lower.contains("[action:weather]") || lower.contains("weather") || lower.contains("طقس") -> WidgetType.WEATHER
            lower.contains("[action:timer") || lower.contains("timer") || lower.contains("مؤقت") -> WidgetType.TIMER
            lower.contains("[action:light") || lower.contains("[action:thermostat") || lower.contains("[action:plug") ||
                    lower.contains("إضاءة") || lower.contains("مكيف") || lower.contains("قهوة") -> WidgetType.SMART_DEVICE
            lower.contains("routine") || lower.contains("روتين") -> WidgetType.ROUTINE_BRIEFING
            else -> WidgetType.NONE
        }
    }

    private fun handleActionTags(response: String) {
        if (response.contains("[ACTION:LIGHT_ON]")) {
            toggleDevice("light_living_room", true)
        }
        if (response.contains("[ACTION:LIGHT_OFF]")) {
            toggleDevice("light_living_room", false)
        }
        val thermoMatch = Regex("""\[ACTION:THERMOSTAT:(\d+)\]""").find(response)
        if (thermoMatch != null) {
            val temp = thermoMatch.groupValues[1].toIntOrNull() ?: 24
            updateDeviceValue("thermostat_main", temp)
            toggleDevice("thermostat_main", true)
        }
        if (response.contains("[ACTION:SPEAKER_PLAY]")) {
            toggleDevice("speaker_office", true)
        }
        if (response.contains("[ACTION:SPEAKER_PAUSE]")) {
            toggleDevice("speaker_office", false)
        }
        if (response.contains("[ACTION:PLUG_ON]")) {
            toggleDevice("plug_kitchen", true)
        }
        if (response.contains("[ACTION:PLUG_OFF]")) {
            toggleDevice("plug_kitchen", false)
        }
        val timerMatch = Regex("""\[ACTION:TIMER:(\d+)\]""").find(response)
        if (timerMatch != null) {
            val seconds = timerMatch.groupValues[1].toIntOrNull() ?: 300
            setTimer(seconds)
        }
        if (response.contains("[ACTION:ROUTINE_MORNING]")) {
            toggleDevice("light_living_room", true)
            updateDeviceValue("light_living_room", 80)
            toggleDevice("plug_kitchen", true)
        }
        if (response.contains("[ACTION:ROUTINE_BEDTIME]")) {
            updateDeviceValue("light_living_room", 15)
            updateDeviceValue("thermostat_main", 24)
            toggleDevice("plug_kitchen", false)
        }
    }

    fun executeRoutine(routine: RoutineItem) {
        val userPrompt = routine.command
        sendMessage(userPrompt, isVoice = false)
    }

    fun toggleDevice(deviceId: String, isOn: Boolean) {
        _smartDevices.value = _smartDevices.value.map { device ->
            if (device.id == deviceId) {
                device.copy(isOn = isOn)
            } else device
        }
    }

    fun updateDeviceValue(deviceId: String, value: Int) {
        _smartDevices.value = _smartDevices.value.map { device ->
            if (device.id == deviceId) {
                device.copy(value = value, isOn = true)
            } else device
        }
    }

    // Timer controls
    fun setTimer(seconds: Int) {
        _timerTotalSeconds.value = seconds
        _timerRemainingSeconds.value = seconds
        _timerActiveInWidget.value = true
        startTimer()
    }

    fun toggleTimerRunning() {
        if (_timerIsRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        if (_timerRemainingSeconds.value <= 0) {
            _timerRemainingSeconds.value = _timerTotalSeconds.value
        }
        _timerIsRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _timerRemainingSeconds.value > 0) {
                delay(1000)
                if (_timerIsRunning.value) {
                    val next = _timerRemainingSeconds.value - 1
                    _timerRemainingSeconds.value = next
                    if (next <= 0) {
                        _timerIsRunning.value = false
                        ttsManager.speak("انتهى وقت المؤقت المحدد!")
                        break
                    }
                }
            }
        }
    }

    private fun pauseTimer() {
        _timerIsRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        _timerIsRunning.value = false
        timerJob?.cancel()
        _timerRemainingSeconds.value = _timerTotalSeconds.value
    }

    // Reminders
    fun toggleReminder(id: String) {
        _reminders.value = _reminders.value.map { reminder ->
            if (reminder.id == id) {
                reminder.copy(isCompleted = !reminder.isCompleted)
            } else reminder
        }
    }

    fun addReminder(title: String, timeLabel: String) {
        if (title.isBlank()) return
        val newReminder = ReminderItem(title = title, timeLabel = timeLabel, isCompleted = false)
        _reminders.value = listOf(newReminder) + _reminders.value
        ttsManager.speak("تمت إضافة التذكير: $title")
    }

    fun clearChat() {
        ttsManager.stop()
        _messages.value = listOf(
            AssistantMessage(
                sender = MessageSender.ASSISTANT,
                text = "تم مسح المحادثة! كيف يمكنني مساعدتك الآن؟",
                widgetType = WidgetType.NONE
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
        timerJob?.cancel()
    }
}
