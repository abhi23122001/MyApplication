package com.shahsurveyors.myapplication.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

/**
 * Official branding assets used by every exported billing PDF.
 * Assets are bundled in res/drawable with these exact names.
 */
object PdfBrandingAssets {
    private const val LOGO_ASSET = "app_logo"
    private const val SEAL_SIGNATURE_ASSET = "seal_sign"

    fun loadLogo(context: Context): Bitmap? = loadDrawable(context, LOGO_ASSET)

    fun loadSealAndSignature(context: Context): Bitmap? =
        loadDrawable(context, SEAL_SIGNATURE_ASSET)

    private fun loadDrawable(context: Context, name: String): Bitmap? {
        val resId = context.resources.getIdentifier(
            name,
            "drawable",
            context.packageName
        )
        if (resId == 0) return null
        return BitmapFactory.decodeResource(context.resources, resId)
    }
}
