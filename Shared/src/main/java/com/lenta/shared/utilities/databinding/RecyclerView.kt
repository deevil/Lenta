package com.lenta.shared.utilities.databinding


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.NonNull
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.lenta.shared.keys.KeyCode
import com.lenta.shared.utilities.Logg


@BindingAdapter(value = ["items", "rv_config"])
fun <ItemType, BindingType : ViewDataBinding> setupRecyclerView(recyclerView: RecyclerView,
                                                                newItems: List<ItemType>?,
                                                                dataBindingRecyclerViewConfig: DataBindingRecyclerViewConfig<BindingType>?) {

    Logg.d { "newItems: ${newItems}" }

    if (dataBindingRecyclerViewConfig == null) {
        return
    }

    var oldItems: MutableList<ItemType>? = null

    recyclerView.tag?.let {
        @Suppress("UNCHECKED_CAST")
        oldItems = it as MutableList<ItemType>

    }

    if (oldItems == null) {
        oldItems = mutableListOf()
        recyclerView.tag = oldItems
    }

    if (recyclerView.adapter == null) {

        val mLayoutManager: RecyclerView.LayoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = null

        dataBindingRecyclerViewConfig.let {
            recyclerView.adapter = DataBindingRecyclerAdapter(
                    items = oldItems,
                    layoutId = it.layoutId,
                    itemId = it.itemId,
                    realisation = dataBindingRecyclerViewConfig.realisation)
        }


    }

    if (oldItems !== newItems) {
        oldItems?.let { old ->
            old.clear()
            newItems?.let {
                old.addAll(newItems)
            }
        }

    }
    recyclerView.adapter?.notifyDataSetChanged()

}

data class DataBindingRecyclerViewConfig<BindingType : ViewDataBinding>(
        val layoutId: Int,
        val itemId: Int,
        val realisation: DataBindingAdapter<BindingType>? = null
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
        @Suppress("UNCHECKED_CAST")
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

class RecyclerViewKeyHandler<T>(private val rv: RecyclerView,
                                private val items: MutableLiveData<List<T>>,
                                lifecycleOwner: LifecycleOwner) {

    val posInfo = MutableLiveData(PosInfo(-1, -1))

    init {
        posInfo.observe(lifecycleOwner, Observer { info ->
            Logg.d { "new pos: $info" }
            info.currentPos.let { currentPos ->
                rv.adapter?.notifyItemChanged(info.lastPos)
                rv.adapter?.notifyItemChanged(info.currentPos)
                if (currentPos > -1 && currentPos < items.value?.size ?: 0) {
                    rv.scrollToPosition(info.currentPos)
                }
            }

        })
        items.observe(lifecycleOwner, Observer {
            clearPositions()
        })
    }

    fun onKeyDown(keyCode: KeyCode): Boolean {

        var pos = posInfo.value!!.currentPos

        when (keyCode) {
            KeyCode.KEYCODE_DPAD_DOWN -> pos++
            KeyCode.KEYCODE_DPAD_UP -> pos--
            else -> return false
        }
        if (pos < -1) {
            return false
        }
        val itemsSize = items.value?.size ?: 0
        if (pos > itemsSize) {
            return false
        }

        posInfo.value = PosInfo(currentPos = pos, lastPos = posInfo.value!!.currentPos)

        return true
    }

    fun isSelected(pos: Int): Boolean {
        return pos == posInfo.value!!.currentPos
    }

    fun clearPositions() {
        posInfo.value = PosInfo(-1, -1)
    }


}

data class PosInfo(val currentPos: Int, val lastPos: Int)