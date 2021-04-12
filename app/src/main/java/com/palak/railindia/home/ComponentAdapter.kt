package com.palak.railindia.home

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import com.palak.railindia.R
import com.palak.railindia.databinding.LayoutDataEntryBinding
import com.palak.railindia.model.Component
import com.palak.railindia.model.ComponentEntry

/**
 * An adapter to load CityLocation list.
 */
class ComponentAdapter(var noOfBogie : Int, var onPassSave : (Int, Int) -> Unit, var onFailSave : (Int, Int) -> Unit) :
    ListAdapter<Any, RecyclerView.ViewHolder>(StringDataDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComponentViewHolder {

        return ComponentViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_data_entry, parent, false
            ),noOfBogie, onPassSave, onFailSave)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ComponentViewHolder
        holder.bind(getItem(position) as ComponentEntry)
    }

    class ComponentViewHolder(val binding: LayoutDataEntryBinding, var noOfBogie : Int, var onPassSave : (Int, Int) -> Unit,
                              var onFailSave : (Int, Int) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(componentEntry: ComponentEntry) {
            with(binding) {
                binding.bogieNo = noOfBogie
                binding.component = componentEntry.component
                binding.tilPass.editText?.addTextChangedListener {
                    onPassSave(it.toString().trim().toInt(), adapterPosition)
                }
                binding.tilFail.editText?.addTextChangedListener {
                    onFailSave(it.toString().trim().toInt(), adapterPosition)
                }
                executePendingBindings()
            }
        }
    }
}

private class StringDataDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        if (oldItem is Component && newItem is Component) {
            return oldItem.id == newItem.id
        }
        return false
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        if (oldItem is Component && newItem is Component) {
            return oldItem == newItem
        }
        return false
    }

}