package com.lenta.shared.utilities.databinding


import android.annotation.SuppressLint
import android.os.Build
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
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            recyclerView.touchscreenBlocksFocus = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            recyclerView.defaultFocusHighlightEnabled = false
        }


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
) : RecyclerView.Adapter<DataBindingRecyclerAdapter.BindingViewHolder>(), DataBindingAdapter<BindingType> {


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
                    onItemClickListener?.onItemClick(null, view, result.absoluteAdapterPosition, view.id.toLong())
                }

                override fun onDoubleClick(view: View) {
                    onItemDoubleClickListener?.onItemClick(null, view, result.absoluteAdapterPosition, view.id.toLong())
                }
            })
        }

        onItemLongClickListener?.let {
            binding.root.setOnLongClickListener { view ->
                it.onItemLongClick(null, view, result.absoluteAdapterPosition, view.id.toLong())
                false
            }
        }

        return result
    }

    override fun onBindViewHolder(@NonNull holder: BindingViewHolder, @SuppressLint("RecyclerView") position: Int) {
        if (itemId != -1) {
            holder.binding.setVariable(itemId, items!![holder.absoluteAdapterPosition])
        }
        @Suppress("UNCHECKED_CAST")
        onBind(holder.binding as BindingType, holder.absoluteAdapterPosition)
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

class RecyclerViewKeyHandler<T>(
        private var rv: RecyclerView?,
        private var items: LiveData<List<T>>?,
        lifecycleOwner: LifecycleOwner?,
        initPosInfo: PosInfo? = null,
        var onClickPositionFunc: ((Int) -> Unit)? = null
) {

    val posInfo = MutableLiveData(initPosInfo?.copy(isManualClick = false) ?: PosInfo(0, -1))

    init {
        rv?.let { recyclerView ->
            lifecycleOwner?.let { lco ->
                posInfo.observe(lco) { info ->
                    Logg.d { "new pos: $info" }
                    recyclerView.adapter?.notifyDataSetChanged()

                    if (!info.isManualClick && isCorrectPosition(info.currentPos)) {
                        recyclerView.post { recyclerView.scrollToPosition(info.currentPos) }
                    }
                }

                items?.observe(lco) {
                    resendPositions()
                }
            }
        }
    }

    fun onKeyDown(keyCode: KeyCode): Boolean {
        if (rv?.isFocused == false) {
            return false
        }

        when (keyCode) {
            KeyCode.KEYCODE_DPAD_DOWN, KeyCode.KEYCODE_DPAD_UP -> {
                val lastPos = posInfo.value?.currentPos
                        ?: throw IllegalStateException("currentPos cannot be null")
                var currentPos = lastPos

                when (keyCode) {
                    KeyCode.KEYCODE_DPAD_DOWN -> currentPos++
                    KeyCode.KEYCODE_DPAD_UP -> currentPos--
                    else -> return false
                }

                if (isCorrectPosition(currentPos)) {
                    posInfo.value = PosInfo(currentPos = currentPos, lastPos = lastPos)
                    rv?.requestFocus()
                    return true
                }
            }
            KeyCode.KEYCODE_ENTER -> {
                onClickPositionFunc?.let {
                    posInfo.value?.currentPos?.let { position ->
                        if (isCorrectPosition(position)) {
                            it.invoke(position)
                            return true
                        }
                    }
                }
            }
            else -> return false
        }

        return false
    }

    private fun isCorrectPosition(position: Int): Boolean {
        return position > -1 && position < items?.value?.size ?: 0
    }

    fun selectPosition(position: Int) {
        posInfo.value = PosInfo(currentPos = position, lastPos = posInfo.value!!.currentPos, isManualClick = true)
        rv?.requestFocus()
    }

    fun isSelected(pos: Int): Boolean {
        return pos == posInfo.value!!.currentPos
    }

    fun resendPositions() {
        posInfo.value = posInfo.value
    }

    fun onItemClicked(position: Int) {
        if (this.isSelected(position)) {
            onClickPositionFunc?.invoke(position)
        } else {
            this.selectPosition(position)
        }
    }

    fun clear() {
        rv = null
        items = null
        onClickPositionFunc = null
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