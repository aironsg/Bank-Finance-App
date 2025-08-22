package dev.airon.bankfinance.presenter.home.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import dev.airon.bankfinance.data.model.Transaction
import dev.airon.bankfinance.databinding.ViewTransactionListBinding
import dev.airon.bankfinance.util.StateView

class TransactionListView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = ViewTransactionListBinding.inflate(LayoutInflater.from(context), this, true)

    fun setAdapter(adapter: RecyclerView.Adapter<*>) {
        binding.recyclerTransactions.adapter = adapter
    }

    fun bindState(state: StateView<List<Transaction>>) {
        when (state) {
            is StateView.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
                binding.tvEmptyTransactions.visibility = View.GONE
            }
            is StateView.Success -> {
                binding.progressBar.visibility = View.GONE
                if (state.data.isNullOrEmpty()) {
                    binding.tvEmptyTransactions.visibility = View.VISIBLE
                } else {
                    binding.tvEmptyTransactions.visibility = View.GONE
                }
            }
            is StateView.Error -> {
                binding.progressBar.visibility = View.GONE
                binding.tvEmptyTransactions.text = "Erro ao carregar transações"
                binding.tvEmptyTransactions.visibility = View.VISIBLE
            }
        }
    }
}
