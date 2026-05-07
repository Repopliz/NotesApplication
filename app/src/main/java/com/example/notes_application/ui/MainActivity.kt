package com.example.notes_application.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notes_application.R
import com.example.notes_application.data.NoteDatabase
import com.example.notes_application.databinding.ActivityMainBinding
import com.example.notes_application.repository.NoteRepository
import androidx.lifecycle.Observer
import androidx.lifecycle.LiveData
import com.example.notes_application.ui.adapter.NoteAdapter
import com.example.notes_application.viewmodel.NoteViewModel
import com.example.notes_application.viewmodel.NoteViewModelFactory
import androidx.core.widget.addTextChangedListener

import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: NoteViewModel
    private var allCategories = listOf<String>()
    private var selectedCategories = mutableListOf<String>()

    private var adapter = NoteAdapter{note ->
        val intent = Intent(this, NoteEditor::class.java)
        intent.putExtra("NOTE", note)
        startActivity(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        observeData()
        setupUI()

        binding.fabAddNote.setOnClickListener {
            startActivity(Intent(this, NoteEditor::class.java))
        }
    }
    private fun setupViewModel() {
        val dao = NoteDatabase.getDatabase(this).noteDao()
        val repo = NoteRepository(dao)
        val factory = NoteViewModelFactory(repo)

        viewModel = ViewModelProvider(this, factory)[NoteViewModel::class.java]
    }

    private fun setupRecyclerView() {
        binding.rvNotes.layoutManager = LinearLayoutManager(this)
        binding.rvNotes.adapter = adapter
    }

    private fun observeData() {
        viewModel.notes.observe(this, Observer { notes ->
            adapter.setNotes(notes)
            binding.tvEmpty.visibility = if (notes.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        })

        viewModel.allCategories.observe(this, Observer { categories ->
            val rawOldCategories = allCategories.map { if (it == "Uncategorized") "" else it }
            allCategories = categories.map { if (it.isEmpty()) "Uncategorized" else it }
            
            var changed = false
            for (cat in categories) {
                if (!rawOldCategories.contains(cat)) {
                    // This is a brand new category! Always add it to active selection
                    if (!selectedCategories.contains(cat)) {
                        selectedCategories.add(cat)
                        changed = true
                    }
                }
            }

            // Handle first-time initialization
            if (selectedCategories.isEmpty() && categories.isNotEmpty()) {
                selectedCategories.addAll(categories)
                changed = true
            }

            if (changed) {
                viewModel.setSelectedCategories(selectedCategories.toList())
            }
        })
    }

    private fun setupUI() {
        binding.etSearch.addTextChangedListener{ text ->
            viewModel.setSearchQuery(text.toString())
        }

        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val categoryArray = allCategories.toTypedArray()
        val checkedItems = BooleanArray(categoryArray.size) { index ->
            val cat = if (categoryArray[index] == "Uncategorized") "" else categoryArray[index]
            selectedCategories.contains(cat)
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Filter by Category")
            .setMultiChoiceItems(categoryArray, checkedItems) { _, which, isChecked ->
                val cat = if (categoryArray[which] == "Uncategorized") "" else categoryArray[which]
                if (isChecked) {
                    if (!selectedCategories.contains(cat)) selectedCategories.add(cat)
                } else {
                    selectedCategories.remove(cat)
                }
            }
            .setPositiveButton("Apply") { _, _ ->
                viewModel.setSelectedCategories(selectedCategories.toList())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}