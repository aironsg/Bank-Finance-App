import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.ImageView
import java.io.ByteArrayOutputStream

// Nome do arquivo de preferences
private const val PREFS_NAME = "app_prefs"
private const val KEY_PROFILE_IMAGE = "profile_image"

// Salvar a imagem (Bitmap -> Base64 -> SharedPreferences) atrelada ao userId
fun Context.saveProfileImage(bitmap: Bitmap, userId: String) {
    val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val base64 = bitmapToBase64(bitmap)
    prefs.edit().putString("${KEY_PROFILE_IMAGE}_$userId", base64).apply()
}

// Carregar e aplicar direto em um ImageView se existir imagem para o userId
fun ImageView.loadProfileImage(context: Context, userId: String) {
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.getString("${KEY_PROFILE_IMAGE}_$userId", null)?.let { base64 ->
        val bitmap = base64ToBitmap(base64)
        this.setImageBitmap(bitmap)
    }
}

// Remover a imagem de perfil de um usuário específico
fun Context.clearProfileImage(userId: String) {
    val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().remove("${KEY_PROFILE_IMAGE}_$userId").apply()
}

// Converter Bitmap -> Base64
private fun bitmapToBase64(bitmap: Bitmap): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val bytes = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(bytes, Base64.DEFAULT)
}

// Converter Base64 -> Bitmap
private fun base64ToBitmap(base64: String): Bitmap {
    val bytes = Base64.decode(base64, Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}
