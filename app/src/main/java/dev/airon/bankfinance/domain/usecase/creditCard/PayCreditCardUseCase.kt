package dev.airon.bankfinance.domain.usecase.creditCard

import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.data.repository.creditCard.CreditCardRepositoryImpl
import jakarta.inject.Inject

class PayCreditCardUseCase @Inject constructor(
    private val repository: CreditCardRepositoryImpl
) {

    suspend operator fun invoke(cardId: String, amount: Float): Boolean {
        // A lógica de qual cardId usar (se é o ID do cartão ou o ID do usuário)
        // deve ser decidida antes de chamar este UseCase, tipicamente no ViewModel.
        return repository.payCreditCard(cardId, amount)
    }
}
