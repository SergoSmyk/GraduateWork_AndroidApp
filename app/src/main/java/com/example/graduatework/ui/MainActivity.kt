package com.example.graduatework.ui

import android.Manifest.permission.CAMERA
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Surface
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.graduatework.R
import com.example.graduatework.tf.RecognizedSign
import com.example.graduatework.tools.ImageTransformer
import com.example.graduatework.ui.adapter.Adapter
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : ComponentActivity(R.layout.activity_main),
    MainActivityContract.View {

    private val presenter by inject<MainActivityContract.Presenter>()

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = Adapter()
        namesList.adapter = adapter
        presenter.attachView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private val cameraPermissionsCall =
        registerForActivityResult(RequestPermission()) { isGranted ->
            Timber.d("CAMERA permission GRANTED")
            presenter.cameraPermissionResult(isGranted)
        }

    override fun requestCameraPermission() {
        if (checkSelfPermission(CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Timber.d("Request CAMERA permission")
            cameraPermissionsCall.launch(CAMERA)
        } else {
            presenter.cameraPermissionResult(isGranted = true)
        }
    }

    override fun initCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this).also { future ->
            future.addListener(Runnable {
                val cameraProvider = cameraProviderFuture.get()
                bindCameraToLifecycle(cameraProvider)
            }, ContextCompat.getMainExecutor(this))
        }
    }

    override fun closeAppWithToast(toastMessage: String) {
        Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()
        this.finish()
    }

    override fun drawRecognizedSigns(signs: List<RecognizedSign>, rotation: Int) {
        val coloredSigns = overlayView.updateSigns(signs)
        overlayView.setRotation(rotation)
        lifecycleScope.launch(Dispatchers.Main) {
            adapter.setNewItems(coloredSigns)
        }
    }

    private fun bindCameraToLifecycle(provider: ProcessCameraProvider) {

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val preview = getCameraPreview(previewView)
        val analysis = getCameraAnalysis()

        provider.bindToLifecycle(this, cameraSelector, preview, analysis)
    }

    private fun getCameraPreview(previewView: PreviewView): Preview {
        return Preview.Builder()
            .build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
    }

    private fun getCameraAnalysis(): ImageAnalysis {

        return ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(Surface.ROTATION_90)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().apply {
                setAnalyzer(Dispatchers.Default.asExecutor(), ImageAnalysis.Analyzer { image ->
                    val rotationDegrees = image.imageInfo.rotationDegrees + 90
                    ImageTransformer.setAnalyzerImageSize(image.width, image.height)
                    presenter.analyzeImage(image, rotationDegrees)
                })
            }
    }
}
