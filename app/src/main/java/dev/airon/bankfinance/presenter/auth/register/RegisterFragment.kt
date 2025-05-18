package dev.airon.bankfinance.presenter.auth.register

import android.os.Bundle
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
import dev.airon.bankfinance.databinding.FragmentRegisterBinding
import dev.airon.bankfinance.presenter.profile.SaveProfileViewModel
import dev.airon.bankfinance.util.ColorStatusBar
import dev.airon.bankfinance.util.FirebaseHelper
import dev.airon.bankfinance.util.StateView
import dev.airon.bankfinance.util.applyPhoneMask
import dev.airon.bankfinance.util.initToolbar
import dev.airon.bankfinance.util.showBottomSheet

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val registerViewModel : RegisterViewModel by viewModels()
    private val profileViewModel : SaveProfileViewModel by viewModels()

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
        initListener()
    }

    private fun initListener() {
        binding.btnCreateAccount.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        val name = binding.editName.text.toString().trim()
        val phone = binding.editPhone.unMaskedText
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (name.isNotEmpty()) {
            if (phone?.isNotEmpty() == true) {
                if( phone.length == 11){

                if (email.isNotEmpty()) {
                    if (password.isNotEmpty()) {
                        //sucesso
                        val user = User(name, phone, email, password)
                        registerUser(name, phone, email, password)

                    } else {
                        showBottomSheet(message = getString(R.string.password_is_empty_alert))
                    }
                } else {
                    showBottomSheet(message = getString(R.string.email_is_empty_alert))
                }
                }else{
                    showBottomSheet(message = getString(R.string.phone_invalid_alert))

                }

            } else {
                showBottomSheet(message = getString(R.string.phone_is_empty_alert))
            }
        } else {
            showBottomSheet(message = getString(R.string.name_is_empty_alert))

        }

    }

    private fun saveProfile(user: User){
        profileViewModel.saveProfile(user).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {

                }
                is StateView.Success -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    findNavController().navigate(R.id.action_global_homeFragment)
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(message = getString( FirebaseHelper.validError(stateView.message ?: "")))
                }
            }
        }
    }

    private fun registerUser(name: String, phone: String, email: String, password: String) {
        registerViewModel.register(name, phone, email, password).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is StateView.Success -> {
                    stateView.data?.let { saveProfile(it)}

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