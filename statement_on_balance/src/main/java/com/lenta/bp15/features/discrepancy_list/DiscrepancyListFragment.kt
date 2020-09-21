package com.lenta.bp15.features.discrepancy_list

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lenta.bp15.R

class DiscrepancyListFragment : Fragment() {

    companion object {
        fun newInstance() = DiscrepancyListFragment()
    }

    private lateinit var viewModel: DiscrepancyListViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_discrepancy_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DiscrepancyListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}