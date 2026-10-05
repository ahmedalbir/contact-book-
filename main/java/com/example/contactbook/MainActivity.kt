package com.example.contactbook

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.contactbook.databinding.ActivityMainBinding
import com.example.contactbook.databinding.DialogAddEditContactBinding
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ContactAdapter

    private val contacts = mutableListOf<Contact>()
    private var nextId = 1L
    private val prefsName = "contact_book_prefs"
    private val prefsKey = "contacts_json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = ContactAdapter(
            onEdit = { contact -> showAddEditDialog(contact) },
            onDelete = { contact -> confirmDelete(contact) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadContacts()
        if (contacts.isEmpty()) {
            seedSampleData()
        }
        refreshList()

        binding.fabAdd.setOnClickListener { showAddEditDialog(null) }

        binding.searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                refreshList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun seedSampleData() {
        contacts.add(Contact(nextId++, "Rahim Uddin", "01712345678", "rahim@example.com", "Office colleague"))
        contacts.add(Contact(nextId++, "Karim Hossain", "01898765432", "", "Cousin"))
        contacts.add(Contact(nextId++, "Ayesha Akter", "01655551234", "ayesha@example.com", ""))
        saveContacts()
    }

    private fun refreshList() {
        val query = binding.searchBox.text.toString().trim().lowercase()
        val filtered = contacts.filter {
            it.name.lowercase().contains(query) || it.phone.contains(query)
        }.sortedBy { it.name.lowercase() }

        adapter.submitList(filtered)
        binding.emptyText.visibility = if (filtered.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun showAddEditDialog(existing: Contact?) {
        val dialogBinding = DialogAddEditContactBinding.inflate(layoutInflater)
        if (existing != null) {
            dialogBinding.inputName.setText(existing.name)
            dialogBinding.inputPhone.setText(existing.phone)
            dialogBinding.inputEmail.setText(existing.email)
            dialogBinding.inputNote.setText(existing.note)
        }

        val title = if (existing == null) "Notun contact" else "Contact edit korun"

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogBinding.root)
            .setPositiveButton("Save") { _, _ ->
                val name = dialogBinding.inputName.text.toString().trim()
                val phone = dialogBinding.inputPhone.text.toString().trim()
                val email = dialogBinding.inputEmail.text.toString().trim()
                val note = dialogBinding.inputNote.text.toString().trim()

                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Naam ar phone number must dite hobe", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (existing == null) {
                    contacts.add(Contact(nextId++, name, phone, email, note))
                } else {
                    existing.name = name
                    existing.phone = phone
                    existing.email = email
                    existing.note = note
                }
                saveContacts()
                refreshList()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDelete(contact: Contact) {
        AlertDialog.Builder(this)
            .setTitle("Delete contact")
            .setMessage("\"${contact.name}\" delete korte chan?")
            .setPositiveButton("Delete") { _, _ ->
                contacts.remove(contact)
                saveContacts()
                refreshList()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveContacts() {
        val arr = JSONArray()
        for (c in contacts) {
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("name", c.name)
            obj.put("phone", c.phone)
            obj.put("email", c.email)
            obj.put("note", c.note)
            arr.put(obj)
        }
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        prefs.edit().putString(prefsKey, arr.toString()).putLong("next_id", nextId).apply()
    }

    private fun loadContacts() {
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val json = prefs.getString(prefsKey, null) ?: return
        nextId = prefs.getLong("next_id", 1L)
        val arr = JSONArray(json)
        contacts.clear()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            contacts.add(
                Contact(
                    id = obj.getLong("id"),
                    name = obj.getString("name"),
                    phone = obj.getString("phone"),
                    email = obj.optString("email", ""),
                    note = obj.optString("note", "")
                )
            )
        }
    }
}
