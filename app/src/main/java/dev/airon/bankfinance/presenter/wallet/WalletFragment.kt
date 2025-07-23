package dev.airon.bankfinance.presenter.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dev.airon.bankfinance.R
import dev.airon.bankfinance.databinding.FragmentProfileBinding
import dev.airon.bankfinance.databinding.FragmentWalletBinding
import dev.airon.bankfinance.presenter.auth.login.LoginViewModel
import kotlin.getValue


class WalletFragment : Fragment() {
    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}