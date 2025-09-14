package dev.airon.bankfinance.domain.usecase.transaction

import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import dev.airon.bankfinance.domain.model.TransactionPix
import jakarta.inject.Inject

class GetTransactionPixUseCase @Inject constructor(
    private val repository: TransactionRepositoryImpl
) {
    suspend operator fun invoke(id: String): TransactionPix? = repository.getTransactionPixById(id)
}
