package dev.airon.bankfinance.presenter.auth.recover

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dev.airon.bankfinance.R
import dev.airon.bankfinance.databinding.FragmentRecoverBinding

private var _binding : FragmentRecoverBinding? = null
private val binding get() = _binding!!
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