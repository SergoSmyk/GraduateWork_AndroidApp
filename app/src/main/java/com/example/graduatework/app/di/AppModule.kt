package com.example.graduatework.app.di

import com.example.graduatework.ui.MainActivityContract
import com.example.graduatework.presenters.MainPresenter
import com.example.graduatework.tf.TFTrafficSignsModel
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val appModule = module {
    single { androidApplication().assets }
    single { TFTrafficSignsModel(get()) }
    single<MainActivityContract.Presenter> { MainPresenter(get()) }
}