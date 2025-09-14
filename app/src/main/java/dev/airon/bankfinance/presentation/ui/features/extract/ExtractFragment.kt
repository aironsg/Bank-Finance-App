package dev.airon.bankfinance.presentation.ui.features.extract

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.core.extensions.initToolbar
import dev.airon.bankfinance.core.extensions.showBottomSheet
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.databinding.FragmentExtractBinding
import dev.airon.bankfinance.presentation.ui.features.extract.ExtractFragmentDirections
import dev.airon.bankfinance.presentation.ui.home.TransactionsAdapter

@AndroidEntryPoint
class ExtractFragment : Fragment() {

    private var _binding: FragmentExtractBinding? = null
    private val binding get() = _binding!!
    private val extractViewModel: ExtractViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExtractBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
        configRecyclerView()
        getTransactions()
    }

    private fun getTransactions() {
        extractViewModel.getTransactions().observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                is StateView.Success -> {
                    binding.progressBar.visibility = View.GONE
                    transactionAdapter.submitList(stateView.data?.reversed())
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showBottomSheet(message = stateView.message)
                }
            }
        }
    }

    private fun configRecyclerView() {
        transactionAdapter = TransactionsAdapter { transaction ->
            when (transaction.operation) {
                TransactionOperation.DEPOSIT -> {
                    val action = ExtractFragmentDirections
                        .actionExtractFragmentToDepositReceiptFragment(transaction.id)
                    findNavController().navigate(action)
                }
                TransactionOperation.PIX -> {
                    extractViewModel.getTransactionPix(transaction.id).observe(viewLifecycleOwner) { state ->
                        when (state) {
                            is StateView.Success -> {
                                state.data?.let { transactionPix ->
                                    val action = ExtractFragmentDirections
                                        .actionExtractFragmentToTransferReceiptFragment(transactionPix)
                                    findNavController().navigate(action)
                                }
                            }
                            is StateView.Error -> {
                                showBottomSheet(message = state.message)
                            }
                            else -> Unit
                        }
                    }
                }
                TransactionOperation.CARD_PAYMENT -> {
                    val cardId = transaction.relatedCardId ?: run {
                        showBottomSheet(message = "ID do cartão não encontrado para este pagamento")
                        return@TransactionsAdapter
                    }

                    val action = ExtractFragmentDirections
                        .actionExtractFragmentToCreditCardReceiptFragment(
                            cardId = cardId,
                            amountPaid = transaction.amount
                        )
                    findNavController().navigate(action)
                }
                TransactionOperation.RECHARGE -> {
                    val action = ExtractFragmentDirections
                        .actionExtractFragmentToRechargeReceiptFragment(transaction.id)
                    findNavController().navigate(action)
                }
                else -> {
                    showBottomSheet(message = "Recibo não implementado para esta operação")
                }
            }
        }

        with(binding.recyclerViewTransactions) {
            setHasFixedSize(true)
            adapter = transactionAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

