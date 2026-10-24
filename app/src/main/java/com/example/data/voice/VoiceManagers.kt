package com.example.data.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class SpeechManager(
    private val context: Context,
    private val onPartialResult: (String) -> Unit,
    private val onFinalResult: (String) -> Unit,
    private val onListeningStateChanged: (Boolean) -> Unit,
    private val onErrorOccurred: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    var currentLanguageTag: String = "ar-SA" // Default to Arabic

    init {
        initRecognizer()
    }

    private fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        isListening = true
                        onListeningStateChanged(true)
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        isListening = false
                        onListeningStateChanged(false)
                    }

                    override fun onError(error: Int) {
                        isListening = false
                        onListeningStateChanged(false)
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "خطأ في تسجيل الصوت / Audio recording error"
                            SpeechRecognizer.ERROR_CLIENT -> "خطأ في الاتصال / Client error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "مطلوب إذن الميكروفون / Microphone permission needed"
                            SpeechRecognizer.ERROR_NETWORK -> "مشكلة في شبكة الإنترنت / Network issue"
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "انتهت مهلة الشبكة / Network timeout"
                            SpeechRecognizer.ERROR_NO_MATCH -> "لم يتم التعرف على الصوت، حاول مجدداً / No speech detected"
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "المساعد مشغول حالياً / Assistant busy"
                            SpeechRecognizer.ERROR_SERVER -> "خطأ في خادم التعرف الصوتي / Server error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "انتهت مهلة التحدث / Speech timeout"
                            else -> "خطأ في التعرف الصوتي: $error"
                        }
                        Log.w("SpeechManager", errorMsg)
                        onErrorOccurred(errorMsg)
                    }

                    override fun onResults(results: Bundle?) {
                        isListening = false
                        onListeningStateChanged(false)
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull().orEmpty()
                        if (text.isNotBlank()) {
                            onFinalResult(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull().orEmpty()
                        if (text.isNotBlank()) {
                            onPartialResult(text)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            initRecognizer()
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguageTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, currentLanguageTag)
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ar-SA", "ar", "en-US"))
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("SpeechManager", "Failed to start listening", e)
            onErrorOccurred("Could not start speech recognition: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            isListening = false
            onListeningStateChanged(false)
        } catch (e: Exception) {
            Log.e("SpeechManager", "Failed to stop listening", e)
        }
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}

class TtsManager(
    context: Context,
    private val onSpeakingStateChanged: (Boolean) -> Unit
) {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    var isMuted: Boolean = false
    private val arabicLocale = Locale.forLanguageTag("ar-SA")

    init {
        textToSpeech = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Try Arabic locale first
                val result = textToSpeech?.setLanguage(arabicLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val genericArabic = Locale.forLanguageTag("ar")
                    val genericResult = textToSpeech?.setLanguage(genericArabic)
                    if (genericResult == TextToSpeech.LANG_MISSING_DATA || genericResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        textToSpeech?.language = Locale.getDefault()
                    }
                }
                textToSpeech?.setPitch(1.05f)
                textToSpeech?.setSpeechRate(1.0f)
                isInitialized = true
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        onSpeakingStateChanged(true)
                    }

                    override fun onDone(utteranceId: String?) {
                        onSpeakingStateChanged(false)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        onSpeakingStateChanged(false)
                    }
                })
            } else {
                Log.e("TtsManager", "TTS initialization failed: $status")
            }
        }
    }

    fun speak(text: String) {
        if (isMuted || !isInitialized || text.isBlank()) return

        // Strip action tags before speaking aloud
        val cleanText = text.replace(Regex("""\[ACTION:[^\]]+\]"""), "").trim()
        if (cleanText.isEmpty()) return

        // Check if text is Arabic or English and adjust locale
        val hasArabic = cleanText.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' }
        if (hasArabic) {
            textToSpeech?.setLanguage(arabicLocale)
        } else {
            textToSpeech?.setLanguage(Locale.US)
        }

        val utteranceId = "assistant_utterance_${System.currentTimeMillis()}"
        textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        textToSpeech?.stop()
        onSpeakingStateChanged(false)
    }

    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
