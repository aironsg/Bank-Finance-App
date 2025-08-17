package dev.airon.bankfinance.presenter.features.recharge

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
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.data.model.Deposit
import dev.airon.bankfinance.data.model.Recharge
import dev.airon.bankfinance.data.model.Transaction
import dev.airon.bankfinance.databinding.FragmentRechargeBinding
import dev.airon.bankfinance.databinding.FragmentRechargeReceiptBinding
import dev.airon.bankfinance.presenter.features.deposit.DepositViewModel
import dev.airon.bankfinance.util.FirebaseHelper
import dev.airon.bankfinance.util.GetMask
import dev.airon.bankfinance.util.PhoneMaskWatcher
import dev.airon.bankfinance.util.StateView
import dev.airon.bankfinance.util.addMoneyMask
import dev.airon.bankfinance.util.hideKeyboard
import dev.airon.bankfinance.util.initToolbar
import dev.airon.bankfinance.util.showBottomSheet

@AndroidEntryPoint
class RechargeFragment : Fragment() {
    private var _binding: FragmentRechargeBinding? = null
    private val binding get() = _binding!!
    private val rechargeViewModel: RechargeViewModel by viewModels()
    private var balance: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRechargeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar, isToolbarDefaultColor = true)
        getTransactions()
        initListener()
        showMaskMoney()
    }

    private fun showMaskMoney() {
        binding.editAmount.addMoneyMask()
    }


    private fun initListener() {
        binding.btnRecharge.setOnClickListener {
            validateDeposit()

        }
    }

    private fun validateDeposit() {

        var amount =
            binding.editAmount.text.toString().replace("[R$\\s.]".toRegex(), "").replace(",", ".")
        val phone =
            binding.editPhone.text.toString().replace("[()\\s-]".toRegex(), "").replace("#", "")
                .replace("-", "").replace(" ", "")
        if (amount.isNotEmpty()) {
            hideKeyboard()
            if (phone.isNotEmpty()) {
                hideKeyboard()
                var recharge = Recharge(amount = amount.toFloat(), phoneNumber = phone)
                saveRecharge(recharge)
            } else {
                Toast.makeText(requireContext(), "Digite um telefone", Toast.LENGTH_SHORT).show()

            }

        } else {
            Toast.makeText(requireContext(), "Digite um valor", Toast.LENGTH_SHORT).show()
        }

    }

    private fun saveRecharge(recharge: Recharge) {
        rechargeViewModel.saveRecharge(recharge).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is StateView.Success -> {


                    stateView.data?.let {
                        saveTransaction(it)

                    }
                }

                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(
                        message = getString(
                            FirebaseHelper.Companion.validError(
                                stateView.message ?: ""
                            )
                        )
                    )
                }
            }
        }
    }

    private fun saveTransaction(recharge: Recharge) {
        val transaction = Transaction(
            id = recharge.id,
            operation = TransactionOperation.RECHARGE,
            date = recharge.date,
            amount = recharge.amount,
            type = TransactionType.CASH_OUT
        )

        rechargeViewModel.saveTransaction(transaction).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is StateView.Success -> {
                    val amount = binding.editAmount.text.toString()
                        .replace("[R$\\s.]".toRegex(), "").replace(",", ".").toFloat()
                    if (balance >= amount) {
                        val action =
                            RechargeFragmentDirections.actionRechargeFragmentToRechargeReceiptFragment(
                                recharge.id
                            )

                        findNavController().navigate(action)
                    } else {
                        showBottomSheet(
                            message = "Saldo insuficiente para recarga"
                        )
                    }
                }

                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(
                        message = getString(
                            FirebaseHelper.Companion.validError(
                                stateView.message ?: ""
                            )
                        )
                    )
                }
            }
        }
    }

    private fun getTransactions() {
        rechargeViewModel.getTransactions().observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE

                }

                is StateView.Success -> {
                    binding.progressBar.visibility = View.GONE
                    balance = getBalance(stateView.data ?: emptyList())

                }

                is StateView.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showBottomSheet(message = stateView.message)
                }
            }
        }


    }

    private fun getBalance(transactions: List<Transaction>): Float {
        var cashIn = 0f
        var cashOut = 0f
        var balance = 0f
        transactions.forEach { transaction ->
            if (transaction.type == TransactionType.CASH_IN) {
                cashIn += transaction.amount
            } else {
                cashOut += transaction.amount
            }
        }
        balance = cashIn - cashOut
        return balance

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}