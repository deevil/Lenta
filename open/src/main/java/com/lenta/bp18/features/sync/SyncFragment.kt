package com.lenta.bp18.features.sync

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.lenta.bp18.R

class SyncFragment : Fragment() {

    companion object {
        fun newInstance() = SyncFragment()
    }

    private lateinit var viewModel: SyncViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sync, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SyncViewModel::class.java)
        // TODO: Use the ViewModel
    }

}