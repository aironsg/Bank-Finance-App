package dev.airon.bankfinance.presenter.features.extract

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.databinding.FragmentDepositBinding
import dev.airon.bankfinance.databinding.FragmentExtractBinding
import dev.airon.bankfinance.presenter.features.deposit.DepositViewModel
import dev.airon.bankfinance.presenter.home.HomeFragmentDirections
import dev.airon.bankfinance.presenter.home.TransactionsAdapter
import dev.airon.bankfinance.util.StateView
import dev.airon.bankfinance.util.initToolbar
import dev.airon.bankfinance.util.showBottomSheet
import kotlin.getValue

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
                is StateView.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE

                }

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

            when(transaction.operation){
                TransactionOperation.DEPOSIT -> {
                    val action = ExtractFragmentDirections.actionExtractFragmentToDepositReceiptFragment(transaction.id)
                    findNavController().navigate(action)
                }else ->{

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