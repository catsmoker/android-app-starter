package com.example.androidstarter.application.appearance

import com.example.androidstarter.ui.components.languageEndonym
import com.example.androidstarter.ui.components.languageOptions
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.*
import org.junit.Test

class LanguageConfigurationTest {
    @Test fun `British English keeps its shipped regional table`() {
        assertEquals("en-GB", LocaleHelper.normalizeTag("en-GB"))
        assertEquals("en-GB", LocaleHelper.normalizeTag("EN-gb"))
        assertEquals("en", LocaleHelper.normalizeTag("en-US"))
    }

    @Test fun `French region variants use French`() {
        assertEquals("fr", LocaleHelper.normalizeTag("fr-CA"))
        assertEquals("fr", LocaleHelper.normalizeTag("FR-fr"))
    }

    @Test fun `other supported region variants remain normalized`() {
        for ((input, expected) in mapOf("ar-SA" to "ar", "es-MX" to "es", "ja-JP" to "ja",
            "pt-PT" to "pt-BR", "zh-Hans-CN" to "zh-CN")) {
            assertEquals(input, expected, LocaleHelper.normalizeTag(input))
        }
    }

    @Test fun `system follows system locale while an explicit selection wins`() {
        assertEquals("en-GB", LocaleHelper.effectiveLanguageTag("", "en-GB"))
        assertEquals("fr", LocaleHelper.effectiveLanguageTag("fr", "ar-SA"))
        assertEquals("", LocaleHelper.normalizeTag(""))
        assertEquals("de-DE", LocaleHelper.normalizeTag("de-DE"))
    }

    @Test fun `every picker locale has a native name and normalization is stable`() {
        val options = languageOptions()
        assertEquals(options.size, options.toSet().size)
        assertTrue(options.containsAll(listOf("en-GB", "fr")))
        for (tag in options.filter { it.isNotEmpty() }) {
            assertTrue(tag, languageEndonym(tag).isNotEmpty())
            assertEquals(tag, tag, LocaleHelper.normalizeTag(tag))
        }
    }

    @Test fun `Android advertised locales exactly match the in app picker`() {
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val nodes = factory.newDocumentBuilder().parse(File("src/main/res/xml/locales_config.xml"))
            .getElementsByTagName("locale")
        val advertised = (0 until nodes.length).map { index ->
            nodes.item(index).attributes.getNamedItemNS("http://schemas.android.com/apk/res/android", "name").nodeValue
        }.toSet()
        assertEquals(advertised, languageOptions().filter { it.isNotEmpty() }.toSet())
    }
}
