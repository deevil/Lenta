package com.lenta.shared.utilities.databinding


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.NonNull
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.lenta.shared.utilities.Logg


@BindingAdapter(value = ["items", "rv_config"])
fun <ItemType, BindingType : ViewDataBinding> setupRecyclerView(recyclerView: RecyclerView,
                                                                newItems: List<ItemType>?,
                                                                dataBindingRecyclerViewConfig: DataBindingRecyclerViewConfig) {

    Logg.d { "newItems: ${newItems}" }


    var oldItems: MutableList<ItemType>? = null

    recyclerView.tag?.let {
        oldItems = it as MutableList<ItemType>

    }

    if (oldItems == null) {
        oldItems = mutableListOf()
        recyclerView.setTag(oldItems)
    }

    if (oldItems !== newItems) {
        oldItems?.let { old ->
            old.clear()
            newItems?.let {
                old.addAll(newItems)
            }
        }

    }


    if (recyclerView.adapter == null) {

        val mLayoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.layoutManager = mLayoutManager

        dataBindingRecyclerViewConfig.let {
            recyclerView.adapter = DataBindingRecyclerAdapter<ItemType, BindingType>(oldItems,
                    it.layoutId, it.itemId)
        }


    } else {
        recyclerView.adapter?.notifyDataSetChanged()
    }

}

data class DataBindingRecyclerViewConfig (
        val layoutId: Int,
        val itemId: Int
)


class DataBindingRecyclerAdapter<ItemType, BindingType : ViewDataBinding>(
        private val items: List<ItemType>?,
        private val layoutId: Int,
        private val itemId: Int,
        private val realisation: DataBindingAdapter<BindingType>? = null,
        private val onItemClickListener: AdapterView.OnItemClickListener? = null,
        private val onItemLongClickListener: AdapterView.OnItemLongClickListener? = null
) :
        RecyclerView.Adapter<DataBindingRecyclerAdapter.BindingViewHolder>(), DataBindingAdapter<BindingType> {


    override fun getItemCount(): Int {
        return items?.size ?: 0
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = DataBindingUtil.inflate<BindingType>(LayoutInflater.from(parent.context), layoutId, parent, false)
        onCreate(binding)
        val result = BindingViewHolder(binding)
        binding.root.setOnClickListener { view ->
            onItemClickListener?.onItemClick(null, view, result.adapterPosition, view.id.toLong())
        }
        binding.root.setOnLongClickListener { view ->
            onItemLongClickListener?.onItemLongClick(null, view, result.adapterPosition, view.id.toLong())
            false
        }
        return result
    }

    override fun onBindViewHolder(@NonNull holder: BindingViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (itemId != -1) {
            holder.binding.setVariable(itemId, items!![holder.adapterPosition])
        }
        onBind(holder.binding as BindingType, holder.adapterPosition)
        holder.binding.executePendingBindings()
    }

    override fun onCreate(binding: BindingType) {
        realisation?.onCreate(binding)
    }

    override fun onBind(binding: BindingType, position: Int) {
        realisation?.onBind(binding, position)
    }

    class BindingViewHolder(val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)
}

interface DataBindingAdapter<BindingType : ViewDataBinding> {
    fun onCreate(binding: BindingType)
    fun onBind(binding: BindingType, position: Int)
}


interface Evenable {
    fun isEven(): Boolean
}