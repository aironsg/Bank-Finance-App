package dev.airon.bankfinance.presenter.home.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.HorizontalScrollView
import dev.airon.bankfinance.R
import dev.airon.bankfinance.databinding.ItemActionButtonBinding
import dev.airon.bankfinance.databinding.ViewActionButtonsBinding





class ActionButtonsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : HorizontalScrollView(context, attrs) {

    private val binding =
        ViewActionButtonsBinding.inflate(LayoutInflater.from(context), this, true)

    private var depositClick: (() -> Unit)? = null
    private var transferClick: (() -> Unit)? = null
    private var payClick: (() -> Unit)? = null
    private var accountClick: (() -> Unit)? = null

    init {
        // Define ícones, rótulos e listeners de cada ação
        setupAction(
            item = binding.btnDeposit,
            iconRes = R.drawable.ic_add_deposit,
            label = "Depositar"
        ) { depositClick?.invoke() }

        setupAction(
            item = binding.btnTransfer,
            iconRes = R.drawable.ic_transfer,
            label = "Transferir"
        ) { transferClick?.invoke() }

        setupAction(
            item = binding.btnPayment,
            iconRes = R.drawable.ic_payment,
            label = "Pagar Contas"
        ) { payClick?.invoke() }

        setupAction(
            item = binding.btnAccount,
            iconRes = R.drawable.ic_information_account,
            label = "Conta"
        ) { accountClick?.invoke() }
    }

    private fun setupAction(
        item: ItemActionButtonBinding,
        iconRes: Int,
        label: String,
        onClick: () -> Unit
    ) {
        // define ícone e texto do include (item_action_button.xml)
        item.icon.setImageResource(iconRes)
        item.label.text = label

        // clique deve ser setado no root do include
        item.root.isClickable = true
        item.root.isFocusable = true
        item.root.setOnClickListener { onClick() }
    }

    // APIs de listener (duas assinaturas para conveniência)
    fun setOnDepositClickListener(listener: () -> Unit) { depositClick = listener }
    fun onDepositClick(listener: () -> Unit) = setOnDepositClickListener(listener)

    fun setOnTransferClickListener(listener: () -> Unit) { transferClick = listener }
    fun onTransferClick(listener: () -> Unit) = setOnTransferClickListener(listener)

    fun setOnPaymentClickListener(listener: () -> Unit) { payClick = listener }
    fun onPayClick(listener: () -> Unit) = setOnPaymentClickListener(listener)

    fun setOnAccountClickListener(listener: () -> Unit) { accountClick = listener }
    fun onAccountInfoClick(listener: () -> Unit) = setOnAccountClickListener(listener)

    // (Opcional) Helpers para personalizar em runtime
    fun setDepositLabel(text: String) { binding.btnDeposit.label.text = text }
    fun setTransferLabel(text: String) { binding.btnTransfer.label.text = text }
    fun setPaymentLabel(text: String) { binding.btnPayment.label.text = text }
    fun setAccountLabel(text: String) { binding.btnAccount.label.text = text }

    fun setDepositIcon(resId: Int) { binding.btnDeposit.icon.setImageResource(resId) }
    fun setTransferIcon(resId: Int) { binding.btnTransfer.icon.setImageResource(resId) }
    fun setPaymentIcon(resId: Int) { binding.btnPayment.icon.setImageResource(resId) }
    fun setAccountIcon(resId: Int) { binding.btnAccount.icon.setImageResource(resId) }
}
