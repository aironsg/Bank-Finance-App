package dev.airon.bankfinance.presentation.ui.features.creditCard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.core.util.GetMask
import dev.airon.bankfinance.databinding.FragmentCreditCardReceiptBinding

@AndroidEntryPoint
class CreditCardReceiptFragment : Fragment() {

    private var _binding: FragmentCreditCardReceiptBinding? = null
    private val binding get() = _binding!!

    private val args: CreditCardReceiptFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditCardReceiptBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        initListener()
    }

    private fun setupUI() {
        val cardId = args.cardId
        val amountPaid = args.amountPaid

        // 🔹 Preenche os dados do recibo
        binding.textCodePayment.text = cardId
        binding.textAmountPayment.text = GetMask.getFormatedValue(amountPaid)
        binding.textDatePayment.text =
            GetMask.getFormatedDate(System.currentTimeMillis(), GetMask.DAY_MONTH_YEAR)
        binding.textHourPayment.text =
            GetMask.getFormatedDate(System.currentTimeMillis(), GetMask.HOUR_MINUTE)
    }

    private fun initListener() {
        binding.btnConfirmationTransaction.setOnClickListener {
            findNavController().navigate(R.id.action_creditCardReceiptFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

