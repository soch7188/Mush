package ustchangdong.com.mush;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements LoveFragment.OnFragmentInteractionListener,
        StudyFragment.OnFragmentInteractionListener, AllFragment.OnFragmentInteractionListener {
    private static final String TAG = "MainActivity";
    final Context context = this;

    // A Native Express ad is placed in every nth position in the RecyclerView.
    public static final int ITEMS_PER_AD = 6;

    // The Native Express ad height.
    public static final int NATIVE_EXPRESS_AD_HEIGHT = 180;

    // The Native Express ad unit ID.
    public static final String AD_UNIT_ID = "ca-app-pub-6525167222338120/6384998774";

    private static LoveFragment loveFrag;
    private static StudyFragment studyFrag;
    private static AllFragment allFrag;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference mDatabase;
    private DatabaseReference postLoveRef;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mDatabase = FirebaseDatabase.getInstance().getReference();
        postLoveRef = FirebaseDatabase.getInstance().getReference("love");
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_love:
                    transaction.replace(R.id.fragment_container_main, loveFrag);
                    transaction.commit();
                    getSupportFragmentManager().executePendingTransactions();
                    return true;
                case R.id.navigation_study:
                    transaction.replace(R.id.fragment_container_main, studyFrag);
                    transaction.commit();
                    getSupportFragmentManager().executePendingTransactions();
                    return true;
                case R.id.navigation_all:
                    transaction.replace(R.id.fragment_container_main, allFrag);
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

        setFloatingActionButton();
        MobileAds.initialize(this, AD_UNIT_ID);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            signInAnonymously(savedInstanceState);
        } else {
            populateFragments(savedInstanceState);
        }

//        if (!(currentUser == null)) {
//            populateFragments(savedInstanceState);
//        } else {
//            Snackbar.make(findViewById(R.id.placeSnackBar), "There was an error with authentication. Please restart", Snackbar.LENGTH_INDEFINITE).show();
//        }

    }

    private void populateFragments(Bundle savedInstanceState){
        loveFrag = new LoveFragment();
        studyFrag = new StudyFragment();
        allFrag = new AllFragment();

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
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container_main, loveFrag).commit();
        }
    }

    private void setFloatingActionButton(){
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_post);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.dialog_add_post, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText editTextTitle = promptsView.findViewById(R.id.editTextTitle);
                final EditText editTextContent = promptsView.findViewById(R.id.editTextContent);
                final RadioGroup radioGroupCategory = promptsView.findViewById(R.id.rg_category);

                try {
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            String title = editTextTitle.getText().toString();
                                            String content = editTextContent.getText().toString();
                                            writeNewPost(mAuth.getCurrentUser().getUid(), title, content, radioGroupCategory.getCheckedRadioButtonId());
                                            Snackbar.make(findViewById(R.id.placeSnackBar), "Post Successfully Added", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    })
                            .create()
                            .show();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void writeNewPost(String userId, String title, String content, int radioGroupCategory) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        String key = mDatabase.push().getKey();
        Post post = new Post(userId, title, content);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();

        switch(radioGroupCategory) {
            case R.id.rb_love:
                childUpdates.put("/love/" + key, postValues);
                childUpdates.put("/user-love/" + userId + "/" + key, postValues);
                break;
            case R.id.rb_study:
                childUpdates.put("/study/" + key, postValues);
                childUpdates.put("/user-study/" + userId + "/" + key, postValues);
                break;
            case R.id.rb_all:
                childUpdates.put("/all/" + key, postValues);
                childUpdates.put("/user-all/" + userId + "/" + key, postValues);
                break;
        }

        mDatabase.updateChildren(childUpdates);
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
