package com.lenta.shared.utilities.databinding


import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.NonNull
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
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
                    realisation = dataBindingRecyclerViewConfig.realisation,
                    onItemClickListener = dataBindingRecyclerViewConfig.onItemClickListener,
                    onItemDoubleClickListener = dataBindingRecyclerViewConfig.onItemDoubleClickListener,
                    onItemLongClickListener = dataBindingRecyclerViewConfig.onItemLongClickListener
            )

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
        val realisation: DataBindingAdapter<BindingType>? = null,
        val onItemClickListener: AdapterView.OnItemClickListener? = null,
        val onItemLongClickListener: AdapterView.OnItemLongClickListener? = null,
        val onItemDoubleClickListener: AdapterView.OnItemClickListener? = null
)


class DataBindingRecyclerAdapter<ItemType, BindingType : ViewDataBinding>(
        private val items: List<ItemType>?,
        private val layoutId: Int,
        private val itemId: Int,
        private val realisation: DataBindingAdapter<BindingType>? = null,
        private val onItemClickListener: AdapterView.OnItemClickListener? = null,
        private val onItemLongClickListener: AdapterView.OnItemLongClickListener? = null,
        private val onItemDoubleClickListener: AdapterView.OnItemClickListener? = null
) :
        RecyclerView.Adapter<DataBindingRecyclerAdapter.BindingViewHolder>(), DataBindingAdapter<BindingType> {


    override fun getItemCount(): Int {
        return items?.size ?: 0
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {
        val binding = DataBindingUtil.inflate<BindingType>(LayoutInflater.from(parent.context), layoutId, parent, false)
        onCreate(binding)
        val result = BindingViewHolder(binding)
        if (onItemClickListener != null || onItemDoubleClickListener != null) {
            binding.root.setOnClickListener(object : DoubleClickListener() {
                override fun onSingleClick(view: View) {
                    onItemClickListener?.onItemClick(null, view, result.adapterPosition, view.id.toLong())
                }

                override fun onDoubleClick(view: View) {
                    onItemDoubleClickListener?.onItemClick(null, view, result.adapterPosition, view.id.toLong())
                }
            })
        }

        onItemLongClickListener?.let {
            binding.root.setOnLongClickListener { view ->
                it.onItemLongClick(null, view, result.adapterPosition, view.id.toLong())
                false
            }
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
                                private val items: LiveData<List<T>>,
                                lifecycleOwner: LifecycleOwner,
                                initPosInfo: PosInfo? = null) {

    val posInfo = MutableLiveData(initPosInfo?.copy(isManualClick = false) ?: PosInfo(0, -1))

    init {
        posInfo.observe(lifecycleOwner, Observer { info ->
            Logg.d { "new pos: $info" }
            //rv.adapter?.notifyItemChanged(info.lastPos)
            //rv.adapter?.notifyItemChanged(info.currentPos)
            rv.adapter?.notifyDataSetChanged()
            if (!info.isManualClick && info.currentPos > -1 && info.currentPos < items.value?.size ?: 0) {
                rv.scrollToPosition(info.currentPos)
            }

        })
        items.observe(lifecycleOwner, Observer {
            resendPositions()
        })
    }

    fun onKeyDown(keyCode: KeyCode): Boolean {

        if (!rv.isFocused) {
            return false
        }

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

        rv.requestFocus()

        return true
    }

    fun selectPosition(position: Int) {
        posInfo.value = PosInfo(currentPos = position, lastPos = posInfo.value!!.currentPos, isManualClick = true)
    }

    fun isSelected(pos: Int): Boolean {
        return pos == posInfo.value!!.currentPos
    }

    fun resendPositions() {
        posInfo.value = posInfo.value
    }


}

abstract class DoubleClickListener : View.OnClickListener {

    private var lastClickTime: Long = 0

    override fun onClick(v: View) {
        val clickTime = System.currentTimeMillis()

        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
            lastClickTime = 0
        } else {
            onSingleClick(v)
        }

        lastClickTime = clickTime
    }

    abstract fun onSingleClick(view: View)

    abstract fun onDoubleClick(view: View)

    companion object {

        private val DOUBLE_CLICK_TIME_DELTA = ViewConfiguration.getDoubleTapTimeout().toLong()
    }
}

data class PosInfo(val currentPos: Int, val lastPos: Int, val isManualClick: Boolean = false)