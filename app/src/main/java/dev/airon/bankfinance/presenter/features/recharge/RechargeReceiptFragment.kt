package dev.airon.bankfinance.presenter.features.recharge

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
import dev.airon.bankfinance.R
import dev.airon.bankfinance.data.model.Deposit
import dev.airon.bankfinance.data.model.Recharge
import dev.airon.bankfinance.databinding.FragmentDepositBinding
import dev.airon.bankfinance.databinding.FragmentDepositReceiptBinding
import dev.airon.bankfinance.databinding.FragmentRechargeReceiptBinding
import dev.airon.bankfinance.presenter.features.deposit.DepositReceiptViewModel
import dev.airon.bankfinance.util.GetMask
import dev.airon.bankfinance.util.PhoneMaskWatcher
import dev.airon.bankfinance.util.StateView
import dev.airon.bankfinance.util.formatPhoneNumber

@AndroidEntryPoint
class RechargeReceiptFragment : Fragment() {
    private var _binding: FragmentRechargeReceiptBinding? = null
    private val binding get() = _binding!!
    private val args: RechargeReceiptFragmentArgs by navArgs()
    private val viewModel: RechargeReceiptViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRechargeReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getRecharge()
        initListener()
    }


    private fun initListener(){
        binding.btnConfirmationRecharge.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun getRecharge(){
        viewModel.getRecharge(args.IdRecharge).observe(viewLifecycleOwner){ stateView ->
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

    private fun configData(recharge: Recharge) {
        binding.textCodeRecharge.text = recharge.id
        binding.textAmountRecharge.text = GetMask.getFormatedValue(recharge.amount)
        binding.textHourRecharge.text = GetMask.getFormatedDate(recharge.date, GetMask.DAY_MONTH_YEAR)
        binding.textHourRecharge.text = GetMask.getFormatedDate(recharge.date, GetMask.HOUR_MINUTE)
        binding.textPhoneNumber.text = formatPhoneNumber(recharge.phoneNumber)

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}