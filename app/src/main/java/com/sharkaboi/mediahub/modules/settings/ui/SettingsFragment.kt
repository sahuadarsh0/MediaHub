package com.sharkaboi.mediahub.modules.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sharkaboi.mediahub.BuildConfig
import com.sharkaboi.mediahub.R
import com.sharkaboi.mediahub.common.constants.AppConstants
import com.sharkaboi.mediahub.common.data.sharedpref.SharedPreferencesKeys
import com.sharkaboi.mediahub.common.extensions.showToast
import com.sharkaboi.mediahub.modules.auth.OAuthActivity
import com.sharkaboi.mediahub.modules.settings.vm.SettingsStates
import com.sharkaboi.mediahub.modules.settings.vm.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val settingsViewModel by viewModels<SettingsViewModel>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setData()
        setListeners()
        setObservers()
    }

    private fun setData() {
        findPreference<Preference>(SharedPreferencesKeys.ABOUT)?.summary =
            "v${BuildConfig.VERSION_NAME}"
    }

    private fun setListeners() {
        findPreference<SwitchPreferenceCompat>(SharedPreferencesKeys.DARK_MODE)?.setOnPreferenceChangeListener { _, newValue ->
            setDefaultNightMode(
                if (newValue as Boolean) {
                    MODE_NIGHT_YES
                } else {
                    MODE_NIGHT_NO
                }
            )
            true
        }
        findPreference<Preference>(SharedPreferencesKeys.ABOUT)?.setOnPreferenceClickListener { _ ->
            showAboutDialog()
            true
        }
        findPreference<Preference>(SharedPreferencesKeys.LOG_OUT)?.setOnPreferenceClickListener { _ ->
            showLogOutDialog()
            true
        }
        findPreference<Preference>(SharedPreferencesKeys.ANIME_NOTIFS)?.setOnPreferenceClickListener { _ ->
            showAnimeNotificationsDialog()
            true
        }
    }

    private fun setObservers() {
        settingsViewModel.uiState.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is SettingsStates.LogOutFailure -> showToast(uiState.message)
                SettingsStates.LogOutSuccess -> moveToOAuthScreen()
                else -> Unit
            }
        }
    }

    private fun showAnimeNotificationsDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Anime notifications")
            .setMessage("This feature is coming soon!")
            .setPositiveButton("Pog") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showLogOutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Log out?")
            .setMessage("This is permanent and you have to log in again after to use MediaHub.")
            .setPositiveButton("Yes, Log me out") { _, _ ->
                settingsViewModel.logOutUser()
            }
            .setNegativeButton("No, take me back") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("About")
            .setMessage(AppConstants.description)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Github") { _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.githubLink)))
                activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
            .setNeutralButton("View licenses") { _, _ ->
                startActivity(Intent(context, OssLicensesMenuActivity::class.java))
                activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }.show()
    }

    private fun moveToOAuthScreen() {
        startActivity(Intent(context, OAuthActivity::class.java))
        activity?.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        activity?.finishAffinity()
    }
}