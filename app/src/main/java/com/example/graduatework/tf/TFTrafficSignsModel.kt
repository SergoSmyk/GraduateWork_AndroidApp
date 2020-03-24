package com.example.graduatework.tf

import android.content.res.AssetManager
import androidx.annotation.WorkerThread
import androidx.camera.core.ImageProxy
import com.example.graduatework.tools.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.atomic.AtomicBoolean

class TFTrafficSignsModel(private val assetManager: AssetManager) {

    private val atomicIsInitialised = AtomicBoolean(false)

    private val isInitialised: Boolean
        get() = atomicIsInitialised.get()

    private lateinit var tfLite: Interpreter
    private lateinit var labels: List<String>

    //region Initialization

    suspend fun initialize() {
        if (isInitialised) {
            return
        }
        innerInitialize()
    }

    private suspend fun innerInitialize() = withContext(Dispatchers.IO) {
        tfLite = loadModelFile(assetManager)
        labels = loadLabelsList(assetManager)
        atomicIsInitialised.set(true)
    }

    @WorkerThread
    private fun loadModelFile(assets: AssetManager): Interpreter {
        with(assets.openFd(Constants.TF_OD_MODEL_FILENAME)) {

            val fileChannel = FileInputStream(fileDescriptor).channel.map(
                FileChannel.MapMode.READ_ONLY,
                startOffset,
                declaredLength
            )

            return Interpreter(fileChannel as ByteBuffer)
        }
    }

    @WorkerThread
    private fun loadLabelsList(assets: AssetManager): List<String> {
        val inputStream = assets.open(Constants.TF_OD_LABELS_FILENAME)
        with(BufferedReader(InputStreamReader(inputStream))) {
            return readLines()
        }
    }

    //endregion

    //region Object Detection
    suspend fun analyzeImage(
        image: ImageProxy,
        rotation: Int
    ): List<RecognizedSign> = withContext(Dispatchers.Default) {
        if (!isInitialised) {
            throw NullPointerException("TF Model not initialized")
        }

        val input = TFInput.create(
            image = image,
            isModelQuantized = true,
            sensorOrientation = rotation
        )
        val output = TFOutput()

        tfLite.runForMultipleInputsOutputs(
            input.getInput(),
            output.getOutput()
        )

        output.mapToRecognizedSignsList(labels)
    }

    //endregion
}