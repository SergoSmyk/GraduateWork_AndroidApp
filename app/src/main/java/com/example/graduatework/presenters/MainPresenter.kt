package com.example.graduatework.presenters

import androidx.camera.core.ImageProxy
import com.example.graduatework.tf.TFTrafficSignsModel
import com.example.graduatework.tools.Constants.MIN_RESULT_SCORE
import com.example.graduatework.tools.suspendMeasureAndLog
import com.example.graduatework.ui.MainActivityContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.CoroutineContext

class MainPresenter(
    private val tfModel: TFTrafficSignsModel
) : MainActivityContract.Presenter, CoroutineScope {

    private var view: MainActivityContract.View? = null
    private lateinit var presenterJob: Job

    override val coroutineContext: CoroutineContext
        get() = presenterJob + Dispatchers.Default

    override fun attachView(view: MainActivityContract.View) {
        presenterJob = Job()
        this.view = view
        view.requestCameraPermission()
    }

    override fun detachView() {
        presenterJob.cancel()
        tfModel.close()
        this.view = null
    }

    override fun cameraPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            view?.initCamera()
        } else {
            view?.closeAppWithToast("App cannot work without CAMERA permission")
        }
    }

    override fun analyzeImage(image: ImageProxy, rotation: Int) {
        launch {
            with(tfModel) {
                initialize()

                val signs = suspendMeasureAndLog("Full analyzing time: ", TAG) {
                    analyzeImage(image, rotation)
                        .filter { it.score > MIN_RESULT_SCORE }
                        .sortedBy { -it.score }
                }
                signs.forEach {
                    Timber.d("${it.label} ${it.score}")
                }

                image.close()
                view?.drawRecognizedSigns(signs, rotation)
                    ?: Timber.w("Trying to draw recognized signs on NULL view")
            }
        }
    }

    companion object {
        private const val TAG = "MainPresenter"
    }
}
