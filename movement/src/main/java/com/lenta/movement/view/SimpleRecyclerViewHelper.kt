package com.lenta.movement.view

import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.lenta.movement.BR
import com.lenta.movement.R
import com.lenta.movement.databinding.LayoutItemSimpleBinding
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler

fun simpleListRecyclerViewConfig(
    recyclerView: RecyclerView?,
    selectionItemsHelper: SelectionItemsHelper,
    recyclerViewKeyHandler: RecyclerViewKeyHandler<*>?,
    onClickItem: (Int) -> Unit = {}
): DataBindingRecyclerViewConfig<LayoutItemSimpleBinding> {
    val onClickSelectionListener = View.OnClickListener {
        (it!!.tag as Int).let { position ->
            selectionItemsHelper.revert(position = position)
            recyclerView?.adapter?.notifyItemChanged(position)
        }
    }

    return DataBindingRecyclerViewConfig(
        layoutId = R.layout.layout_item_simple,
        itemId = BR.item,
        realisation = object : DataBindingAdapter<LayoutItemSimpleBinding> {
            override fun onCreate(binding: LayoutItemSimpleBinding) {
                // do nothing
            }

            override fun onBind(binding: LayoutItemSimpleBinding, position: Int) {
                binding.tvCounter.tag = position
                binding.tvCounter.setOnClickListener(onClickSelectionListener)
                binding.selectedForDelete = selectionItemsHelper.isSelected(position)
                recyclerViewKeyHandler?.let {
                    binding.root.isSelected = it.isSelected(position)
                }
            }
        },
        onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            recyclerViewKeyHandler?.let {
                if (it.isSelected(position)) {
                    onClickItem(position)
                } else {
                    it.selectPosition(position)
                }
            }
        }
    )
}