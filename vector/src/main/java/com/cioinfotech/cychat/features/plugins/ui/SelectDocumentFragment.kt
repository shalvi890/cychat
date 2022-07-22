package com.cioinfotech.cychat.features.plugins.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cioinfotech.cychat.R
import com.cioinfotech.cychat.core.extensions.addFragmentToBackstack
import com.cioinfotech.cychat.core.platform.VectorBaseFragment
import com.cioinfotech.cychat.features.plugins.PluginsActivity
import com.cioinfotech.cychat.features.plugins.adapter.SelectDocumentAdapter

class SelectDocumentFragment : VectorBaseFragment<com.cioinfotech.cychat.databinding.FragmentSelectDocumentBinding>(), SelectDocumentAdapter.ItemClickListener {
    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) = com.cioinfotech.cychat.databinding.FragmentSelectDocumentBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as PluginsActivity).setToolbarTitle(getString(R.string.select_document))
        views.rvDocuments.adapter = SelectDocumentAdapter().apply {
            itemClickListener = this@SelectDocumentFragment
        }
    }

    override fun onItemClicked() {
        addFragmentToBackstack(R.id.container, SelectedDocumentDetailsFragment::class.java, allowStateLoss = false)
    }
}
