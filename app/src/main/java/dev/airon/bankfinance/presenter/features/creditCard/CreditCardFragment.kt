package dev.airon.bankfinance.presenter.features.creditCard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.data.model.Account
import dev.airon.bankfinance.data.model.CreditCard
import dev.airon.bankfinance.databinding.FragmentCreditCardBinding
import dev.airon.bankfinance.util.FirebaseHelper
import dev.airon.bankfinance.util.GetMask
import kotlin.getValue


class CreditCardFragment : Fragment() {

    private var _binding: FragmentCreditCardBinding? = null
    private val binding get() = _binding!!
    private val creditCardViewModel: CreditCardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCreditCard()

    }

    private fun getCreditCard() {
        val userId = FirebaseHelper.getUserId()
        val creditCardRef = FirebaseDatabase.getInstance()
            .getReference("creditCard")
            .child(userId)
        creditCardRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        // Tenta mapear como BankAccount

                        val creditCard = child.getValue(CreditCard::class.java)

                        // Se for um cartão de crédito
                        if (creditCard?.number != null) {
                            binding.cardBalanceFront.creditCardNumber.text =
                               creditCard.number

                            binding.cardBalanceFront.creditCardValidDate.text =
                                creditCard.validDate ?: "--/--"

                            binding.textAvailableBalanceValue.text =
                                GetMask.getFormatedValue(creditCard.balance ?: 0.0)

                            binding.textAvailableLimitValue.text =
                                GetMask.getFormatedValue(creditCard.limit ?: 0.0)

                            binding.cardBalanceBack.textSecurityCodeNumber.text =
                                creditCard.securityCode ?: "Código não encontrado"



                            binding.cardBalanceFront.textUserName.text =

                                creditCard.account?.name ?: "Titular não encontrado"

                            binding.cardBalanceBack.textBankBranchNumber.text =
                                creditCard.account?.branch ?: "Agência não encontrada"

                            binding.cardBalanceBack.textAccountNumber.text =
                                creditCard.account?.accountNumber ?: "0000-0000"
                        }
                    }
                } else {
                    binding.textErrorMessage.visibility = View.VISIBLE
                    binding.textErrorMessage.text = "Nenhum dado encontrado"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.textErrorMessage.visibility = View.VISIBLE
                binding.textErrorMessage.text = "Erro: ${error.message}"
            }
        })
    }





    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}