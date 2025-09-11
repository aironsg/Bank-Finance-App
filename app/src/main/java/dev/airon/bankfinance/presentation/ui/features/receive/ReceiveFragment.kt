package dev.airon.bankfinance.presentation.ui.features.receive

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.databinding.FragmentReceiveBinding
import dev.airon.bankfinance.core.extensions.initToolbar
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.QRCodeGenerator

@AndroidEntryPoint
class ReceiveFragment : Fragment() {

    private var _binding: FragmentReceiveBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar, isToolbarDefaultColor = true)

        setupQRCode()
        setupCopyButton()
    }

    private fun setupQRCode() {
        val user = FirebaseHelper.getUser()

        val pixData = user?.email.toString()

        val qrBitmap = QRCodeGenerator.generate(pixData)
        binding.ivQRCode.setImageBitmap(qrBitmap)

        // Guardamos o payload para copiar no botão
        binding.btnCopyAndPaste.tag = pixData
    }

    private fun setupCopyButton() {
        binding.btnCopyAndPaste.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Pix Code", binding.btnCopyAndPaste.tag.toString())
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Chave Pix copiada com sucesso!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
