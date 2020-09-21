package com.lenta.bp15.features.good_list

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lenta.bp15.R

class GoodListFragment : Fragment() {

    companion object {
        fun newInstance() = GoodListFragment()
    }

    private lateinit var viewModel: GoodListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.good_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(GoodListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}