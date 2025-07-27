package dev.airon.bankfinance.presenter.features.deposit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.data.model.Deposit
import dev.airon.bankfinance.databinding.FragmentDepositBinding
import dev.airon.bankfinance.presenter.auth.login.LoginViewModel
import dev.airon.bankfinance.util.FirebaseHelper
import dev.airon.bankfinance.util.StateView
import dev.airon.bankfinance.util.initToolbar
import dev.airon.bankfinance.util.showBottomSheet
import java.time.LocalDate

@AndroidEntryPoint
class DepositFormFragment : Fragment() {
    private var _binding: FragmentDepositBinding? = null
    private val binding get() = _binding!!
    private val depositViewModel: DepositViewModel by viewModels()

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
        initListener()
    }


    private fun initListener() {
        binding.btnDeposit.setOnClickListener {
            validateDeposit()

        }
    }

    private fun validateDeposit(){

        var amount = binding.editAmount.text.toString().trim()
        if (amount.isNotEmpty()){
            var deposit = Deposit(amount = amount.toFloat())

            saveDeposit(deposit)
        }else{
            Toast.makeText(requireContext(), "Digite um valor", Toast.LENGTH_SHORT).show()
        }

    }

    private fun saveDeposit(deposit: Deposit){
        depositViewModel.saveDeposit(deposit).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is StateView.Success -> {

                    Toast.makeText(requireContext(), "deposito realizado com sucesso", Toast.LENGTH_SHORT).show()
                }

                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(message = getString( FirebaseHelper.validError(stateView.message ?: "")))
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}