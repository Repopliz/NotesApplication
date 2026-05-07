package com.example.notes_application.repository

import androidx.lifecycle.LiveData
import com.example.notes_application.data.Note

import com.example.notes_application.data.NoteDao

class NoteRepository(private val noteDao: NoteDao) {

    val allNotes: LiveData<List<Note>> = noteDao.getAllNotes()

    suspend fun insert(note: Note) {
        noteDao.insert(note)
    }

    suspend fun update(note: Note) {
        noteDao.update(note)
    }

    suspend fun delete(note: Note) {
        noteDao.delete(note)
    }

    fun searchNotes(query: String): LiveData<List<Note>> {
        return noteDao.searchNotesWithFilters("%$query%", listOf()) // Note: This will be updated in VM
    }

    fun searchNotesWithFilters(query: String, categories: List<String>): LiveData<List<Note>> {
        return noteDao.searchNotesWithFilters("%$query%", categories)
    }

    fun getAllCategories(): LiveData<List<String>> {
        return noteDao.getAllCategories()
    }
}