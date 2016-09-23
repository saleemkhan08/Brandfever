package co.thnki.brandfever;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import co.thnki.brandfever.interfaces.ConnectivityListener;
import co.thnki.brandfever.pojos.Accounts;
import co.thnki.brandfever.utils.ConnectivityUtil;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, ConnectivityListener
{
    private static final String TAG = "loginActivity";
    public static final String LOG_OUT = "logOut";
    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences mPreference;
    private static final int REQUEST_CODE_GOOGLE_PLAY_SERVICES = 198;
    private static final int REQUEST_CODE_GET_TOKEN = 199;
    private static final String LOGIN_STATUS = "login_status";
    private ProgressDialog mProgressDialog;
    private boolean mIsFetched;

    // [START declare_auth]
    private FirebaseAuth mAuth;
    private boolean mIsLogOut;
    // [END declare_auth]

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        mPreference = Brandfever.getPreferences();
        setContentView(R.layout.activity_login);
        TextView title = (TextView) findViewById(R.id.title);
        title.setTypeface(Brandfever.getTypeFace());

        TextView description = (TextView) findViewById(R.id.descriptionText);
        description.setTypeface(Brandfever.getTypeFace());

        checkGooglePlayServices();

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        mIsLogOut = getIntent().hasExtra(LOG_OUT);
    }

    private void checkGooglePlayServices()
    {
        Log.d(TAG, "checkGooglePlayServices");
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int code = api.isGooglePlayServicesAvailable(this);
        if (code == ConnectionResult.SUCCESS)
        {
            onActivityResult(REQUEST_CODE_GOOGLE_PLAY_SERVICES, Activity.RESULT_OK, null);
        }
        else if (api.isUserResolvableError(code))
        {
            api.showErrorDialogFragment(this, code, REQUEST_CODE_GOOGLE_PLAY_SERVICES);
        }
        else
        {
            Brandfever.toast(api.getErrorString(code));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG, "onActivityResult");
        switch (requestCode)
        {
            case REQUEST_CODE_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK)
                {
                    if (mPreference.getBoolean(LOGIN_STATUS, false))
                    {
                        launchMainActivity();
                    }
                    else
                    {
                        setupLogin();
                    }
                }
                break;
            case REQUEST_CODE_GET_TOKEN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                Log.d(TAG, "REQUEST_CODE_GET_TOKEN");
                if (result.isSuccess())
                {
                    GoogleSignInAccount account = result.getSignInAccount();
                    firebaseAuthWithGoogle(account);
                }
                else
                {
                    loginFailed();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void setupLogin()
    {
        Log.d(TAG, "setUpLogin");
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build GoogleAPIClient with the Google Sign-In API and the above options.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mGoogleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks()
        {
            @Override
            public void onConnected(@Nullable Bundle bundle)
            {
                if(mIsLogOut)
                {

                    revokeAccess();
                    signOut();
                }
            }

            @Override
            public void onConnectionSuspended(int i)
            {

            }
        });

        SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setVisibility(View.VISIBLE);
        signInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "setUpLogin : onClick");
                ConnectivityUtil.isConnected(LoginActivity.this, LoginActivity.this);
            }
        });
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());
    }

    private void signIn()
    {
        Log.d(TAG, "signIn");
        revokeAccess();
        signOut();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE_GET_TOKEN);
        showProgressDialog(getString(R.string.signing_in));
    }

    private void showProgressDialog(String msg)
    {
        Log.d(TAG, "showProgressDialog");
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog()
    {
        Log.d(TAG, "hideProgressDialog");
        if (mProgressDialog != null && mProgressDialog.isShowing())
        {
            mProgressDialog.hide();
        }
    }

    private void loginFailed()
    {
        hideProgressDialog();
        Brandfever.toast(getString(R.string.something_went_wrong));
        Brandfever.toast(getString(R.string.please_try_again));
        signOut();
        revokeAccess();

    }

    @Override
    public void onInternetConnected()
    {
        if (!mIsFetched)
        {

        }
        signIn();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        hideProgressDialog();
    }

    private void signOut()
    {
        if (mGoogleApiClient.isConnected())
        {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>()
                    {
                        @Override
                        public void onResult(@NonNull Status status)
                        {
                            Log.d(TAG, "signOut:onResult:" + status);
                        }
                    });
        }
    }

    private void revokeAccess()
    {
        if (mGoogleApiClient.isConnected())
        {
            Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>()
                    {
                        @Override
                        public void onResult(@NonNull Status status)
                        {
                            Log.d(TAG, "revokeAccess:onResult:" + status);
                        }
                    });
        }
    }

    private void launchMainActivity()
    {
        Log.d(TAG, "Launching MainActivity : through Handler");
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                Intent intent = new Intent(LoginActivity.this, StoreActivity.class);
                startActivity(intent);
                finish();
            }
        }, 500);
    }

    @Override
    public void onCancelled()
    {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        loginFailed();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        hideProgressDialog();
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount user)
    {
        Log.d(TAG, "firebaseAuthWithGoogle:" + user.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(user.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful())
                        {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            // User is signed in
                            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getId());
                            Uri photo_url = user.getPhotoUrl();
                            mPreference.edit()
                                    .putString(Accounts.NAME, user.getDisplayName())
                                    .putBoolean(LoginActivity.LOGIN_STATUS, true)
                                    .putString(Accounts.EMAIL, user.getEmail())
                                    .putString(Accounts.PHOTO_URL, photo_url != null ? photo_url.toString() : "")
                                    .putString(Accounts.GOOGLE_ID, user.getId())
                                    .apply();
                            Log.d(TAG, "startActivity : MainActivity");
                            startActivity(new Intent(LoginActivity.this, StoreActivity.class));
                            finish();
                        }
                        // ...
                    }
                });
    }
}