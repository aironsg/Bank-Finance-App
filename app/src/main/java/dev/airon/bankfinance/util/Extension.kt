package dev.airon.bankfinance.util

import android.os.Message
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import dev.airon.bankfinance.R
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.airon.bankfinance.databinding.LayoutBottomSheetBinding

//responsavel por inicializar a toolbar
fun Fragment.initToolbar(toolbar: Toolbar, homeAsUpEnabled: Boolean = true, isToolbarDefaultColor: Boolean = false) {
    (activity as AppCompatActivity).setSupportActionBar(toolbar)
    (activity as AppCompatActivity).title = ""
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(homeAsUpEnabled)

    // Obtenha o drawable da seta de volta
    val arrowDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back)

    // Verifique se a toolbar está usando a cor padrão (color_default)
    if (isToolbarDefaultColor){
        // Se for, defina a cor do drawable para a cor padrão
        arrowDrawable?.setTint(ContextCompat.getColor(requireContext(), R.color.white))
    } else {
        // Caso contrário, defina a cor do drawable para a cor branca
        arrowDrawable?.setTint(ContextCompat.getColor(requireContext(), R.color.color_default))
    }

    (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(arrowDrawable)
    toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
}

fun Fragment.ColorStatusBar(color: Int) {
    requireActivity().window.statusBarColor = resources.getColor(color)
}

fun Fragment.showBottomSheet(
    titleDialog: Int? = null,
    titleButton: Int? = null,
    message: String?,
    onClick: () -> Unit = {}
){
    val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
    val bottomSheetBinding = LayoutBottomSheetBinding.inflate(layoutInflater, null, false)

    bottomSheetBinding.textTitle.text = getString(titleDialog ?: R.string.title_default_bottom_sheet)
    bottomSheetBinding.textMessage.text = message ?: getString(R.string.default_error_alert)
    bottomSheetBinding.btnOk.text = getString(titleButton ?: R.string.title_default_button_bottom_sheet)
    bottomSheetBinding.btnOk.setOnClickListener {
        onClick.invoke()
        bottomSheetDialog.dismiss()
    }

    bottomSheetDialog.setContentView(bottomSheetBinding.root)
    bottomSheetDialog.show()

}





class PhoneMaskWatcher(private val editText: EditText) : TextWatcher {

    private var isUpdating = false
    private var old = ""

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        old = s.toString()
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        val str = s.toString().replace(Regex("[^\\d]"), "") // Remove tudo que não for dígito
        var mask = ""
        var i = 0
        // Define a máscara com base no comprimento do número
        mask = when (str.length) {
            in 0..10 -> "(##) ####-####" // Formato (XX) XXXX-XXXX
            else -> "(##) #-####-####" // Formato (XX) X-XXXX-XXXX
        }

        var formated = ""
        if (isUpdating) {
            old = str
            isUpdating = false
            return
        }

        // Aplica a máscara
        for (m in mask.toCharArray()) {
            if (m != '#') {
                formated += m
                continue
            }
            try {
                formated += str[i]
            } catch (e: Exception) {
                break
            }
            i++
        }

        isUpdating = true
        editText.setText(formated)
        // Move o cursor para o final
        editText.setSelection(formated.length)
    }

    override fun afterTextChanged(s: Editable) {
        // Não é necessário fazer nada aqui neste caso
    }

}

fun EditText.applyPhoneMask() {
    this.addTextChangedListener(PhoneMaskWatcher(this))
}
