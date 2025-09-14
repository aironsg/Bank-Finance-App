package dev.airon.bankfinance.presentation.ui.features.deposit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.domain.model.Deposit
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.databinding.FragmentDepositBinding
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.core.extensions.addMoneyMask
import dev.airon.bankfinance.core.extensions.hideKeyboard
import dev.airon.bankfinance.core.extensions.initToolbar
import dev.airon.bankfinance.core.extensions.showBottomSheet
import dev.airon.bankfinance.presentation.ui.features.deposit.DepositFormFragmentDirections

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
        showMaskMoney()
    }

    private fun showMaskMoney() {
        binding.editAmount.addMoneyMask()
    }


    private fun initListener() {
        binding.btnDeposit.setOnClickListener {
            validateDeposit()

        }
    }

    private fun validateDeposit() {
        val amountText = binding.editAmount.text.toString()
            .replace("[R$\\s.]".toRegex(), "")
            .replace(",", ".")

        if (amountText.isNotEmpty()) {
            hideKeyboard()
            // O ID será gerado no construtor de Deposit, date será preenchido pelo servidor
            val newDeposit = Deposit(amount = amountText.toFloat())
            processDeposit(newDeposit) // Chama o método atualizado do ViewModel
        } else {
            Toast.makeText(requireContext(), "Digite um valor", Toast.LENGTH_SHORT).show()
        }
    }



    private fun processDeposit(deposit: Deposit) {
        depositViewModel.processNewDeposit(deposit).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is StateView.Success -> {
                    binding.progressBar.visibility = View.INVISIBLE // Esconda o progressbar
                    // Sucesso! O depósito foi salvo, a wallet foi atualizada e a transação foi registrada.
                    // Agora, navegue para o recibo usando o ID do depósito que foi retornado.
                    stateView.data?.let { savedDeposit ->
                        val action = DepositFormFragmentDirections
                            .actionDepositFragmentToDepositReceiptFragment(savedDeposit.id)
                        findNavController().navigate(action)
                    }
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(
                        message = getString(
                            FirebaseHelper.validError(stateView.message ?: "")
                        )
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}