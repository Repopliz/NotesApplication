package com.example.notes_application.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM notes WHERE (title LIKE :query OR content LIKE :query OR category LIKE :query) AND (category IN (:categories) OR (category = '' AND '' IN (:categories))) ORDER BY updatedAt DESC")
    fun searchNotesWithFilters(query: String, categories: List<String>): LiveData<List<Note>>

    @Query("SELECT DISTINCT category FROM notes")
    fun getAllCategories(): LiveData<List<String>>

}