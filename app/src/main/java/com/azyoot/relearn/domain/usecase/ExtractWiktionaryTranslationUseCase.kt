package com.azyoot.relearn.domain.usecase

import com.azyoot.relearn.domain.entity.WebpageTranslation
import com.azyoot.relearn.domain.entity.WebpageVisit
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import javax.inject.Inject

class ExtractWiktionaryTranslationUseCase @Inject constructor() {
    private fun Element.indexInParent() = this.parent().children().indexOf(this)

    private fun getTranslationsForTitle(title: Element) =
        title.parent()
            ?.parent()
            ?.select("ol")
            ?.filter { it.indexInParent() > title.parent().indexInParent() }
            ?.flatMap { it.select(":root > li") }
            ?.filter { it.select("span.form-of-definition").count() == 0 }
            ?.map { it.select("a, span").text()
                ?.trim()
                ?.replace(Regex("\\s+"), " ")
                ?.replace("( ", "(")
                ?.replace(" )", ")")}
            ?.filterNotNull()
            ?: listOf()


    fun extractTranslationsFromWiktionaryPage(webpageVisit: WebpageVisit, pageText: String) =
        Jsoup.parse(pageText)
            ?.select("strong[lang=ru]") //TODO language
            ?.flatMap { titleElement ->
                val title = titleElement.text()
                getTranslationsForTitle(titleElement).map {
                    WebpageTranslation(
                        title,
                        it,
                        webpageVisit
                    )
                }
            }?.distinct() ?: listOf()
}