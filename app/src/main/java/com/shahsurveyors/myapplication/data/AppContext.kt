package com.shahsurveyors.myapplication.data

import android.app.Application
import android.content.Context

object AppContext {

    private lateinit var context: Context

    fun initialize(application: Application) {
        context = application.applicationContext
    }

    fun get(): Context {
        if (!::context.isInitialized) {
            throw IllegalStateException(
                "AppContext has not been initialized"
            )
        }

        return context
    }
}