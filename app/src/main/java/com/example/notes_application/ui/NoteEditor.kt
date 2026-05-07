package com.example.notes_application.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.notes_application.R
import com.example.notes_application.data.Note
import com.example.notes_application.data.NoteDatabase
import com.example.notes_application.databinding.ActivityNoteEditorBinding
import com.example.notes_application.repository.NoteRepository
import com.example.notes_application.viewmodel.NoteViewModel
import com.example.notes_application.viewmodel.NoteViewModelFactory

class NoteEditor : AppCompatActivity() {
    private lateinit var binding: ActivityNoteEditorBinding
    private lateinit var viewModel: NoteViewModel

    private var currentNote: Note? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModel()

        currentNote = intent.getParcelableExtra("NOTE")
        if(currentNote !=null){
            binding.etTitle.setText(currentNote!!.title)
            binding.etCategory.setText(currentNote!!.category)
            binding.etContent.setText(currentNote!!.content)
        }

    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun setupViewModel() {
        val dao = NoteDatabase.getDatabase(this).noteDao()
        val repo = NoteRepository(dao)
        val factory = NoteViewModelFactory(repo)

        viewModel = ViewModelProvider(this, factory)[NoteViewModel::class.java]
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_note_editor, menu)
        menu.findItem(R.id.action_delete).isVisible = currentNote !=null
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                onBackPressed()
                true
            }
            R.id.action_save -> {
                saveNote()
                true
            }
            R.id.action_delete -> {
                deleteNote()
                true
            }
            else -> super.onOptionsItemSelected(item)

        }
    }

    private fun deleteNote() {
        if(currentNote !=null){
            viewModel.delete(currentNote!!)

        }
        finish()

    }

    private fun saveNote() {
        var title = binding.etTitle.text.toString().trim()
        var category = binding.etCategory.text.toString().trim()
        val content = binding.etContent.text.toString().trim()

        if (title.isEmpty()) {
            title = "New Note"
        }

        if (category.isEmpty()) {
            category = "Default"
        }

        if (content.isEmpty()){
            binding.etContent.error = "Content is required"
            return
        }

        if (currentNote == null){
            val note = Note(title = title, content = content, category = category)
            viewModel.insert(note)
        }else{
            val note = currentNote!!.copy(title = title, content = content, category = category, updatedAt = System.currentTimeMillis())
            viewModel.update(note)
        }

        finish()
    }

}