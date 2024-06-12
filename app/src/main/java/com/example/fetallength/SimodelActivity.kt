package com.example.fetallength

import android.annotation.SuppressLint
import android.content.res.AssetManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SimodelActivity : AppCompatActivity() {

    private lateinit var interpreter: Interpreter
    private val mModelPath = "fetal.tflite"

    private lateinit var resultText: TextView
    private lateinit var accelerations: EditText
    private lateinit var fetal_movement: EditText
    private lateinit var uterine_contractions: EditText
    private lateinit var light_decelerations: EditText
    private lateinit var severe_decelerations: EditText
    private lateinit var prolongued_decelerations: EditText
    private lateinit var abnormal_short_term_variability: EditText
    private lateinit var checkButton : Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simodel)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        resultText = findViewById(R.id.txtResult)
        accelerations = findViewById(R.id.accelerations)
        fetal_movement = findViewById(R.id.fetal_movement)
        uterine_contractions = findViewById(R.id.uterine_contractions)
        light_decelerations = findViewById(R.id.light_decelerations)
        severe_decelerations = findViewById(R.id.severe_decelerations)
        prolongued_decelerations = findViewById(R.id.severe_decelerations)
        abnormal_short_term_variability = findViewById(R.id.abnormal_short_term_variability)
        checkButton = findViewById(R.id.btnCheck)

        checkButton.setOnClickListener {
            var result = doInference(
                accelerations.text.toString(),
                fetal_movement.text.toString(),
                uterine_contractions.text.toString(),
                light_decelerations.text.toString(),
                severe_decelerations.text.toString(),
                severe_decelerations.text.toString(),
                abnormal_short_term_variability.text.toString())
            runOnUiThread {
                if (result == 0) {
                    resultText.text = "Normal"
                }else if (result == 1){
                    resultText.text = "Suspect"
                }else if (result == 2){
                    resultText.text = "Pathological"
                }
            }
        }
        initInterpreter()
    }

    private fun initInterpreter() {
        val options = org.tensorflow.lite.Interpreter.Options()
        options.setNumThreads(7)
        options.setUseNNAPI(true)
        interpreter = org.tensorflow.lite.Interpreter(loadModelFile(assets, mModelPath), options)
    }

    private fun doInference(input1: String, input2: String, input3: String, input4: String, input5: String, input6: String, input7: String): Int{
        val inputVal = FloatArray(7)
        inputVal[0] = input1.toFloat()
        inputVal[1] = input2.toFloat()
        inputVal[2] = input3.toFloat()
        inputVal[3] = input4.toFloat()
        inputVal[4] = input5.toFloat()
        inputVal[5] = input6.toFloat()
        inputVal[6] = input7.toFloat()
        val output = Array(1) { FloatArray(3) }
        interpreter.run(inputVal, output)

        Log.e("result", (output[0].toList()+" ").toString())

        return output[0].indexOfFirst { it == output[0].maxOrNull() }
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer{
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}