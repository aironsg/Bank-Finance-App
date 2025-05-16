package dev.airon.bankfinance.util

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import dev.airon.bankfinance.R

//responsavel por inicializar a toolbar
fun Fragment.initToolbar(toolbar: Toolbar, homeAsUpEnabled: Boolean = true){
    (activity as AppCompatActivity).setSupportActionBar(toolbar)
    (activity as AppCompatActivity).title=""
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(homeAsUpEnabled)
    (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
    toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
}

fun Fragment.ColorStatusBar(color: Int){
    requireActivity().window.statusBarColor = resources.getColor(color)
}