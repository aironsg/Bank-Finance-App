package dev.airon.bankfinance.core.extensions

import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import dev.airon.bankfinance.R
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.SecurityUtils.hashPassword
import dev.airon.bankfinance.databinding.BottomsheetPasswordTransactionBinding
import dev.airon.bankfinance.databinding.LayoutBottomSheetBinding
import java.text.NumberFormat
import java.util.Locale

//responsavel por inicializar a toolbar
fun Fragment.initToolbar(
    toolbar: Toolbar,
    homeAsUpEnabled: Boolean = true,
    isToolbarDefaultColor: Boolean = false
) {
    (activity as AppCompatActivity).setSupportActionBar(toolbar)
    (activity as AppCompatActivity).title = ""
    (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(homeAsUpEnabled)

    // Obtenha o drawable da seta de volta
    val arrowDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_back)

    // Verifique se a toolbar está usando a cor padrão (color_default)
    if (isToolbarDefaultColor) {
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

fun isEmailValid(email: String): Boolean {
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return email.matches(emailPattern.toRegex())
}

fun isPasswordValid(password: String): Boolean {
    val passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    return password.matches(passwordPattern.toRegex())
}

fun Fragment.showBottomSheet(
    titleDialog: Int? = null,
    titleButton: Int? = null,
    message: String?,
    onClick: () -> Unit = {}
) {
    val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
    val bottomSheetBinding = LayoutBottomSheetBinding.inflate(layoutInflater, null, false)

    bottomSheetBinding.textTitle.text =
        getString(titleDialog ?: R.string.title_default_bottom_sheet)
    bottomSheetBinding.textMessage.text = message ?: getString(R.string.default_error_alert)
    bottomSheetBinding.btnOk.text =
        getString(titleButton ?: R.string.title_default_button_bottom_sheet)
    bottomSheetBinding.btnOk.setOnClickListener {
        onClick.invoke()
        bottomSheetDialog.dismiss()
    }

    bottomSheetDialog.setContentView(bottomSheetBinding.root)
    bottomSheetDialog.show()

}

@RequiresApi(Build.VERSION_CODES.O)
fun Fragment.bottomSheetPasswordTransaction(
    titleDialog: Int? = null,
    titleButton: Int? = null,
    message: String?,
    onPasswordConfirmed: () -> Unit = {}
) {
    val bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
    val bottomSheetBinding =
        BottomsheetPasswordTransactionBinding.inflate(layoutInflater, null, false)

    bottomSheetBinding.textTitle.text =
        getString(titleDialog ?: R.string.title_default_bottom_sheet)
    bottomSheetBinding.textMessage.text = message ?: getString(R.string.default_error_alert)
    bottomSheetBinding.btnOk.text =
        getString(titleButton ?: R.string.title_default_button_bottom_sheet)

    val editPassword = bottomSheetBinding.editPasswordTransaction
    val btnConfirm = bottomSheetBinding.btnOk

    btnConfirm.setOnClickListener {
        val typedPassword = editPassword.text.toString().trim()

        if (typedPassword.isEmpty()) {
            editPassword.error = "Digite a senha"
            return@setOnClickListener
        }

        // 🔑 Agora buscamos hash + salt no Firebase
        FirebaseHelper.Companion.getPasswordTransaction { storedHash, salt ->
            if (storedHash.isNotEmpty() && salt.isNotEmpty()) {
                // Gera hash da senha digitada com o salt armazenado
                val typedHash = hashPassword(typedPassword, salt)

                if (typedHash == storedHash) {
                    onPasswordConfirmed.invoke()
                    bottomSheetDialog.dismiss()
                } else {
                    editPassword.error = "Senha incorreta"
                }
            } else {
                editPassword.error = "Erro ao validar senha"
            }
        }
    }

    bottomSheetDialog.setContentView(bottomSheetBinding.root)
    bottomSheetDialog.show()
}


fun formatPhoneNumber(phone: String): String {
    // Remove tudo que não for número
    val digits = phone.filter { it.isDigit() }

    // Define a máscara de acordo com a quantidade de dígitos
    val mask = if (digits.length <= 10) "(##) ####-####" else "(##) #####-####"

    var formatted = ""
    var i = 0

    for (m in mask) {
        if (m == '#') {
            if (i < digits.length) {
                formatted += digits[i]
                i++
            } else {
                break
            }
        } else {
            formatted += m
        }
    }

    return formatted
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

fun EditText.addMoneyMask() {
    val locale = Locale("pt", "BR")
    val currencyFormat = NumberFormat.getCurrencyInstance(locale)

    this.addTextChangedListener(object : TextWatcher {
        private var current = ""

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        override fun afterTextChanged(s: Editable?) {
            if (s.toString() != current) {
                this@addMoneyMask.removeTextChangedListener(this)

                val cleanString = s.toString()
                    .replace("[R$,.\\s]".toRegex(), "")

                val parsed = cleanString.toDoubleOrNull() ?: 0.0
                val formatted = currencyFormat.format(parsed / 100)

                current = formatted
                this@addMoneyMask.setText(formatted)
                this@addMoneyMask.setSelection(formatted.length)

                this@addMoneyMask.addTextChangedListener(this)
            }
        }
    })
}

fun Fragment.hideKeyboard() {
    val activity = requireActivity()
    val inputMethodManager =
        activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
    val view = activity.currentFocus ?: View(activity)
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun String.onlyDigits(): String {
    return this.replace(Regex("[^\\d]"), "")
}

fun String.toCpfMask(): String {
    val digits = this.onlyDigits()
    return if (digits.length == 11) {
        "${digits.substring(0, 3)}.${digits.substring(3, 6)}.${
            digits.substring(
                6,
                9
            )
        }-${digits.substring(9, 11)}"
    } else this
}

fun String.toRgMask(): String {
    val digits = this.onlyDigits()
    return if (digits.length == 8) {
        "${digits.substring(0, 1)}.${digits.substring(1, 4)}.${digits.substring(4, 7)}"
    } else this
}


fun EditText.addCpfMask() {
    this.addTextChangedListener(object : TextWatcher {
        private var isUpdating = false
        private val mask = "###.###.###-##"
        private val regex = Regex("[^\\d]")

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (isUpdating) return
            isUpdating = true

            val digits = s.toString().replace(regex, "")
            var formatted = ""
            var i = 0
            for (m in mask.toCharArray()) {
                if (m != '#') {
                    formatted += m
                    continue
                }
                if (i >= digits.length) break
                formatted += digits[i]
                i++
            }

            this@addCpfMask.setText(formatted)
            this@addCpfMask.setSelection(formatted.length)
            isUpdating = false
        }

        override fun afterTextChanged(s: Editable?) {}
    })
}

fun EditText.addRgMask() {
    this.addTextChangedListener(object : TextWatcher {
        private var isUpdating = false
        private val mask = "#.###.###"
        private val regex = Regex("[^\\d]")

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (isUpdating) return
            isUpdating = true

            val digits = s.toString().replace(regex, "")
            var formatted = ""
            var i = 0
            for (m in mask.toCharArray()) {
                if (m != '#') {
                    formatted += m
                    continue
                }
                if (i >= digits.length) break
                formatted += digits[i]
                i++
            }

            this@addRgMask.setText(formatted)
            this@addRgMask.setSelection(formatted.length)
            isUpdating = false
        }

        override fun afterTextChanged(s: Editable?) {}
    })
}

fun EditText.addPhoneMask() {
    this.addTextChangedListener(object : TextWatcher {
        private var isUpdating: Boolean = false
        private val mask = "(##) #-####-####"

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (isUpdating) {
                isUpdating = false
                return
            }

            val digits = s.toString().replace("[^\\d]".toRegex(), "")
            var i = 0
            val formatted = StringBuilder()

            for (m in mask.toCharArray()) {
                if (m == '#') {
                    if (i >= digits.length) break
                    formatted.append(digits[i])
                    i++
                } else {
                    if (i < digits.length) {
                        formatted.append(m)
                    } else break
                }
            }

            isUpdating = true
            this@addPhoneMask.setText(formatted.toString())
            this@addPhoneMask.setSelection(formatted.length)
        }

        override fun afterTextChanged(s: Editable?) {}
    })
}

fun EditText.addEmailValidation() {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            val email = s.toString()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                this@addEmailValidation.error = null
            } else {
                this@addEmailValidation.error = "Email inválido"
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    })
}


