package dev.airon.bankfinance.presenter.auth.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.databinding.FragmentLoginBinding
import dev.airon.bankfinance.util.ColorStatusBar
import dev.airon.bankfinance.util.FirebaseHelper
import dev.airon.bankfinance.util.StateView
import dev.airon.bankfinance.util.showBottomSheet


@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ColorStatusBar(R.color.color_default)
        initListener()
    }

    private fun initListener() {
        binding.btnLogin.setOnClickListener {
            validateData()
        }

        binding.btnRecover.setOnClickListener {
            // Navigate to RecoverFragment
            findNavController().navigate(R.id.action_loginFragment_to_recoverFragment)
        }
        binding.btnCreateAccount.setOnClickListener {
            //Navigate to CreateAccountFragment
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun validateData() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (email.isNotEmpty()) {

            if (password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                showBottomSheet(message = getString(R.string.password_is_empty_alert))
            }

        } else {
            showBottomSheet(message = getString(R.string.email_is_empty_alert))

        }
    }

    private fun loginUser(email: String, password: String) {
        loginViewModel.login(email, password).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is StateView.Success -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    findNavController().navigate(R.id.action_global_homeFragment)
                    Toast.makeText(requireContext(), "Bem-Vindo!!!", Toast.LENGTH_SHORT).show()
                }

                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(message = getString( FirebaseHelper.validError(stateView.message ?: "")))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}