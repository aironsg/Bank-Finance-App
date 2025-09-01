package dev.airon.bankfinance.presentation.ui.features.receive

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.databinding.FragmentReceiveBinding
import dev.airon.bankfinance.core.extensions.initToolbar

@AndroidEntryPoint
class ReceiveFragment : Fragment() {

    private var _binding: FragmentReceiveBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReceiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar, isToolbarDefaultColor = true)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}