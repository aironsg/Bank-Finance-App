package dev.airon.bankfinance.util

import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import dev.airon.bankfinance.R
import android.text.Editable
import android.text.TextWatcher

//responsavel por inicializar a toolbar
fun Fragment.initToolbar(toolbar: Toolbar, homeAsUpEnabled: Boolean = true) {
    (activity as AppCompatActivity).setSupportActionBar(toolbar)
    (activity as AppCompatActivity).title = ""
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(homeAsUpEnabled)
    (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
    toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
}

fun Fragment.ColorStatusBar(color: Int) {
    requireActivity().window.statusBarColor = resources.getColor(color)
}




// Certifique-se de que a classe PhoneMaskWatcher está definida em algum lugar no seu projeto,
// por exemplo, em um arquivo separado chamado PhoneMaskWatcher.kt dentro do mesmo pacote util.

// Exemplo de como seria a classe PhoneMaskWatcher.kt


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
