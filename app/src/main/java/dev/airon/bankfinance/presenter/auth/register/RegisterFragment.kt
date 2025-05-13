package dev.airon.bankfinance.presenter.auth.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dev.airon.bankfinance.R
import dev.airon.bankfinance.databinding.FragmentRegisterBinding

private var _binding: FragmentRegisterBinding? = null
private val binding get() = _binding!!

class RegisterFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                        Toast.makeText(requireContext(), "usuário cadastrado com sucesso", Toast.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}