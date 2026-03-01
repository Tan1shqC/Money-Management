package com.moneyapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface FundDao {
    @Insert
    suspend fun insert(fund: Fund): Long

    @Query("SELECT * FROM funds WHERE id = :id")
    suspend fun getById(id: String): Fund?

    @Query("SELECT * FROM funds WHERE isArchived = 0 ORDER BY name")
    suspend fun getActive(): List<Fund>

    @Update
    suspend fun update(fund: Fund)

    @Delete
    suspend fun delete(fund: Fund)
}
