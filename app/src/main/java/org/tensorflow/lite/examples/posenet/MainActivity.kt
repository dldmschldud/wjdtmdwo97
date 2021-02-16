/*
정확도 높인 버전
 */
package org.tensorflow.lite.examples.posenet

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*

class MainActivity : AppCompatActivity(), OnInitListener {
    private var initialized = false
    private var queuedText: String? = null
    private var tts: TextToSpeech? = null
    var imageButton: Button? = null
    var button: Button? = null
    var inputText: EditText? = null
    var textView: TextView? = null
    var intent1:Intent? = null
    var sRecognizer: SpeechRecognizer? = null
    val PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.first_page)
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO), PERMISSION)
        }
        textView = findViewById<View>(R.id.sttResult) as TextView
        imageButton = findViewById<View>(R.id.btn1) as Button
        button = findViewById<View>(R.id.button) as Button
        inputText = findViewById<View>(R.id.editText) as EditText
        imageButton!!.setOnClickListener(buttonListener)
        button!!.setOnClickListener(buttonListener)
        intent1 = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent1!!.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        intent1!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")

        tts = TextToSpeech(this /* context */, this /* listener */)
        tts!!.setOnUtteranceProgressListener(mProgressListener)
        speak("안녕하세요 ")
        intent1 = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent1!!.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.packageName)
        intent1!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
    }

    fun speak(text: String?) {
        if (!initialized) {
            queuedText = text
            return
        }
        queuedText = null
        setTtsListener()
        val map = HashMap<String, String>()
        map[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "MessageId"
        tts!!.speak(text, TextToSpeech.QUEUE_ADD, map)
    }

    private fun setTtsListener() {}
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            initialized = true
            tts!!.language = Locale.KOREAN
            if (queuedText != null) {
                speak(queuedText)
            }
        }
    }

    private abstract inner class runnable : Runnable

    private val mProgressListener: UtteranceProgressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String) {}
        override fun onError(utteranceId: String) {}
        override fun onDone(utteranceId: String) {
            object : Thread() {
                override fun run() {
                    runOnUiThread(object : runnable() {
                        override fun run() {

                            sRecognizer = SpeechRecognizer.createSpeechRecognizer(this@MainActivity)
                            sRecognizer?.setRecognitionListener(listener)
                            sRecognizer?.startListening(intent1)
                        }
                    })
                }
            }.start()
        }
    }
    private val listener: RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle) {
            Toast.makeText(applicationContext, "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show()
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray) {}
        override fun onEndOfSpeech() {}
        override fun onError(error: Int) {
            val message: String
            message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> "클라이언트 에러"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "퍼미션 없음"
                SpeechRecognizer.ERROR_NETWORK -> "네트워크 에러"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "네트웍 타임아웃"
                SpeechRecognizer.ERROR_NO_MATCH -> "찾을 수 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RECOGNIZER가 바쁨"
                SpeechRecognizer.ERROR_SERVER -> "서버가 이상함"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "말하는 시간초과"
                else -> "알 수 없는 오류임"
            }
            Toast.makeText(applicationContext, "에러가 발생하였습니다. : $message", Toast.LENGTH_SHORT).show()
        }

        override fun onResults(results: Bundle) {
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            for (i in matches!!.indices) {
                textView!!.text = matches[i]
                val string = "나무"
                if (textView!!.text.toString() == string) {
                    val intent = Intent(applicationContext, CameraActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        override fun onPartialResults(partialResults: Bundle) {}
        override fun onEvent(eventType: Int, params: Bundle) {}
    }
    var buttonListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.btn1 -> {
                val intent = Intent(applicationContext, CameraActivity::class.java)
                startActivity(intent)
            }
            R.id.button -> {
                val string = "tree"
                if (inputText!!.text.toString() == string) {
                    intent = Intent(applicationContext, CameraActivity::class.java)
                    startActivity(intent)
                } else Toast.makeText(applicationContext, "해당 자세는 없습니다 ", Toast.LENGTH_SHORT).show()
            }
        }
    }

}