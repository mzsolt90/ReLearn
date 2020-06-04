package com.azyoot.relearn.domain.usecase.parsing

import com.azyoot.relearn.domain.entity.WebpageTranslation
import com.azyoot.relearn.domain.entity.WebpageVisit
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode
import timber.log.Timber
import javax.inject.Inject

class ExtractWiktionaryTranslationUseCase @Inject constructor() {
    private fun Element.indexInParent() = this.parent().children().indexOf(this)

    private fun Collection<Element>.skipFormOfDefinitions() =
        filter { it.select("span.form-of-definition").count() == 0 }

    private fun String.fixSpaces() = trim()
        .replace(Regex("\\s+"), " ")
        .replace("( ", "(")
        .replace(" )", ")")

    private fun String.simplifyDashes() = replace(Regex("([\\-–—−―]+)[\\s\\-–—−―]+"), "$1 ")

    private fun Collection<Element>.getTranslationRows() = flatMap { it.select(":root > li") }

    private fun Collection<Element>.removeTransliterations() =
        apply {
            forEach {
                it.select(".e-transliteration").forEach { it.remove() }
            }
        }

    private fun Collection<Element>.addDashForUsageExample() =
        apply {
            forEach {
                it.select(".e-example,.e-translation").forEach {
                    it.insertChildren(0, TextNode(" ― "))
                }
            }
        }

    private fun Collection<Element>.addDashForSynonym() =
        apply {
            forEach {
                it.select(".synonym").forEach {
                    it.insertChildren(0, TextNode(" ― "))
                }
            }
        }

    private fun Element.indexOfNextTitle() =
        parent().select("strong")
            .map { it.parent().indexInParent() } /* title spans are wrapped in <p> */
            .filter { it > this.indexInParent() }
            .min()

    private fun getTranslationsForTitle(title: Element) =
        title.parent()
            ?.select("ol")
            ?.filter { it.indexInParent() > title.indexInParent() }
            ?.filter { it.indexInParent() < title.indexOfNextTitle() ?: Int.MAX_VALUE }
            ?.getTranslationRows()
            ?.skipFormOfDefinitions()
            ?.removeTransliterations()
            ?.addDashForUsageExample()
            ?.addDashForSynonym()
            ?.map {
                it.text()
                    ?.simplifyDashes()
                    ?.fixSpaces()
            }
            ?.filterNotNull()
            ?: listOf()


    fun extractTranslationsFromWiktionaryPage(webpageVisit: WebpageVisit, pageText: String) =
        Jsoup.parse(pageText)
            ?.select("strong[lang=ru]") //TODO language
            ?.flatMap { titleElement ->
                val title = titleElement.text()
                getTranslationsForTitle(titleElement.parent()).map {
                    WebpageTranslation(
                        fromText = title,
                        toText = it,
                        webpageVisit = webpageVisit
                    )
                }
            }?.distinct()?.also {
                Timber.d("Found ${it.size} translations for ${webpageVisit.url}")
            } ?: listOf()
}