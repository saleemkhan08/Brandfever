package co.thnki.brandfever.utils;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.interfaces.IOnTokensUpdatedListener;
import co.thnki.brandfever.singletons.Otto;

import static co.thnki.brandfever.StoreActivity.OWNER_PROFILE_UPDATED;

public class FcmTokensUtil
{
    private static final String TAG = "FcmTokensUtil";
    private static FcmTokensUtil mFcmTokensUtil;
    private DatabaseReference mRootDbRef;
    private SharedPreferences mPreferences;
    private Map<String, String> mTokensMap;

    private FcmTokensUtil()
    {
        mRootDbRef = FirebaseDatabase.getInstance().getReference();
        mPreferences = Brandfever.getPreferences();
    }

    public static FcmTokensUtil getInstance()
    {
        if (mFcmTokensUtil == null)
        {
            mFcmTokensUtil = new FcmTokensUtil();
        }
        return mFcmTokensUtil;
    }

    void updateAllTokens(final String googleId, final IOnTokensUpdatedListener iOnTokensUpdatedListener)
    {
        Log.d(TAG, "updateAllOwnersTokens : OWNERS_TOKENS");
        DatabaseReference databaseReference = mRootDbRef.child(Accounts.OWNERS);
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    ArrayList<String> googleIds = getGids(dataSnapshot, googleId);
                    int size = googleIds.size();
                    mTokensMap = new HashMap<>();
                    for (int index = 0; index < size; index++)
                    {
                        boolean isLast = index < (size - 1);
                        String gId = googleIds.get(index);
                        updateToken(gId, isLast, index, iOnTokensUpdatedListener);
                        index++;
                    }
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

    private ArrayList<String> getGids(DataSnapshot dataSnapshot, String googleId)
    {
        ArrayList<String> gIds = new ArrayList<>();
        addToArrayList(googleId, gIds);

        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
        HashSet<String> gIdSet = new HashSet<>();
        for (DataSnapshot snapshot : children)
        {
            String gId = "";
            try
            {
                gId = snapshot.getValue(String.class);
                gIdSet.add(gId);
            }
            catch (Exception e)
            {
                Log.d(TAG, e.getMessage());
                return gIds;
            }
            addToArrayList(gId, gIds);
        }
        mPreferences.edit()
                .putStringSet(Accounts.OWNERS_GOOGLE_IDS, gIdSet)
                .commit();
        return gIds;
    }

    private ArrayList<String> addToArrayList(String googleId, ArrayList<String> gIds)
    {
        if (googleId != null && !googleId.equals(mPreferences.getString(Accounts.GOOGLE_ID, "")))
        {
            gIds.add(googleId);
        }
        return gIds;
    }

    private void updateToken(String googleId, final boolean isLast, final long index,
                             final IOnTokensUpdatedListener iOnTokensUpdatedListener)
    {
        final DatabaseReference usersDbRef = mRootDbRef.child(Accounts.USERS).child(googleId);

        usersDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    Accounts account = dataSnapshot.getValue(Accounts.class);
                    mTokensMap.put("" + index, account.fcmToken);
                    if (isLast)
                    {
                        iOnTokensUpdatedListener.onTokensUpdated(mTokensMap);
                    }
                    usersDbRef.removeEventListener(this);
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

    public void updateIsOwner()
    {
        DatabaseReference databaseReference = mRootDbRef.child(Accounts.OWNERS);
        Log.d(TAG, "databaseReference : " + databaseReference);
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    boolean isOwner = false;
                    for (DataSnapshot snapshot : children)
                    {
                        String googleId = "";
                        try
                        {
                            googleId = snapshot.getValue(String.class);
                        }
                        catch (Exception e)
                        {
                            return;
                        }
                        if (mPreferences.getString(Accounts.GOOGLE_ID, "").equals(googleId))
                        {
                            isOwner = true;
                        }
                    }
                    mPreferences.edit().putBoolean(Accounts.IS_OWNER, isOwner).apply();
                    Otto.post(OWNER_PROFILE_UPDATED);
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
