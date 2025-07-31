package dev.airon.bankfinance.presenter.home

import android.graphics.drawable.ColorStateListDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.airon.bankfinance.R
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.data.model.Transaction
import dev.airon.bankfinance.databinding.TransactionItemBinding
import dev.airon.bankfinance.util.GetMask

class TransactionsAdapter(
    private val transactionSelected: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionsAdapter.ViewHolder>(DIFF_CALLBACK) {

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
            TransactionItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = getItem(position)
        transaction.operation?.let {
            holder.binding.textTransactionInformation.text = TransactionOperation.getOperation(it)


            holder.binding.textTransactionType.text = TransactionType.getType(it).toString()
            holder.binding.textTransactionType.background = if(transaction.type == TransactionType.CASH_IN) {
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_round_cash_in)
            } else {
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_round_cash_out)
            }


        }
        holder.binding.textTransactionValue.text = GetMask.getFormatedValue(transaction.amount)

        holder.binding.textTransactionDate.text =
            GetMask.getFormatedDate(transaction.date, GetMask.DAY_MONTH_YEAR_HOUR_MINUTE)

        holder.itemView.setOnClickListener {
            transactionSelected(transaction)
        }
    }

    inner class ViewHolder(val binding: TransactionItemBinding) :
        RecyclerView.ViewHolder(binding.root)

}