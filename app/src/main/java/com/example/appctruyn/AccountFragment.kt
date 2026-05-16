package com.example.appctruyn

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.appctruyn.auth.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

class AccountFragment : Fragment() {

    private lateinit var tvCurrentTheme: TextView
    private lateinit var ivAvatar: ImageView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            processAndUploadAvatar(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName      = view.findViewById<TextView>(R.id.tvUserName)
        val tvEmail     = view.findViewById<TextView>(R.id.tvUserEmail)
        val tvRoleBadge = view.findViewById<TextView>(R.id.tvRoleBadge)
        val layoutAuthorMenu = view.findViewById<LinearLayout>(R.id.layoutAuthorMenu)
        val layoutAdminMenu  = view.findViewById<LinearLayout>(R.id.layoutAdminMenu)
        val btnLogout   = view.findViewById<Button>(R.id.btnLogout)
        ivAvatar        = view.findViewById(R.id.ivAvatar)
        val btnEditAvatar = view.findViewById<View>(R.id.btnEditAvatar)
        
        tvCurrentTheme = view.findViewById(R.id.tvCurrentTheme)
        val btnThemeMode = view.findViewById<View>(R.id.btnThemeMode)

        updateThemeText()

        btnThemeMode.setOnClickListener {
            showThemeSelectionDialog()
        }

        btnEditAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        lifecycleScope.launch {
            val user = AuthManager.getCurrentUserInfo()
            if (user != null) {
                tvName.text  = user.displayName.ifEmpty { getString(R.string.user_default_name) }
                tvEmail.text = user.email

                loadAvatar(user.avatarUrl)

                when (user.role) {
                    "author" -> {
                        tvRoleBadge.text = getString(R.string.role_author_label)
                        tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_author)
                        layoutAuthorMenu.visibility = View.VISIBLE
                        layoutAdminMenu.visibility  = View.GONE
                    }
                    "admin" -> {
                        tvRoleBadge.text = getString(R.string.role_admin_label)
                        tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_admin)
                        layoutAuthorMenu.visibility = View.VISIBLE
                        layoutAdminMenu.visibility  = View.VISIBLE
                    }
                    else -> {
                        tvRoleBadge.text = getString(R.string.role_reader_label)
                        tvRoleBadge.setBackgroundResource(R.drawable.bg_badge_reader)
                        layoutAuthorMenu.visibility = View.GONE
                        layoutAdminMenu.visibility  = View.GONE
                    }
                }
            }
        }

        view.findViewById<View>(R.id.btnManageUsers)?.setOnClickListener {
            startActivity(Intent(requireContext(), AdminManageUsersActivity::class.java))
        }

        view.findViewById<View>(R.id.btnMyStories)?.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.manage_stories_coming_soon), Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnAddStory)?.setOnClickListener {
            Toast.makeText(requireContext(), getString(R.string.add_story_coming_soon), Toast.LENGTH_SHORT).show()
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.logout))
                .setMessage(getString(R.string.logout_confirm))
                .setPositiveButton(getString(R.string.logout)) { _, _ ->
                    AuthManager.logout()
                    startActivity(Intent(requireContext(), LoginActivity::class.java))
                    requireActivity().finishAffinity()
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
    }

    private fun loadAvatar(avatarData: String?) {
        if (avatarData == null) {
            ivAvatar.setImageResource(R.drawable.ic_nav_account)
            return
        }

        if (avatarData.startsWith("data:image") || isBase64(avatarData)) {
            val cleanBase64 = if (avatarData.contains(",")) avatarData.split(",")[1] else avatarData
            try {
                val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Glide.with(this).load(decodedImage).circleCrop().into(ivAvatar)
            } catch (e: Exception) {
                ivAvatar.setImageResource(R.drawable.ic_nav_account)
            }
        } else {
            Glide.with(this).load(avatarData).placeholder(R.drawable.ic_nav_account).circleCrop().into(ivAvatar)
        }
    }

    private fun isBase64(s: String): Boolean {
        return try {
            Base64.decode(s, Base64.DEFAULT)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun processAndUploadAvatar(uri: Uri) {
        lifecycleScope.launch {
            try {
                val base64String = withContext(Dispatchers.IO) {
                    uriToBase64(uri)
                }
                
                if (base64String != null) {
                    AuthManager.updateAvatarBase64(base64String).onSuccess {
                        loadAvatar(base64String)
                        Toast.makeText(requireContext(), getString(R.string.update_avatar_success), Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        val errorMsg = getString(R.string.update_avatar_fail, it.message)
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), getString(R.string.err_unknown), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val resizedBitmap = resizeBitmap(bitmap, 400)
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()
            "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            null
        }
    }

    private fun resizeBitmap(source: Bitmap, maxLength: Int): Bitmap {
        val outWidth: Int
        val outHeight: Int
        val inWidth = source.width
        val inHeight = source.height
        if (inWidth > inHeight) {
            outWidth = maxLength
            outHeight = (inHeight.toFloat() / inWidth * maxLength).toInt()
        } else {
            outHeight = maxLength
            outWidth = (inWidth.toFloat() / inHeight * maxLength).toInt()
        }
        return Bitmap.createScaledBitmap(source, outWidth, outHeight, false)
    }

    private fun updateThemeText() {
        val currentMode = ThemeHelper.getThemePreference(requireContext())
        tvCurrentTheme.text = ThemeHelper.getThemeName(requireContext(), currentMode)
    }

    private fun showThemeSelectionDialog() {
        val options = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_auto)
        )
        val currentMode = ThemeHelper.getThemePreference(requireContext())
        
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.theme_selection))
            .setSingleChoiceItems(options, currentMode) { dialog, which ->
                ThemeHelper.setThemePreference(requireContext(), which)
                updateThemeText()
                dialog.dismiss()
                requireActivity().recreate()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
