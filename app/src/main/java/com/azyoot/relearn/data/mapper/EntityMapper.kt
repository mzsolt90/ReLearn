package com.azyoot.relearn.data.mapper

interface EntityMapper<DOMAIN, DATA>{
    fun toDataEntity(domainEntity: DOMAIN): DATA
    fun toDomainEntity(dataEntity: DATA): DOMAIN
}