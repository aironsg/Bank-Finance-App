package dev.airon.bankfinance.presenter.deposit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dev.airon.bankfinance.databinding.FragmentDepositBinding
import dev.airon.bankfinance.presenter.auth.login.LoginViewModel
import dev.airon.bankfinance.util.initToolbar


class DepositFragment : Fragment() {
    private var _binding: FragmentDepositBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDepositBinding.inflate(inflater, container, false)
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