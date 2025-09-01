package dev.airon.bankfinance.presentation.ui.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.databinding.FragmentSplashBinding
import dev.airon.bankfinance.core.util.FirebaseHelper

@AndroidEntryPoint
class SplashFragment : Fragment() {

    private var _binding : FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //responsavel por aguardar 3 segundos antes de verificar se o usuario esta autenticado
        Handler(Looper.getMainLooper()).postDelayed(this::verifyAuth, 3000)

    }

    private fun verifyAuth() {
        if(FirebaseHelper.isAuthenticated()){
            findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
        }else{
            findNavController().navigate(R.id.action_splashFragment_to_authentication)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}