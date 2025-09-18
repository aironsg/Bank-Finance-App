package dev.airon.bankfinance.data.repository.creditcard

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.InsufficientBalanceException
import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.domain.repository.creditCard.CreditCardRepository
import dev.airon.bankfinance.domain.repository.wallet.WalletRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class CreditCardRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val walletRepository: WalletRepository
) : CreditCardRepository {

    private val currentUserId: String get() = FirebaseHelper.getUserId()
    private val userCreditCardRef get() = database.reference.child("creditCard").child(currentUserId)

    override suspend fun getCreditCard(): CreditCard {
        val snapshot = userCreditCardRef.get().await()
        if (!snapshot.exists()) throw Exception("Cartão de crédito não encontrado para o usuário '$currentUserId'.")

        // Tenta desserializar direto
        val direct = try {
            snapshot.getValue(CreditCard::class.java)
        } catch (e: Exception) {
            null
        }
        if (direct != null) return direct.copy(id = currentUserId)

        // Se não é direto, pode ser que o snapshot contenha filhos (ex: childCardId -> cardObject)
        val firstChildSnapshot = snapshot.children.firstOrNull()
        val cardFromChild = firstChildSnapshot?.getValue(CreditCard::class.java)
        if (cardFromChild != null) return cardFromChild.copy(id = firstChildSnapshot.key ?: currentUserId)

        throw Exception("Erro ao ler os dados do cartão de crédito (formato inesperado).")
    }

    override suspend fun initCreditCard(creditCard: CreditCard) {
        val cardToSave = creditCard.copy(id = currentUserId)
        userCreditCardRef.setValue(cardToSave).await()
    }

    override suspend fun addCreditCardToUser(creditCard: CreditCard) {
        // no fluxo atual há apenas 1 cartão por usuário -> usa init
        initCreditCard(creditCard)
    }

    override suspend fun deleteCreditCard(id: String): Boolean {
        userCreditCardRef.removeValue().await()
        return true
    }

    /**
     * Paga a fatura: deduz da wallet do usuário e zera a fatura (balance) do cartão.
     * Atualiza também o limite disponível se sua model tratar limit como disponível.
     */
    override suspend fun payCreditCard(cardId: String, amount: Float): Boolean {
        // 1) Ler cartão e fatura
        val card = getCreditCard()
        val currentBill = card.balance

        if (currentBill <= 0f) {
            throw Exception("Nenhuma fatura pendente para este cartão.")
        }

        // o fluxo atual exige pagar o valor exato da fatura
        if (kotlin.math.abs(currentBill - amount) > 0.001f) {
            throw Exception("Valor informado para pagamento não corresponde à fatura atual.")
        }

        // 2) Ler wallet e validar saldo
        val wallet = walletRepository.getWallet()
        if (wallet.balance < amount) {
            throw InsufficientBalanceException("Saldo insuficiente na conta (R$${"%.2f".format(wallet.balance)}).")
        }

        val newWalletBalance = wallet.balance - amount
        // 3) Atualiza wallet e cartão atomically (updateChildren)
        val updates = hashMapOf<String, Any?>(
            "wallet/$currentUserId/balance" to newWalletBalance,
            "creditCard/$currentUserId/balance" to 0f,
            // se o `limit` na sua model representa limite disponível, atualize-o.
            // Se `limit` representa limite total, não atualize. Aqui suponho que `limit` é limite total,
            // então não alteramos. Caso o seu modelo trate `limit` como disponível, descomente:
             "creditCard/$currentUserId/limit" to (card.limit + amount)
        )

        database.reference.updateChildren(updates).await()
        return true
    }
}