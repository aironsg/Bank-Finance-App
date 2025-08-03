package dev.airon.bankfinance.presenter.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import dev.airon.bankfinance.R
import dev.airon.bankfinance.databinding.FragmentChargePhoneBinding
import dev.airon.bankfinance.databinding.FragmentProfileBinding
import dev.airon.bankfinance.presenter.auth.login.LoginViewModel
import kotlin.getValue


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListener()
    }

    private fun initListener() {
        // Botão de logout
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_profileFragment_to_authentication)
        }

        binding.btnEditName.setOnClickListener {
            if (binding.editName.visibility == View.VISIBLE && binding.editName.text.isNullOrBlank()) {
                // Campo visível e vazio → ocultar
                binding.textEditName.visibility = View.GONE
                binding.editName.visibility = View.GONE
            } else {
                // Mostrar campo para editar
                binding.textEditName.visibility = View.VISIBLE
                binding.editName.visibility = View.VISIBLE
            }
        }

        binding.btnEditPassword.setOnClickListener {
            if (binding.editPassword.visibility != View.VISIBLE || !binding.editPassword.text.isNullOrBlank()) {
                // Mostrar campo para editar
                binding.textEditPassword.visibility = View.VISIBLE
                binding.editPassword.visibility = View.VISIBLE
            } else {
                // Campo visível e vazio → ocultar
                binding.textEditPassword.visibility = View.GONE
                binding.editPassword.visibility = View.GONE
            }
        }

        // Listener de texto para ativar/desativar o botão salvar
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSaveButtonVisibility()
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        // Aplica o listener nos campos
        binding.editName.addTextChangedListener(textWatcher)
        binding.editPassword.addTextChangedListener(textWatcher)

        // Botão salvar
        binding.btnSaveProfile.setOnClickListener {
            val name = binding.editName.text.toString()
            val password = binding.editPassword.text.toString()
            Toast.makeText(requireContext(), "Opa, eu fui clicado", Toast.LENGTH_SHORT).show()
            // Aqui você pode continuar com a lógica de atualização
        }
    }

    private fun updateSaveButtonVisibility() {
        val nameVisibleAndFilled = binding.editName.visibility == View.VISIBLE && binding.editName.text?.isNotEmpty() == true
        val passVisibleAndFilled = binding.editPassword.visibility == View.VISIBLE && binding.editPassword.text?.isNotEmpty() == true

        binding.btnSaveProfile.visibility = if (nameVisibleAndFilled || passVisibleAndFilled) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}