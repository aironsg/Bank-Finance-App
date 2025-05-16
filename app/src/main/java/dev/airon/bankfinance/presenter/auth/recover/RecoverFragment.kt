package dev.airon.bankfinance.presenter.auth.recover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.databinding.FragmentRecoverBinding
import dev.airon.bankfinance.util.ColorStatusBar
import dev.airon.bankfinance.util.initToolbar

private var _binding : FragmentRecoverBinding? = null
private val binding get() = _binding!!

@AndroidEntryPoint
class RecoverFragment : Fragment() {

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
        binding.btnRecover.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        val email = binding.appCompatEditText.text.toString().trim()

        if(email.isNotEmpty()){
            Toast.makeText(requireContext(), "email enviado com sucesso, verifique sua caixa de entrada ou spam", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(requireContext(), "digite seu email", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}