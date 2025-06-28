package dev.airon.bankfinance.presenter.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.airon.bankfinance.data.model.Transaction
import dev.airon.bankfinance.databinding.LastTransactionItemBinding
import dev.airon.bankfinance.util.GetMask

class LastTransactionsAdapter(
    private val transactionSelected: (Transaction) -> Unit
) : ListAdapter<Transaction, LastTransactionsAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(
                oldItem: Transaction,
                newItem: Transaction
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Transaction,
                newItem: Transaction
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LastTransactionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.binding.textTransactionInformation.text = transaction.description
        holder.binding.textTransactionType.text = when (transaction.description) {
            "Transferência" -> "T"
            "Recarga" -> "R"
            "Depósito" -> "D"
            else -> {
                ""
            }
        }
        holder.binding.textTransactionValue.text = GetMask.getFormatedValue(transaction.value)
        holder.binding.textTransactionDate.text = GetMask.getFormatedDate(transaction.date, GetMask.DAY_MONTH_YEAR_HOUR_MINUTE)

    }

    inner class ViewHolder(val binding: LastTransactionItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}