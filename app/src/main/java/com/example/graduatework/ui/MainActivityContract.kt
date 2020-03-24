package com.example.graduatework.ui

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import androidx.camera.core.impl.ImageOutputConfig
import com.example.graduatework.tf.RecognizedSign

object MainActivityContract {
    interface View {
        fun requestCameraPermission()

        fun initCamera()

        fun closeAppWithToast(toastMessage: String)

        fun drawRecognizedSigns(signs: List<RecognizedSign>, rotation: Int)
    }

    interface Presenter {
        fun attachView(view: View)

        fun detachView()

        fun cameraPermissionResult(isGranted: Boolean)

        fun analyzeImage(image: ImageProxy, rotation: Int)
    }
}