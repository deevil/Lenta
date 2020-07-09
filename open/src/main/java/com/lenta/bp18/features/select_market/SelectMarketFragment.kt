package com.lenta.bp18.features.select_market

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lenta.bp18.R

class SelectMarketFragment : Fragment() {

    companion object {
        fun newInstance() = SelectMarketFragment()
    }

    private lateinit var viewModel: SelectMarketViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_market, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SelectMarketViewModel::class.java)
        // TODO: Use the ViewModel
    }

}