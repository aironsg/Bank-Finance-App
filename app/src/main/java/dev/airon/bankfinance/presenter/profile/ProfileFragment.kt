package dev.airon.bankfinance.presenter.profile

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.databinding.FragmentProfileBinding
import dev.airon.bankfinance.util.FirebaseHelper
import dev.airon.bankfinance.util.StateView
import dev.airon.bankfinance.util.hideKeyboard
import dev.airon.bankfinance.util.showBottomSheet
import loadProfileImage
import saveProfileImage

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val profileViewModel: ProfileViewModel by viewModels()

    private var user: User? = null

    // Contrato para pegar imagem da galeria
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
            binding.imgProfile.setImageBitmap(bitmap)

            // Agora usa a extension para salvar globalmente
            requireContext().saveProfileImage(bitmap)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Exibir a imagem de perfil já salva
        binding.imgProfile.loadProfileImage(requireContext())

        getProfile()
        setupListeners()
    }

    private fun setupListeners() {
        // Selecionar nova imagem ao clicar
        binding.imgProfile.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            findNavController().navigate(R.id.action_profileFragment_to_authentication)
        }

        binding.btnEditName.setOnClickListener {
            toggleVisibility(binding.textEditName, binding.editName)
        }

        binding.btnEditPassword.setOnClickListener {
            toggleVisibility(binding.textEditPassword, binding.editLastPassword)
            toggleVisibility(binding.textEditNewPassword, binding.editNewPassword)
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateSaveButtonVisibility()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        binding.editName.addTextChangedListener(watcher)
        binding.editLastPassword.addTextChangedListener(watcher)
        binding.editNewPassword.addTextChangedListener(watcher)

        binding.btnSaveProfile.setOnClickListener {
            handleSave()
            getProfile()
        }
    }

    private fun handleSave() {
        val name = binding.editName.text.toString().trim()
        val lastPassword = binding.editLastPassword.text.toString().trim()
        val newPassword = binding.editNewPassword.text.toString().trim()

        if (user == null) return

        var updated = false

        if (binding.editName.isVisible && name.isNotEmpty()) {
            user?.name = name
            saveProfile()
            updated = true
        }

        if (binding.editLastPassword.isVisible && binding.editNewPassword.isVisible &&
            lastPassword.isNotEmpty() && newPassword.isNotEmpty()
        ) {
            if (newPassword.length < 6) {
                showBottomSheet(message = "A nova senha deve ter pelo menos 6 caracteres.")
                return
            }
            if (lastPassword == newPassword) {
                showBottomSheet(message = "A nova senha não pode ser igual à senha atual.")
                return
            }
            updatePasswordUser(lastPassword, newPassword)
            updated = true
        }

        if (!updated) {
            showBottomSheet(message = "Nenhuma alteração detectada.")
        }
    }

    private fun toggleVisibility(vararg views: View) {
        views.forEach {
            it.visibility = if (it.isVisible) View.GONE else View.VISIBLE
        }
    }

    private fun updateSaveButtonVisibility() {
        val nameValid = binding.editName.isVisible && binding.editName.text?.isNotBlank() == true
        val passwordValid = binding.editLastPassword.isVisible &&
                binding.editNewPassword.isVisible &&
                binding.editLastPassword.text?.isNotBlank() == true &&
                binding.editNewPassword.text?.isNotBlank() == true

        binding.btnSaveProfile.visibility = if (nameValid || passwordValid) View.VISIBLE else View.GONE
    }

    private fun getProfile() {
        profileViewModel.getProfile().observe(viewLifecycleOwner) { state ->
            when (state) {
                is StateView.Loading -> {}
                is StateView.Success -> {
                    user = state.data
                    showUserData()
                }
                is StateView.Error -> {
                    Toast.makeText(requireContext(), "Erro ao carregar perfil", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveProfile() {
        user?.let {
            profileViewModel.saveProfile(it).observe(viewLifecycleOwner) { state ->
                when (state) {
                    is StateView.Loading -> {}
                    is StateView.Success -> {
                        Toast.makeText(requireContext(), "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show()
                    }
                    is StateView.Error -> {
                        showBottomSheet(
                            message = getString(FirebaseHelper.validError(state.message ?: ""))
                        )
                    }
                }
            }
        }
    }

    private fun updatePasswordUser(currentPassword: String, newPassword: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val email = user?.email ?: return

        val credential = EmailAuthProvider.getCredential(email, currentPassword)

        currentUser?.reauthenticate(credential)
            ?.addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    currentUser.updatePassword(newPassword)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                Toast.makeText(
                                    requireContext(),
                                    "Senha atualizada com sucesso!\nSaia do aplicativo para uma nova autenticação!",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                showBottomSheet(
                                    message = getString(FirebaseHelper.validError(updateTask.exception?.message ?: ""))
                                )
                            }
                        }
                } else {
                    showBottomSheet(
                        message = getString(FirebaseHelper.validError(reauthTask.exception?.message ?: ""))
                    )
                }
            }
    }

    private fun showUserData() {
        binding.textUserName.text = user?.name
        binding.textUserMail.text = user?.email
        // Carregar a imagem de perfil sempre que atualizar os dados do usuário
        binding.imgProfile.loadProfileImage(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
