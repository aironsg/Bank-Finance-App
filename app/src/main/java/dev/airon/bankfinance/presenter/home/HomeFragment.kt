package dev.airon.bankfinance.presenter.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.data.model.Wallet
import dev.airon.bankfinance.databinding.FragmentHomeBinding
import dev.airon.bankfinance.util.GetMask
import dev.airon.bankfinance.util.StateView
import dev.airon.bankfinance.util.showBottomSheet

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding : FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var bottomNavigationView: BottomNavigationView
    private val homeViewModel : HomeViewModel by viewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getWallet()
        initNavigationDeposit()

    }

    private fun initNavigationDeposit(){

        binding.newDeposit.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_depositFragment)
        }
    }

    private fun getWallet(){
        homeViewModel.getWallet().observe(viewLifecycleOwner){ stateView ->
             when(stateView){
                is StateView.Loading -> {

                }
                is StateView.Success -> {
                    stateView.data?.let{
                    showBalance(it)

                    }
                }
                is StateView.Error -> {
                    showBottomSheet(message = stateView.message)
                }
            }
        }
    }

    private fun showBalance(wallet: Wallet){
        binding.textBalance.text = getString(R.string.text_formated_value, GetMask.getFormatedValue(wallet.balance))

    }


   
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}