package ustchangdong.com.mush;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import ustchangdong.com.mush.Fragments.AllFragment;
import ustchangdong.com.mush.Fragments.FoodFragment;
import ustchangdong.com.mush.Fragments.MarketFragment;
import ustchangdong.com.mush.Fragments.PostingFragment;
import ustchangdong.com.mush.Fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity implements AllFragment.OnFragmentInteractionListener {
    private static final String TAG = "MainActivity";
    final Context context = this;

    // A Native Express ad is placed in every nth position in the RecyclerView.
    public static final int ITEMS_PER_AD = 6;

    // The Native Express ad unit ID.
    public static final String AD_UNIT_ID = "ca-app-pub-6525167222338120/6384998774";

    private PostingFragment postingFragment;
    private MarketFragment marketFragment;
    private FoodFragment foodFragment;
    private SettingsFragment settingsFragment;

    public static FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    public static DatabaseReference mDatabase;

    public static FloatingActionButton fab;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_posting:
                    transaction.replace(R.id.fragment_container_main, postingFragment);
                    transaction.commit();
                    getSupportFragmentManager().executePendingTransactions();
                    return true;
                case R.id.navigation_food:
                    transaction.replace(R.id.fragment_container_main, foodFragment);
                    transaction.commit();
                    getSupportFragmentManager().executePendingTransactions();
                    return true;
                case R.id.navigation_market:
                    transaction.replace(R.id.fragment_container_main, marketFragment);
                    transaction.commit();
                    getSupportFragmentManager().executePendingTransactions();
                    return true;
                case R.id.navigation_settings:
                    transaction.replace(R.id.fragment_container_main, settingsFragment);
                    transaction.commit();
                    getSupportFragmentManager().executePendingTransactions();
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, AD_UNIT_ID);

        // Subscribe to FCM Topic
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean notifOnOff = sharedPref.getBoolean(getString(R.string.notificationOnOffKey), true);
        Log.i(TAG, String.valueOf(notifOnOff));

        if (notifOnOff){
            FirebaseMessaging.getInstance().subscribeToTopic("general");
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("general");
        }

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            signInAnonymously(savedInstanceState);
        } else {
            populateFragments(savedInstanceState);
        }

    }

    private void populateFragments(Bundle savedInstanceState){
        postingFragment = new PostingFragment();
        foodFragment = new FoodFragment();
        marketFragment = new MarketFragment();
        settingsFragment = new SettingsFragment();

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container_main) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_main, postingFragment).commit();
        }
    }
  
    private void signInAnonymously(final Bundle savedInstanceState) {
        showProgressDialog();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            populateFragments(savedInstanceState);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    @VisibleForTesting
    public ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {
    }
}