package dev.airon.bankfinance.presentation.ui.features.transfer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import dev.airon.bankfinance.databinding.FragmentTransferBinding
import dev.airon.bankfinance.presentation.auth.login.LoginViewModel
import kotlin.getValue

import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.core.extensions.initToolbar

@AndroidEntryPoint
class TransferFragment : Fragment() {
    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar, isToolbarDefaultColor = true)
    }




    // Inicialize o ScanContract no onCreate() ou no escopo de classe
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            // O usuário cancelou o scan
            Toast.makeText(requireContext(), "Leitura cancelada", Toast.LENGTH_SHORT).show()
        } else {
            // Leitura bem-sucedida, `result.contents` contém o conteúdo do QR Code
            val qrCodeContent = result.contents
            Toast.makeText(requireContext(), "Conteúdo: $qrCodeContent", Toast.LENGTH_LONG).show()
        }
    }


    fun startQrCodeScanner() {
        //esta função é chamada ao clicar no botão de escanear QR code
        val options = ScanOptions()
        options.setPrompt("Aponte a câmera para o QR Code")
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setCameraId(0) // Use a câmera traseira
        options.setBeepEnabled(true) // Reproduzir som ao ler
        options.setBarcodeImageEnabled(true) // Retornar a imagem do QR Code

        barcodeLauncher.launch(options)
    }



    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
