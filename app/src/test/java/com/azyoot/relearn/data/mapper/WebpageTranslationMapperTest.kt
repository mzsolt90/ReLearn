package com.azyoot.relearn.data.mapper


import com.azyoot.relearn.domain.entity.WebpageVisit
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDateTime
import com.azyoot.relearn.data.entity.WebpageTranslation as DataEntity
import com.azyoot.relearn.domain.entity.WebpageTranslation as DomainEntity

class WebpageTranslationMapperTest {

    private val mapper = WebpageTranslationMapper()

    @Test
    fun `Given domain entity When mapped to data entity Then correctly mapped all properties`() {
        val domainEntity = DomainEntity(FROM_TEXT, TO_TEXT, WEBPAGE_VISIT, PARSE_VERSION, ID)

        val mapped = mapper.toDataEntity(domainEntity)

        assertThat(mapped).isEqualTo(DataEntity(ID, FROM_TEXT, TO_TEXT, WEBPAGE_ID, PARSE_VERSION))
    }

    @Test
    fun `Given data entity When mapped to domain entity Then correctly mapped all properties`() {
        val dataEntity = DataEntity(ID, FROM_TEXT, TO_TEXT, WEBPAGE_ID, PARSE_VERSION)

        val mapped = mapper.toDomainEntity(dataEntity)

        assertThat(mapped).isEqualTo(
            DomainEntity(
                FROM_TEXT,
                TO_TEXT,
                UNSET_WEBPAGE_VISIT,
                PARSE_VERSION,
                ID
            )
        )
    }

    companion object {
        const val FROM_TEXT = "FROM"
        const val TO_TEXT = "TO"
        const val ID = 99
        const val WEBPAGE_ID = 9978
        const val URL = "http://wiktionary.org"
        const val PACKAGE_NAME = "com.google.chrome"
        val TIME = LocalDateTime.of(1990, 5, 4, 17, 32, 59)
        const val PARSE_VERSION = 99998

        val WEBPAGE_VISIT = WebpageVisit(URL, PACKAGE_NAME, TIME, WEBPAGE_ID, PARSE_VERSION)
    }
}