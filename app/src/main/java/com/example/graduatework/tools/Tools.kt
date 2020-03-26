package com.example.graduatework.tools

import android.os.SystemClock
import timber.log.Timber

fun <T> measureAndLog(logText: String, tag: String? = null, code: () -> T): T {
    val startTime = SystemClock.uptimeMillis()
    val codeResult = code.invoke()
    val endTime = SystemClock.uptimeMillis()
    tag ?.let {
        Timber.tag(it).i("$logText : ${endTime - startTime}")
    } ?: run {
        Timber.i("$logText : ${endTime - startTime}")
    }

    return codeResult
}

suspend fun <T> suspendMeasureAndLog(logText: String, tag: String? = null, code: suspend () -> T): T {
    val startTime = SystemClock.uptimeMillis()
    val codeResult = code.invoke()
    val endTime = SystemClock.uptimeMillis()
    tag ?.let {
        Timber.tag(it).i("$logText : ${endTime - startTime}")
    } ?: run {
        Timber.i("$logText : ${endTime - startTime}")
    }

    return codeResult
}