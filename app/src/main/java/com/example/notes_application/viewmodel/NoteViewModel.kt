package com.example.notes_application.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.switchMap
import com.example.notes_application.data.Note
import com.example.notes_application.repository.NoteRepository
import kotlinx.coroutines.launch

class NoteViewModel(private val respository: NoteRepository) : ViewModel() {

    private val _searchQuery = MutableLiveData<String>("")
    private val _selectedCategories = MutableLiveData<List<String>>(null)

    val allCategories: LiveData<List<String>> = respository.getAllCategories()
    
    // Trigger LiveData that combines search query and selected categories
    private val filterTrigger = MediatorLiveData<Pair<String, List<String>?>>().apply {
        addSource(_searchQuery) { query -> value = Pair(query, _selectedCategories.value) }
        addSource(_selectedCategories) { categories -> value = Pair(_searchQuery.value ?: "", categories) }
    }

    val notes: LiveData<List<Note>> = filterTrigger.switchMap { (query, categories) ->
        if (categories == null || categories.isEmpty()) {
            if (query.isEmpty()) {
                respository.allNotes
            } else {
                // Fallback to simple search if no categories selected
                respository.searchNotes(query)
            }
        } else {
            respository.searchNotesWithFilters(query, categories)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategories(categories: List<String>) {
        _selectedCategories.value = categories
    }

    val allNotes: LiveData<List<Note>> = respository.allNotes

    fun insert(note: Note) = viewModelScope.launch {
        respository.insert(note)
    }

    fun update(note: Note) = viewModelScope.launch {
        respository.update(note)
    }

    fun delete(note: Note) = viewModelScope.launch {
        respository.delete(note)
    }

}

class NoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(NoteViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}