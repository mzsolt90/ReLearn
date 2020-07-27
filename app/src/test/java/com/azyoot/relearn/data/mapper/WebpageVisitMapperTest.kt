package com.azyoot.relearn.data.mapper

import com.azyoot.relearn.util.DateTimeMapper
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset

import com.azyoot.relearn.data.entity.WebpageVisit as DataEntity
import com.azyoot.relearn.domain.entity.WebpageVisit as DomainEntity

class WebpageVisitMapperTest {

    private val mockDateTimeMapper: DateTimeMapper = mock()
    private val mapper = WebpageVisitMapper(mockDateTimeMapper)

    @Before
    fun setup() {
        whenever(mockDateTimeMapper.mapToLocalDateTime(TIMESTAMP)).thenReturn(TIME)
        whenever(mockDateTimeMapper.mapToTimestamp(TIME)).thenReturn(TIMESTAMP)
    }

    @Test
    fun `Given domain entity When mapped to data entity Then correctly mapped all properties`() {
        val domainEntity = DomainEntity(URL, PACKAGE_NAME, TIME, ID, PARSE_VERSION)

        val mapped = mapper.toDataEntity(domainEntity)

        assertThat(mapped).isEqualTo(DataEntity(ID, URL, PACKAGE_NAME, TIMESTAMP, PARSE_VERSION))
    }

    @Test
    fun `Given data entity When mapped to domain entity Then correctly mapped all properties`() {
        val dataEntity = DataEntity(ID, URL, PACKAGE_NAME, TIMESTAMP, PARSE_VERSION)

        val mapped = mapper.toDomainEntity(dataEntity)

        assertThat(mapped).isEqualTo(DomainEntity(URL, PACKAGE_NAME, TIME, ID, PARSE_VERSION))
    }

    companion object {
        const val ID = 99
        const val URL = "http://wiktionary.org"
        const val PACKAGE_NAME = "com.google.chrome"
        val TIME = LocalDateTime.of(1990, 5, 4, 17, 32, 59)
        val TIMESTAMP = TIME.toInstant(ZoneOffset.UTC).toEpochMilli()
        const val PARSE_VERSION = 99998
    }
}