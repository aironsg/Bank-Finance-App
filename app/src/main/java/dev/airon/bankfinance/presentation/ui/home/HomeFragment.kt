package dev.airon.bankfinance.presentation.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.core.extensions.showBottomSheet
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.GetMask
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.databinding.FragmentHomeBinding
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.presentation.ui.features.account.AccountViewModel
import loadProfileImage


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private val accountViewModel: AccountViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imgProfile.loadProfileImage(requireContext(), FirebaseHelper.getUserId())
        initListener()
        configRecyclerView()
        getTransactions()
        getWalletBalance() // 🔹 Busca o saldo real no servidor
        getUserProfile()
        initNavigationDeposit()
    }

    private fun getUserProfile() {
        val userId = FirebaseHelper.getUserId()
        val nameRef = FirebaseDatabase.getInstance()
            .getReference("profile")
            .child(userId)
            .child("name")

        val accountNumber = FirebaseDatabase.getInstance()
            .getReference("account")
            .child(userId)
            .child("accountNumber")

        nameRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.getValue(String::class.java)
                binding.textUser.text = name ?: "Usuário não encontrado"
            }

            override fun onCancelled(error: DatabaseError) {
                binding.textUser.text = "Erro ao carregar nome"
            }
        })

        accountNumber.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val accountNumber = snapshot.getValue(String::class.java)
                binding.accountNumber.text = accountNumber ?: "conta não encontrada"
            }

            override fun onCancelled(error: DatabaseError) {
                binding.textUser.text = "Erro ao carregar número da conta"
            }
        })
    }

    private fun initNavigationDeposit() {
        binding.cardNewDeposit.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_depositFragment)
        }

        binding.btnAllTransactions.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_extractFragment)
        }

        binding.btnReceiveValues.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_receiveFragment)
        }
    }

    private fun initListener() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_homeFragment_to_authentication)
        }

        binding.cardTransfer.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_transferFragment)
        }

        binding.btnAllTransactions.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_extractFragment)
        }
    }

    private fun getTransactions() {
        homeViewModel.getTransactions().observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is StateView.Success -> {
                    binding.progressBar.visibility = View.GONE
                    transactionAdapter.submitList(stateView.data?.reversed()?.take(6))
                    showSentReceived(stateView.data ?: emptyList()) // 🔹 só enviados/recebidos
                }

                is StateView.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showBottomSheet(message = stateView.message)
                }
            }
        }
    }

    private fun getWalletBalance() {
        homeViewModel.refreshWallet().observe(viewLifecycleOwner) { state ->
            when (state) {
                is StateView.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is StateView.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val wallet = state.data
                    binding.cardBalance.txtTotalBalanceValue.text =
                        getString(R.string.text_formated_value, GetMask.getFormatedValue(wallet?.balance
                            ?: 0f))
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showBottomSheet(message = state.message)
                }
            }
        }
    }

    private fun configRecyclerView() {
        transactionAdapter = TransactionsAdapter { transaction ->
            when (transaction.operation) {
                TransactionOperation.DEPOSIT -> {
                    val action = HomeFragmentDirections
                        .actionHomeFragmentToDepositReceiptFragment(transaction.id)
                    findNavController().navigate(action)
                }

                TransactionOperation.PIX -> {
                    val txId = transaction.id
                    homeViewModel.getTransactionPix(txId).observe(viewLifecycleOwner) { state ->
                        when (state) {
                            is StateView.Success -> {
                                val transactionPix = state.data
                                if (transactionPix != null) {
                                    val action = HomeFragmentDirections
                                        .actionHomeFragmentToTransferReceiptFragment(transactionPix)
                                    findNavController().navigate(action)
                                } else {
                                    showBottomSheet(message = "Recibo não encontrado para esta transação")
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
                    val cardId = transaction.relatedCardId ?: transaction.id
                    val action = HomeFragmentDirections
                        .actionHomeFragmentToCreditCardReceiptFragment(
                            cardId = cardId,
                            amountPaid = transaction.amount
                        )
                    findNavController().navigate(action)
                }

                TransactionOperation.RECHARGE -> {
                    val action = HomeFragmentDirections
                        .actionHomeFragmentToRechargeReceiptFragment(transaction.id)
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

    private fun toggleEmptyView(isEmpty: Boolean) {
        val emptyView = binding.tvEmptyTransactions
        val recycler = binding.recyclerViewTransactions

        if (isEmpty) {
            recycler.visibility = View.GONE
            emptyView.apply {
                alpha = 0f
                visibility = View.VISIBLE
                animate().alpha(1f).setDuration(300).start()
            }
        } else {
            emptyView.visibility = View.GONE
            recycler.visibility = View.VISIBLE
        }
    }

    private fun showSentReceived(transactions: List<Transaction>) {
        var sent = 0f
        var received = 0f

        transactions.forEach { transaction ->
            when (transaction.type) {
                TransactionType.PIX_OUT, TransactionType.CASH_OUT -> {
                    sent += transaction.amount
                }
                TransactionType.PIX_IN, TransactionType.CASH_IN -> {
                    received += transaction.amount
                }
                else -> Unit
            }
        }

        binding.cardBalance.txtSentValue.text =
            getString(R.string.text_formated_value, GetMask.getFormatedValue(sent))
        binding.cardBalance.txtReceivedValue.text =
            getString(R.string.text_formated_value, GetMask.getFormatedValue(received))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




