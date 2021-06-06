package com.palak.railindia.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.palak.railindia.R
import com.palak.railindia.databinding.LayoutDataEntryBinding
import com.palak.railindia.model.Component
import com.palak.railindia.model.ComponentEntry

/**
 * An adapter to load CityLocation list.
 */
class ComponentAdapter(
    var noOfBogie: Int,
    var isUpdate: Boolean,
    val listSize: Int,
    var onPassSave: (EditText, Int, Int) -> Unit,
    var onFailSave: (EditText, Int, Int) -> Unit
) :
    ListAdapter<Any, RecyclerView.ViewHolder>(StringDataDiffCallback()) {

    override fun getItemCount(): Int {
        return listSize
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComponentViewHolder {

        return ComponentViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.layout_data_entry, parent, false
            ),noOfBogie, isUpdate, listSize, onPassSave, onFailSave)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ComponentViewHolder
        holder.bind(getItem(position) as ComponentEntry)
    }

    class ComponentViewHolder(val binding: LayoutDataEntryBinding, var noOfBogie : Int, val isSearch: Boolean,
                              val listSize : Int, var onPassSave : (EditText, Int, Int) -> Unit,
                              var onFailSave : (EditText, Int, Int) -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(componentEntry: ComponentEntry) {
            with(binding) {
                binding.isUpdate = isSearch
                binding.bogieNo = noOfBogie
                binding.componentEntry = componentEntry
                binding.tilPass.editText?.addTextChangedListener {
                    val valInStr = it.toString().trim()
                    if(valInStr.isNotEmpty()){
                        onPassSave(binding.tilPass.editText!!,valInStr.toInt(), adapterPosition)
                    }
                }
                binding.tilFail.editText?.addTextChangedListener {
                    val valInStr = it.toString().trim()
                    if(valInStr.isNotEmpty()){
                        onFailSave(binding.tilFail.editText!!, valInStr.toInt(), adapterPosition)
                    }

                }
                if(adapterPosition == listSize - 1){
                    //last item.
                    binding.tilFail.editText?.imeOptions = EditorInfo.IME_ACTION_DONE
                }
                else{
                    binding.tilFail.editText?.imeOptions = EditorInfo.IME_ACTION_NEXT
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