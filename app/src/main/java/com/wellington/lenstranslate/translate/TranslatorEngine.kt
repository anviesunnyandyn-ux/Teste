package com.wellington.lenstranslate.translate

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await

class TranslatorEngine(
    source: String = TranslateLanguage.JAPANESE,
    target: String = TranslateLanguage.PORTUGUESE
) {
    private val translator = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(source)
            .setTargetLanguage(target)
            .build()
    )

    suspend fun translate(text: String): String {
        if (text.isBlank()) return "Nenhum texto detectado."
        translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
        return translator.translate(text).await()
    }
}
