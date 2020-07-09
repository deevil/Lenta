package com.lenta.bp18.features.goods_info

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lenta.bp18.R

class GoodsInfoFragment : Fragment() {

    companion object {
        fun newInstance() = GoodsInfoFragment()
    }

    private lateinit var viewModel: GoodsInfoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_goods_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GoodsInfoViewModel::class.java)
        // TODO: Use the ViewModel
    }

}