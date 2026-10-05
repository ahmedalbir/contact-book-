package com.example.contactbook

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.contactbook.databinding.ItemContactBinding

class ContactAdapter(
    private val onEdit: (Contact) -> Unit,
    private val onDelete: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    private val items = mutableListOf<Contact>()

    fun submitList(newItems: List<Contact>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class ContactViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: Contact) {
            binding.nameText.text = contact.name
            binding.avatarText.text = initialsOf(contact.name)

            val sub = if (contact.email.isNotBlank()) {
                "${contact.phone} · ${contact.email}"
            } else {
                contact.phone
            }
            binding.subText.text = sub

            if (contact.note.isNotBlank()) {
                binding.noteText.visibility = android.view.View.VISIBLE
                binding.noteText.text = contact.note
            } else {
                binding.noteText.visibility = android.view.View.GONE
            }

            binding.editBtn.setOnClickListener { onEdit(contact) }
            binding.deleteBtn.setOnClickListener { onDelete(contact) }
        }

        private fun initialsOf(name: String): String {
            val parts = name.trim().split(Regex("\\s+"))
            val first = parts.getOrNull(0)?.firstOrNull()?.uppercaseChar() ?: ' '
            val second = parts.getOrNull(1)?.firstOrNull()?.uppercaseChar() ?: ' '
            return "$first$second".trim()
        }
    }
}
