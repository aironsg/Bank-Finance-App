package dev.airon.bankfinance.presenter.auth.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.databinding.FragmentRegisterBinding
import dev.airon.bankfinance.util.ColorStatusBar
import dev.airon.bankfinance.util.StateView
import dev.airon.bankfinance.util.applyPhoneMask
import dev.airon.bankfinance.util.initToolbar

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val registerViewModel : RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ColorStatusBar(R.color.white)
        initToolbar(binding.toolbar)
        binding.editPhone.applyPhoneMask()
        initListener()
    }

    private fun initListener() {
        binding.btnCreateAccount.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        val name = binding.editName.text.toString().trim()
        val phone = binding.editPhone.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (name.isNotEmpty()) {
            if (phone.isNotEmpty()) {
                if (email.isNotEmpty()) {
                    if (password.isNotEmpty()) {

                        //sucesso
                        val user = User(name, phone, email, password)
                        registerUser(user)

                    } else {
                        Toast.makeText(requireContext(), "digite sua senha", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    Toast.makeText(requireContext(), "digite seu email", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(requireContext(), "digite seu telefone", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "digite seu nome", Toast.LENGTH_SHORT).show()

        }

    }

    private fun registerUser(user: User) {
        registerViewModel.register(user).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is StateView.Success -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), "Usuário cadastrado com sucesso", Toast.LENGTH_SHORT).show()
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    Toast.makeText(requireContext(), stateView.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}