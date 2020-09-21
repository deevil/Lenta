package com.lenta.bp15.features.good_info

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lenta.bp15.R

class GoodInfoFragment : Fragment() {

    companion object {
        fun newInstance() = GoodInfoFragment()
    }

    private lateinit var viewModel: GoodInfoViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_good_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GoodInfoViewModel::class.java)
        // TODO: Use the ViewModel
    }

}