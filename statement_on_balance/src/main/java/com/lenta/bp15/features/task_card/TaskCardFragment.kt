package com.lenta.bp15.features.task_card

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.lenta.bp15.R

class TaskCardFragment : Fragment() {

    companion object {
        fun newInstance() = TaskCardFragment()
    }

    private lateinit var viewModel: TaskCardViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task_card, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TaskCardViewModel::class.java)
        // TODO: Use the ViewModel
    }

}