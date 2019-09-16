package com.lenta.bp9.features.revise

import com.lenta.bp9.R
import com.lenta.bp9.databinding.FragmentTaskReviseBinding
import com.lenta.bp9.platform.extentions.getAppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.platform.toolbar.bottom_toolbar.BottomToolbarUiModel
import com.lenta.shared.platform.toolbar.top_toolbar.TopToolbarUiModel
import com.lenta.shared.utilities.extentions.provideViewModel
import android.os.Bundle
import android.view.LayoutInflater
import com.lenta.shared.utilities.databinding.ViewPagerSettings
import android.view.ViewGroup
import android.view.View
import androidx.databinding.DataBindingUtil
import com.lenta.bp9.BR
import com.lenta.bp9.databinding.ItemTileNotificationsBinding
import com.lenta.bp9.databinding.LayoutTaskCardNotificationsBinding
import com.lenta.bp9.databinding.LayoutTaskReviseInformationBinding
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo
import com.lenta.shared.utilities.databinding.DataBindingAdapter
import com.lenta.shared.utilities.databinding.DataBindingRecyclerViewConfig
import com.lenta.shared.utilities.databinding.RecyclerViewKeyHandler
import com.lenta.shared.utilities.extentions.connectLiveData
import com.lenta.shared.utilities.extentions.generateScreenNumber

class TaskReviseFragment : CoreFragment<FragmentTaskReviseBinding, TaskReviseViewModel>(), ViewPagerSettings {

    var notificationsRecyclerViewKeyHandler: RecyclerViewKeyHandler<*>? = null

    override fun getLayoutId(): Int = R.layout.fragment_task_revise

    override fun getPageNumber() = generateScreenNumber()

    override fun getViewModel(): TaskReviseViewModel {
        provideViewModel(TaskReviseViewModel::class.java).let {
            getAppComponent()?.inject(it)
            return it
        }
    }

    override fun setupTopToolBar(topToolbarUiModel: TopToolbarUiModel) {
        topToolbarUiModel.title.value = vm.taskCaption
        topToolbarUiModel.description.value = getString(R.string.delivery_documents)
    }

    override fun setupBottomToolBar(bottomToolbarUiModel: BottomToolbarUiModel) {
        bottomToolbarUiModel.uiModelButton1.show(ButtonDecorationInfo.back)
        bottomToolbarUiModel.uiModelButton5.show(ButtonDecorationInfo.save)
    }

    override fun getPagerItemView(container: ViewGroup, position: Int): View {
        return when(position) {
            0 -> prepareToCheckView(container)
            1 -> prepareCheckedView(container)
            2 -> prepareNotificationsView(container)
            else -> View(context)
        }
    }

    override fun getTextTitle(position: Int): String {
        return when (position) {
            0 -> getString(R.string.to_check)
            1 -> getString(R.string.checked)
            2 -> getString(R.string.information)
            else -> ""
        }
    }

    fun prepareNotificationsView(container: ViewGroup): View {
        DataBindingUtil
                .inflate<LayoutTaskReviseInformationBinding>(LayoutInflater.from(container.context),
                        R.layout.layout_task_revise_information,
                        container,
                        false).let { layoutBinding ->

                    layoutBinding.rvConfig = DataBindingRecyclerViewConfig(
                            layoutId = R.layout.item_tile_notifications,
                            itemId = BR.vm,
                            realisation = object : DataBindingAdapter<ItemTileNotificationsBinding> {
                                override fun onCreate(binding: ItemTileNotificationsBinding) {
                                }

                                override fun onBind(binding: ItemTileNotificationsBinding, position: Int) {
                                    binding.tvCounter.tag = position
                                }
                            }
                    )

                    layoutBinding.vm = vm
                    layoutBinding.lifecycleOwner = viewLifecycleOwner
                    val rvKeyHandler = RecyclerViewKeyHandler(
                            rv = layoutBinding.rv,
                            items = vm.notifications,
                            lifecycleOwner = layoutBinding.lifecycleOwner!!
                    )
                    notificationsRecyclerViewKeyHandler = rvKeyHandler
                    return layoutBinding.root
                }
    }

    fun prepareCheckedView(container: ViewGroup): View {
        return View(context)
    }

    fun prepareToCheckView(container: ViewGroup): View {
        return View(context)
    }

    override fun countTab(): Int {
        return 3
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.viewPagerSettings = this
    }


}
