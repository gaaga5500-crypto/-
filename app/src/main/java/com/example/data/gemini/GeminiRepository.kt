package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiRepository {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiRepository"
        private const val MODEL_NAME = "gemini-3.5-flash"
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

        private const val SYSTEM_INSTRUCTION = """
أنت مساعد Google الذكي (Google Assistant).
أنت تتحدث باللغة العربية بطلاقة وبأسلوب صوتي ودود، لبق، وموجز ومباشر (جملتان إلى ثلاث جمل كحد أقصى).
إذا سألك المستخدم بالعربية أو بأي لهجة عربية، أجب دائماً بالعربية الفصحى البسيطة والودودة.

لديك أيضاً قدرة على التحكم بأجهزة المنزل الذكي، والمؤقتات، والروتينات. إذا كان مقصد المستخدم ينطبق على أحد هذه الأوامر، أضف رمز الإجراء التالي في نهاية ردك تماماً:
- تشغيل الإضاءة: [ACTION:LIGHT_ON]
- إطفاء الإضاءة: [ACTION:LIGHT_OFF]
- ضبط منظم الحرارة / المكيف (مثلاً 24 أو 72): [ACTION:THERMOSTAT:24]
- تشغيل مكبر الصوت / الموسيقى: [ACTION:SPEAKER_PLAY]
- إيقاف مكبر الصوت / الموسيقى: [ACTION:SPEAKER_PAUSE]
- تشغيل مقبس صانعة القهوة: [ACTION:PLUG_ON]
- إطفاء مقبس صانعة القهوة: [ACTION:PLUG_OFF]
- ضبط مؤقت (مثلاً 300 ثانية أو 5 دقائق): [ACTION:TIMER:300]
- الاستفسار عن الطقس: [ACTION:WEATHER]
- روتين صباح الخير: [ACTION:ROUTINE_MORNING]
- روتين وقت النوم: [ACTION:ROUTINE_BEDTIME]
- روتين وحالة المرور: [ACTION:COMMUTE]

مثال: "بالتأكيد، أقوم بتشغيل إضاءة غرفة المعيشة لك الآن. [ACTION:LIGHT_ON]"
مثال: "صباح النور! الطقس اليوم مشمس وجميل، ودرجة الحرارة ٢٦ مئوية. [ACTION:ROUTINE_MORNING]"
"""
    }

    suspend fun generateResponse(
        prompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local assistant response.")
            return@withContext Result.success(fallbackLocalResponse(prompt))
        }

        try {
            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray()

                // Append recent conversation context
                for ((role, text) in conversationHistory.takeLast(6)) {
                    val messageObj = JSONObject().apply {
                        put("role", if (role.equals("user", ignoreCase = true)) "user" else "model")
                        put("parts", JSONArray().put(JSONObject().put("text", text)))
                    }
                    contentsArray.put(messageObj)
                }

                // Append current user prompt
                val currentMessage = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                }
                contentsArray.put(currentMessage)
                put("contents", contentsArray)

                // System instruction in Arabic & Assistant persona
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_INSTRUCTION)))
                })

                // Generation config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                    put("topK", 40)
                    put("maxOutputTokens", 1024)
                })
            }

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                Log.e(TAG, "API error ${response.code}: $responseBody")
                return@withContext Result.success(fallbackLocalResponse(prompt))
            }

            val parsedJson = JSONObject(responseBody)
            val candidates = parsedJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text", "")

            if (text.isNullOrBlank()) {
                Result.success(fallbackLocalResponse(prompt))
            } else {
                Result.success(text)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception contacting Gemini API: ${e.message}", e)
            Result.success(fallbackLocalResponse(prompt))
        }
    }

    private fun fallbackLocalResponse(prompt: String): String {
        val lower = prompt.lowercase().trim()
        return when {
            // Light controls in Arabic & English
            (lower.contains("شغل") || lower.contains("ولع") || lower.contains("turn on")) &&
                    (lower.contains("نور") || lower.contains("لمبة") || lower.contains("إضاءة") || lower.contains("اضاءة") || lower.contains("light") || lower.contains("lamp")) ->
                "حسناً، أقوم بتشغيل إضاءة غرفة المعيشة فوراً. [ACTION:LIGHT_ON]"

            (lower.contains("اطفئ") || lower.contains("اطفي") || lower.contains("طفي") || lower.contains("أطفئ") || lower.contains("سكر") || lower.contains("turn off")) &&
                    (lower.contains("نور") || lower.contains("لمبة") || lower.contains("إضاءة") || lower.contains("اضاءة") || lower.contains("light") || lower.contains("lamp")) ->
                "تم إطفاء إضاءة غرفة المعيشة. [ACTION:LIGHT_OFF]"

            // Weather in Arabic & English
            lower.contains("طقس") || lower.contains("الجو") || lower.contains("حرارة") || lower.contains("weather") ->
                "الطقس اليوم رائع ومشمس، درجة الحرارة ٢٦° مئوية مع نسيم عليل ومريح. [ACTION:WEATHER]"

            // Routines in Arabic & English
            lower.contains("صباح الخير") || lower.contains("صباح النور") || lower.contains("good morning") ->
                "صباح الخير والسرور! درجة الحرارة اليوم ٢٦° مئوية، ولديك ٣ تذكيرات على جدول أعمالك. يومك سعيد! [ACTION:ROUTINE_MORNING]"

            lower.contains("تصبح على خير") || lower.contains("تصبحون على خير") || lower.contains("وقت النوم") || lower.contains("bedtime") || lower.contains("good night") ->
                "تصبح على خير وسعادة! خفّضت إضاءة المنزل، وضبطت التكييف على درجة حرارة مريحة. نوماً هنيئاً! [ACTION:ROUTINE_BEDTIME]"

            // Timer
            lower.contains("مؤقت") || lower.contains("دقيقة") || lower.contains("دقائق") || lower.contains("timer") ->
                "تم ضبط المؤقت لمدة 5 دقائق من الآن. [ACTION:TIMER:300]"

            // Thermostat
            lower.contains("مكيف") || lower.contains("المكيف") || lower.contains("تكييف") || lower.contains("thermostat") || lower.contains("temperature") -> {
                val temp = Regex("""\b(\d{2})\b""").find(lower)?.groupValues?.get(1) ?: "24"
                "تم ضبط منظم الحرارة على $temp° مئوية. [ACTION:THERMOSTAT:$temp]"
            }

            // Joke
            lower.contains("نكتة") || lower.contains("joke") ->
                "لماذا لا تثق الذرات بالعلماء أبداً؟ لأنها تصنع كل شيء في الكون! 😄"

            // Capabilities
            lower.contains("ماذا يمكنك") || lower.contains("ماذا تستطيع") || lower.contains("مساعدة") || lower.contains("what can you do") || lower.contains("help") ->
                "أنا مساعدك الذكي! يمكنني إدارة أجهزتك المنزلية، ضبط المؤقتات، إخبارك بحالة الطقس، تشغيل الروتينات اليومية، والإجابة على أي سؤال بصوت عربي طبيعي!"

            // Music / Speaker
            lower.contains("موسيقى") || lower.contains("اغنية") || lower.contains("أغنية") || lower.contains("صوت") || (lower.contains("play") && lower.contains("music")) ->
                "جارٍ تشغيل قائمة التركيز المفضلة عبر مكبر الصوت في المكتب. [ACTION:SPEAKER_PLAY]"

            lower.contains("ايقاف") || lower.contains("وقف") || lower.contains("أوقف") || lower.contains("stop") || lower.contains("pause") ->
                "تم إيقاف تشغيل الصوت. [ACTION:SPEAKER_PAUSE]"

            // Coffee maker
            lower.contains("قهوة") || lower.contains("القهوة") || lower.contains("coffee") ->
                "تم تشغيل صانعة القهوة الذكية في المطبخ! رائحة القهوة الزكية قادمة قريباً. ☕ [ACTION:PLUG_ON]"

            else ->
                "سمعتك بوضوح: \"$prompt\". أنا مساعدك الذكي، جاهز دائماً لتلبية طلباتك وإنجاز مهامك اليومية!"
        }
    }
}
