package co.thnki.brandfever.utils;

import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.LoginActivity;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.fcm.UpdateDeleteTokenAsyncTask;

import static co.thnki.brandfever.firebase.database.models.Accounts.USERS;
import static co.thnki.brandfever.firebase.fcm.NotificationInstanceIdService.NOTIFICATION_INSTANCE_ID;
import static co.thnki.brandfever.singletons.VolleyUtil.APP_ID;
import static co.thnki.brandfever.singletons.VolleyUtil.DEFAULT_URL;
import static co.thnki.brandfever.singletons.VolleyUtil.REQUEST_HANDLER_URL;

/**
 * 1. onLogin delete the old token
 * 2. save the account info to server
 * 3. onSave complete update the toke and save it back to server.
 */
public class UserUtil
{
    public static final String APP_DATA = "appData";
    public static final String USER_LIST = "users";
    public static final String OWNER_PHONE_NUMBER = "phoneNumber";
    private static UserUtil sInstance;
    private Accounts mAccount;
    private SharedPreferences mPreferences;

    private UserUtil()
    {
        mPreferences = Brandfever.getPreferences();
        mAccount = new Accounts();
    }

    public static UserUtil getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new UserUtil();
        }
        return sInstance;
    }

    private DatabaseReference getDbReference()
    {
        DatabaseReference databaseReference = null;
        if (mAccount.googleId != null && !mAccount.googleId.isEmpty())
        {
            databaseReference = FirebaseDatabase.getInstance().getReference().child(USERS)
                    .child(mAccount.googleId);
        }
        return databaseReference;
    }

    public void updateToken()
    {
        new UpdateDeleteTokenAsyncTask().execute(true);
    }

    public void register(GoogleSignInAccount googleSignInAccount)
    {
        if (googleSignInAccount != null)
        {
            deleteToken();

            Uri photoUrl = googleSignInAccount.getPhotoUrl();
            mAccount.googleId = googleSignInAccount.getId();
            mAccount.email = googleSignInAccount.getEmail();
            mAccount.name = googleSignInAccount.getDisplayName();
            mAccount.photoUrl = photoUrl != null ? photoUrl.toString() : "";
            mPreferences.edit()
                    .putString(Accounts.NAME, mAccount.name)
                    .putBoolean(LoginActivity.LOGIN_STATUS, true)
                    .putString(Accounts.EMAIL, mAccount.email)
                    .putString(Accounts.PHOTO_URL, mAccount.photoUrl)
                    .putString(Accounts.GOOGLE_ID, mAccount.googleId)
                    .apply();

            updateRequestHandlerUrl();
            updateUserInfo();
            FavoritesUtil.getsInstance().updateFavoriteList();
            CartUtil.getsInstance().updateCartList();
        }
    }

    private void updateRequestHandlerUrl()
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child(APP_DATA);
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    String url = dataSnapshot.child(REQUEST_HANDLER_URL).getValue(String.class);
                    String serverKey = dataSnapshot.child(APP_ID).getValue(String.class);
                    if (url == null || url.isEmpty())
                    {
                        url = DEFAULT_URL;
                    }
                    Brandfever.getPreferences().edit()
                            .putString(APP_ID, serverKey)
                            .putString(REQUEST_HANDLER_URL, url).apply();
                }
                catch (Exception e)
                {
                    Log.d("Exception", e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void deleteToken()
    {
        String token = mPreferences.getString(NOTIFICATION_INSTANCE_ID, "");
        if (!token.isEmpty())
        {
            new UpdateDeleteTokenAsyncTask().execute(false);
        }
    }

    public Accounts getAccount()
    {
        return mAccount;
    }

    public void updateNotificationInstanceId(final String refreshedToken)
    {
        mPreferences.edit().putString(NOTIFICATION_INSTANCE_ID, refreshedToken).apply();

        String googleId = mPreferences.getString(Accounts.GOOGLE_ID, "");
        if (!googleId.isEmpty())
        {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(USERS)
                    .child(googleId);
            databaseReference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    try
                    {
                        mAccount = dataSnapshot.getValue(Accounts.class);
                        mAccount.fcmToken = refreshedToken;
                        updateUserInfo();
                        databaseReference.removeEventListener(this);
                    }
                    catch (Exception e)
                    {
                        Log.d("Exception", e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
    }

    private void updateUserInfo()
    {
        final DatabaseReference usersDbRef = getDbReference();
        if (usersDbRef != null)
        {
            usersDbRef.setValue(mAccount);
        }
    }

    public boolean isOwner(String googleId)
    {
        Set<String> ownersGid = mPreferences.getStringSet(Accounts.OWNERS_GOOGLE_IDS, new HashSet<String>());
        for (String gId : ownersGid)
        {
            if (googleId.equals(gId))
            {
                return true;
            }
        }
        return false;
    }
}