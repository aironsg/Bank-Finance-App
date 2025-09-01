package dev.airon.bankfinance.presentation.ui.auth.recover

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.databinding.FragmentRecoverBinding
import dev.airon.bankfinance.core.extensions.ColorStatusBar
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.core.extensions.hideKeyboard
import dev.airon.bankfinance.core.extensions.initToolbar
import dev.airon.bankfinance.core.extensions.isEmailValid
import dev.airon.bankfinance.core.extensions.showBottomSheet


@AndroidEntryPoint
class RecoverFragment : Fragment() {
    private var _binding: FragmentRecoverBinding? = null
    private val binding get() = _binding!!
    private val recoverViewModel: RecoverViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _binding = FragmentRecoverBinding.inflate(inflater,container,false)
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
            findNavController().navigate(R.id.action_recoverFragment_to_registerFragment)
        }

        binding.btnRecover.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        val email = binding.edtSendEmail.text.toString().trim()



        if(email.isNotEmpty()){
            if(isEmailValid(email)){
                hideKeyboard()
                recoverUser(email)
            }else{
                Toast.makeText(requireContext(), "O e-mail digitado é inválido", Toast.LENGTH_SHORT)
                    .show()
            }
        }else{
            showBottomSheet(message = getString(R.string.email_is_empty_alert))
        }
    }


    private fun sendMail(email: String) {
        binding.flyingMailIcon.visibility = View.VISIBLE
        val translationY = -binding.btnRecover.y - binding.btnRecover.height * 2

        binding.flyingMailIcon
            .animate()
            .translationY(translationY)
            .scaleX(0.5f)  // Reduz a escala no eixo X
            .scaleY(0.5f)  // Reduz a escala no eixo Y
            .alpha(0f)     // Desaparece gradualmente
            .setInterpolator(AccelerateInterpolator()) // Efeito de aceleração
            .setDuration(2500) // Duração mais longa para suavidade
            .withEndAction {
                binding.flyingMailIcon.visibility = View.INVISIBLE
                binding.flyingMailIcon.alpha = 1f // Restaura opacidade
                binding.flyingMailIcon.scaleX = 1f // Restaura escala
                binding.flyingMailIcon.scaleY = 1f
                binding.flyingMailIcon.translationY = 0f // Restaura posição
            }
            .start()
    }

    private fun recoverUser(email: String) {
        recoverViewModel.recover(email).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }

                is StateView.Success -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(message =  "Você está prestes a enviar um E-mail para : $email ",
                        titleButton = R.string.txt_button_bottomSheet_confirm,
                        onClick ={
                            sendMail(email)
                            Handler(Looper.getMainLooper()).postDelayed({
                                findNavController().navigate(R.id.action_recoverFragment_to_loginFragment)
                            }, 3000)
                        } )
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