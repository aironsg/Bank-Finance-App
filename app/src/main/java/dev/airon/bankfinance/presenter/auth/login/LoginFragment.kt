package dev.airon.bankfinance.presenter.auth.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.databinding.FragmentLoginBinding

private var _binding : FragmentLoginBinding?  = null
private val binding get() = _binding!!

@AndroidEntryPoint
class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListener()
    }

    private fun initListener() {
        binding.btnLogin.setOnClickListener {
            validateData()
        }

        binding.btnRecover.setOnClickListener {
            // Navigate to RecoverFragment
            // findNavController().navigate(R.id.action_loginFragment_to_recoverFragment)
        }
        binding.btnCreateAccount.setOnClickListener {
            // Navigate to CreateAccountFragment
            // findNavController().navigate(R.id.action_loginFragment_to_createAccountFragment)
        }
    }

    private fun validateData() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (email.isNotEmpty()) {
            
            if (password.isNotEmpty()) {
                Toast.makeText(requireContext(), "validação realizada com sucesso", Toast.LENGTH_SHORT).show()

            }else{
                Toast.makeText(requireContext(), "digite sua senha", Toast.LENGTH_SHORT).show()
            }

        }else{
                Toast.makeText(requireContext(), "digite seu email", Toast.LENGTH_SHORT).show()
            
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}