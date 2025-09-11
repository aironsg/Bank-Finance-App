package dev.airon.bankfinance.presentation.ui.features.transfer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.navigation.fragment.R
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.core.extensions.showBottomSheet
import dev.airon.bankfinance.core.util.GetMask
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.data.enum.PaymentMethod
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.databinding.FragmentTransferReceiptBinding
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.domain.model.TransactionPix
import dev.airon.bankfinance.presentation.ui.features.recharge.RechargeReceiptViewModel
import kotlinx.coroutines.Dispatchers

@AndroidEntryPoint
class TransferReceiptFragment : Fragment() {

    private var _binding: FragmentTransferReceiptBinding? = null
    private val binding get() = _binding!!

    private val args: TransferReceiptFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransferReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Agora nunca vem nulo, pois passamos o TransactionPix completo
        val transactionPix: TransactionPix = args.transactionPix
        configData(transactionPix)

        initListener()
    }

    private fun initListener() {
        binding.btnConfirmationTransaction.setOnClickListener {
            val action = TransferReceiptFragmentDirections
                .actionTransferReceiptFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }
    private fun configData(transactionPix: TransactionPix) {
        val transfer = transactionPix.transaction
        val pixDetails = transactionPix.pixDetails

        binding.textCodeTransaction.text = transfer.id
        binding.textAmountTransaction.text = GetMask.getFormatedValue(transfer.amount)


        binding.textDateTransaction.text = GetMask.getFormatedDate(transfer.date, GetMask.DAY_MONTH_YEAR)
        binding.textHourTransaction.text = GetMask.getFormatedDate(transfer.date, GetMask.HOUR_MINUTE)

        binding.textUserName.text = pixDetails.recipientName
        binding.textValueKeyPix.text = pixDetails.recipientPix
        binding.textMethodPaymentValue.text = PaymentMethod.getOperation(transactionPix.paymentMethod)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


