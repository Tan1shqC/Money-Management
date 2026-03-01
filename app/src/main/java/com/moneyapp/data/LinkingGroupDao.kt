package com.moneyapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface LinkingGroupDao {
    @Insert
    suspend fun insert(group: LinkingGroup): Long

    @Query("SELECT * FROM linking_groups WHERE id = :id")
    suspend fun getById(id: String): LinkingGroup?

    @Query("SELECT * FROM linking_groups ORDER BY linkedAt DESC")
    suspend fun getAll(): List<LinkingGroup>

    @Update
    suspend fun update(group: LinkingGroup)

    @Delete
    suspend fun delete(group: LinkingGroup)
}
