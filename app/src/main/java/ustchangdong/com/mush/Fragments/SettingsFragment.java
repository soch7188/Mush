package ustchangdong.com.mush.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import ustchangdong.com.mush.R;

/**
 * Created by ziwon on 2017. 10. 4..
 */

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{
    private static final String TAG = SettingsFragment.class.getSimpleName();

    SharedPreferences sharedPreferences;


    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        onSharedPreferenceChanged(sharedPreferences, getString(R.string.notification_new_post_key));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);

        if (preference instanceof SwitchPreference){
            if (preference.getKey().equals(getString(R.string.notificationOnOffKey))) {
                SwitchPreference sp = (SwitchPreference) preference;
                Log.i(TAG, "sp.isChecked(): " + sp.isChecked());
                if (sp.isChecked()) {
                    Log.i(TAG, "Preference is Enabled. Subscribing to topic: GENERAL");
                    FirebaseMessaging.getInstance().subscribeToTopic("general");
                } else {
                    Log.i(TAG, "Preference is Disabled. Unsubscribing from topic: GENERAL");
                    FirebaseMessaging.getInstance().unsubscribeFromTopic("general");
                }
            }
//            else if (preference.getKey().equals(R.string.notificationOnOffKey)){
//                if (preference.isEnabled()) {
//                    FirebaseMessaging.getInstance().subscribeToTopic("general");
//                } else {
//                    FirebaseMessaging.getInstance().unsubscribeFromTopic("general");
//                }
//            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}