package com.example.graduatework.app.di

import com.example.graduatework.ui.MainActivityContract
import com.example.graduatework.presenters.MainPresenter
import com.example.graduatework.tf.TFInputProvider
import com.example.graduatework.tf.TFOutputProvider
import com.example.graduatework.tf.TFTrafficSignsModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { androidApplication().assets }
    single { TFInputProvider() }
    single { TFOutputProvider() }
    single { TFTrafficSignsModel(get(), get(), get()) }
    single<MainActivityContract.Presenter> { MainPresenter(get()) }
}