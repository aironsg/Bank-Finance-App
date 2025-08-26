package dev.airon.bankfinance.presenter.home

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
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.data.model.Transaction
import dev.airon.bankfinance.databinding.FragmentHomeBinding
import dev.airon.bankfinance.util.FirebaseHelper
import dev.airon.bankfinance.util.GetMask
import dev.airon.bankfinance.util.StateView
import dev.airon.bankfinance.util.showBottomSheet
import loadProfileImage
import androidx.core.view.isVisible
import clearProfileImage
import dev.airon.bankfinance.presenter.features.account.AccountViewModel

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
        getUserProfile()
        initNavigationDeposit()


    }

    private fun getUserProfile(){
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
    }

    private fun initListener(){
        binding.btnLogout.setOnClickListener {

            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_homeFragment_to_authentication)
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
                    showBalance(stateView.data ?: emptyList())

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
                    val action = HomeFragmentDirections.actionHomeFragmentToDepositReceiptFragment(transaction.id)
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


    private fun showBalance(transactions: List<Transaction>) {
        var cashIn = 0f
        var cashOut = 0f
        transactions.forEach { transaction ->
            if (transaction.type == TransactionType.CASH_IN) {
                cashIn += transaction.amount
            }

            if (transaction.type == TransactionType.CASH_OUT) {
                cashOut += transaction.amount
            }
        }


        binding.cardBalance.txtTotalBalanceValue.text =
            getString(R.string.text_formated_value, GetMask.getFormatedValue(cashIn - cashOut))
        if (transactions.isEmpty()) {
            binding.tvEmptyTransactions.visibility = View.VISIBLE
            binding.recyclerViewTransactions.visibility = View.GONE
        } else {
            binding.tvEmptyTransactions.visibility = View.GONE
            binding.recyclerViewTransactions.visibility = View.VISIBLE
//            binding.recyclerViewTransactions.adapter = TransactionsAdapter(transactions)
        }
        binding.cardBalance.txtTotalBalanceValue.setTextColor(
            resources.getColor(
                R.color.white,
                null
            )
        )


        //dados apenas para teste de UI
        binding.cardBalance.txtSentValue.text =
            getString(R.string.text_formated_value, GetMask.getFormatedValue(cashOut))
        binding.cardBalance.txtReceivedValue.text =
            getString(R.string.text_formated_value, GetMask.getFormatedValue(cashIn))
        binding.cardBalance.btnToggleBalance.setOnClickListener {
            if (binding.cardBalance.txtTotalBalanceValue.isVisible) {
                binding.cardBalance.txtTotalBalanceValue.visibility = View.GONE
                binding.cardBalance.btnToggleBalance.setImageResource(R.drawable.ic_arrow_drop_down)


            } else {
                binding.cardBalance.txtTotalBalanceValue.visibility = View.VISIBLE
                binding.cardBalance.btnToggleBalance.setImageResource(R.drawable.ic_arrow_drop_up)

            }
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}