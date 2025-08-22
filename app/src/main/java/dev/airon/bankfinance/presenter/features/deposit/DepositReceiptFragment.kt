package dev.airon.bankfinance.presenter.features.deposit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.data.model.Deposit
import dev.airon.bankfinance.databinding.FragmentDepositReceiptBinding
import dev.airon.bankfinance.util.GetMask
import dev.airon.bankfinance.util.StateView

@AndroidEntryPoint
class DepositReceiptFragment : Fragment() {
    private var _binding: FragmentDepositReceiptBinding? = null
    private val binding get() = _binding!!
    private val args: DepositReceiptFragmentArgs by navArgs()
    private val viewModel: DepositReceiptViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDepositReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDeposit()
        initListener()
    }


    private fun initListener(){
        binding.btnConfirmationTransaction.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun getDeposit(){
        viewModel.getDeposit(args.idDeposit).observe(viewLifecycleOwner){ stateView ->
             when(stateView){
                is StateView.Loading -> {

                }
                is StateView.Success -> {
                    stateView.data?.let {
                        configData(it)
                    }


                }
                is StateView.Error -> {
                    Toast.makeText(requireContext(), "Ocorreu um erro.", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }

    }

    private fun configData(deposit: Deposit) {
        binding.textCodeDeposit.text = deposit.id
        binding.textAmountDeposit.text = GetMask.getFormatedValue(deposit.amount)
        binding.textDateDeposit.text = GetMask.getFormatedDate(deposit.date, GetMask.DAY_MONTH_YEAR)
        binding.textHourDeposit.text = GetMask.getFormatedDate(deposit.date, GetMask.HOUR_MINUTE)


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}