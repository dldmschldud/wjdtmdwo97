/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import android.speech.tts.UtteranceProgressListener
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*

class CameraActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
  private var tts1: TextToSpeech? = null
  private var initialized = false
  private var queuedText: String? = null
  var textView: TextView? = null
  var intent1:Intent? = null
  var sRecognizer: SpeechRecognizer? = null
  val PERMISSION = 1
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.tfe_pn_activity_camera)
    if (Build.VERSION.SDK_INT >= 23) {
      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO), PERMISSION)
    }
    savedInstanceState ?: supportFragmentManager.beginTransaction()
            .replace(R.id.container, PosenetActivity())
            .commit()

    tts1 = TextToSpeech(this /* context */,this /* listener */)
    tts1!!.setOnUtteranceProgressListener(mProgressListener)
    speak("요가자세 설명")
    textView = findViewById<View>(R.id.sttResult2) as TextView

    intent1 = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    intent1!!.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.packageName)
    intent1!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR")
    val imageButton = findViewById<View>(R.id.btn2) as Button
    imageButton.setOnClickListener {
      val intent = Intent(applicationContext, MainActivity::class.java)
      startActivity(intent)
        }

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
    tts1!!.speak(text, TextToSpeech.QUEUE_ADD, map)
  }
  private fun setTtsListener() {}

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      initialized = true
      tts1!!.language = Locale.KOREAN
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
              Toast.makeText(applicationContext, "자세설명종료.", Toast.LENGTH_SHORT).show()

              sRecognizer = SpeechRecognizer.createSpeechRecognizer(this@CameraActivity)
              sRecognizer?.setRecognitionListener(listenerR)
              sRecognizer?.startListening(intent1)
            }
          })
        }
      }.start()
    }
  }
  private val listenerR: RecognitionListener = object : RecognitionListener {
    override fun onReadyForSpeech(params: Bundle) {
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
        //textView!!.text = matches[i]
      }
    }

    override fun onPartialResults(partialResults: Bundle) {}
    override fun onEvent(eventType: Int, params: Bundle) {}
  }
  override fun onDestroy() {
    super.onDestroy()
    if (tts1 != null) {
      tts1!!.stop()
      tts1!!.shutdown()
      }
    }


}

/*
    //카메라로 사용자 동작 촬영하면서 동시에 음성으로 자세 설명하는 부분
    tts = TextToSpeech(applicationContext, object : TextToSpeech.OnInitListener {
      override fun onInit(status: Int) {
        if (status != TextToSpeech.ERROR) {
          tts!!.language = Locale.KOREAN
        }
        val utteranceId = this.hashCode().toString() + ""
        tts?.speak("요가자세 설명", TextToSpeech.QUEUE_FLUSH, null,utteranceId);
      }

    })
  }
*/