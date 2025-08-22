package dev.airon.bankfinance.presenter.home.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import dev.airon.bankfinance.R
import dev.airon.bankfinance.data.model.Account
import dev.airon.bankfinance.databinding.CardBalanceBinding

class CardBalanceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binding = CardBalanceBinding.inflate(LayoutInflater.from(context), this, true)

    fun bindAccount(account: Account) {
        binding.txtTotalBalanceValue.text =
            context.getString(R.string.text_formated_value, account.balance)

        binding.btnToggleBalance.setOnClickListener {
            val isVisible = binding.txtTotalBalanceValue.visibility == View.VISIBLE
            binding.txtTotalBalanceValue.visibility = if (isVisible) View.GONE else View.VISIBLE
            binding.btnToggleBalance.setImageResource(
                if (isVisible) R.drawable.ic_arrow_drop_down else R.drawable.ic_arrow_drop_up
            )
        }
    }
}
