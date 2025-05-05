package com.example.contactdedoppelganger.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contactdedoppelganger.databinding.ItemContactBinding
import com.example.contactdedoppelganger.domain.model.ContactDomain

/**
 * Адаптер для rvContacts
 */
class ContactAdapter : ListAdapter<ContactDomain, ContactAdapter.ContactViewHolder>(DiffCallback) {

    inner class ContactViewHolder(
        private val binding: ItemContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: ContactDomain) {
            binding.tvName.text = contact.name
            binding.tvPhone.text = contact.phoneNumbers.firstOrNull().orEmpty()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ContactDomain>() {
        override fun areItemsTheSame(oldItem: ContactDomain, newItem: ContactDomain): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ContactDomain, newItem: ContactDomain): Boolean {
            return oldItem == newItem
        }
    }

    fun update(newItems: List<ContactDomain>) {
        submitList(newItems)
    }
}
