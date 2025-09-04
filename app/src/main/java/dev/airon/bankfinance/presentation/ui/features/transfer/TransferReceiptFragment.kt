package dev.airon.bankfinance.presentation.ui.features.transfer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.databinding.FragmentTransferReceiptBinding
import dev.airon.bankfinance.presentation.ui.features.recharge.RechargeReceiptViewModel


@AndroidEntryPoint
class TransferReceiptFragment : Fragment() {
    private var _binding: FragmentTransferReceiptBinding? = null
    private val binding get() = _binding!!
//    private val args: RechargeReceiptFragmentArgs by navArgs()
    private val viewModel: RechargeReceiptViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTransferReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }


    private fun initListener(){
        binding.btnConfirmationTransaction.setOnClickListener {
            findNavController().popBackStack()
        }
    }

//    private fun getRecharge(){
//        viewModel.getRecharge(args.IdRecharge).observe(viewLifecycleOwner){ stateView ->
//             when(stateView){
//                is StateView.Loading -> {
//
//                }
//                is StateView.Success -> {
//                    stateView.data?.let {
//                        configData(it)
//                    }
//
//
//                }
//                is StateView.Error -> {
//                    Toast.makeText(requireContext(), "Ocorreu um erro.", Toast.LENGTH_SHORT).show()
//                    findNavController().popBackStack()
//                }
//            }
//        }
//
//    }

//    private fun configData(recharge: Recharge) {
//        binding.textCodeTransaction.text = recharge.id
//        binding.textAmountTransaction.text = GetMask.getFormatedValue(recharge.amount)
//        binding.textDateTransaction.text = GetMask.getFormatedDate(recharge.date, GetMask.DAY_MONTH_YEAR)
//        binding.textHourTransaction.text = GetMask.getFormatedDate(recharge.hour, GetMask.HOUR_MINUTE)
//        binding.textValueKeyPix.text = formatPhoneNumber(recharge.phoneNumber)
//        binding.textMethodPaymentValue.text = PaymentMethod.getOperation(recharge.typeRecharge)
//    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}