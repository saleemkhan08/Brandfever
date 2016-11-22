package co.thnki.brandfever;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.fragments.CartFragment;
import co.thnki.brandfever.fragments.EditAddressDialogFragment;
import co.thnki.brandfever.fragments.FavoritesFragment;
import co.thnki.brandfever.fragments.MainPageFragment;
import co.thnki.brandfever.fragments.NotificationListFragment;
import co.thnki.brandfever.fragments.ProductsFragment;
import co.thnki.brandfever.fragments.UserListFragment;
import co.thnki.brandfever.fragments.UsersOrdersFragment;
import co.thnki.brandfever.singletons.Otto;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.utils.NavigationDrawerUtil;
import co.thnki.brandfever.utils.UserUtil;

import static co.thnki.brandfever.Brandfever.toast;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_FIRST_LEVEL_CATEGORIES;
import static co.thnki.brandfever.interfaces.DrawerItemClickListener.ENTER;
import static co.thnki.brandfever.utils.CartUtil.CART_LIST;
import static co.thnki.brandfever.utils.FavoritesUtil.FAV_LIST;

public class StoreActivity extends AppCompatActivity
{
    private static final String TAG = "storeActivity";
    public static final String NOTIFICATION_ACTION = "";
    public static final String ORDER_RECEIVED = "orderReceived";
    public static final String ORDER_PLACED = "orderPlaced";
    public static final String ORDER_CANCELLED = "orderCancelled";
    public static final String OWNER_PROFILE_UPDATED = "ownerProfileUpdated";
    @Bind(R.id.content_main)
    RelativeLayout mContainer;
    private FragmentManager mFragmentManager;
    private NavigationDrawerUtil mNavigationDrawerUtil;
    private SharedPreferences mPreferences;

    @Bind(R.id.title)
    TextView mTitle;


    @Bind(R.id.cartCount)
    TextView mCartCount;

    @Bind(R.id.favCount)
    TextView mFavCount;

    private String mCurrentFragment = "";
    private String mGoogleId;
    private DatabaseReference mRootDbRef;

    @Bind(R.id.home)
    View mHome;

    @Bind(R.id.drawer)
    View mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setupWindowProperties();
        mPreferences = Brandfever.getPreferences();
        updateUserDetails();
        setContentView(R.layout.activity_store);
        ButterKnife.bind(this);
        Otto.register(this);
        mFragmentManager = getSupportFragmentManager();
        mNavigationDrawerUtil = new NavigationDrawerUtil(mFragmentManager, this);
        mNavigationDrawerUtil.onFirstLevelItemClick(AVAILABLE_FIRST_LEVEL_CATEGORIES, ENTER);

        mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener()
        {
            @Override
            public void onBackStackChanged()
            {
                int currentEntryCount = (mFragmentManager.getBackStackEntryCount());
                if (currentEntryCount > 0)
                {
                    mCurrentFragment = mFragmentManager.getBackStackEntryAt(currentEntryCount - 1).getName();
                }
                else
                {
                    mCurrentFragment = "";
                }
                Log.d("CurrentFrag", "Check - mCurrentFragment : " + mCurrentFragment);
            }
        });

        mGoogleId = mPreferences.getString(Accounts.GOOGLE_ID, "dummyId");
        registerCountUpdateListener();
        Intent intent = getIntent();
        if (intent.hasExtra(NOTIFICATION_ACTION))
        {
            switch (intent.getStringExtra(NOTIFICATION_ACTION))
            {
                default:
                    showNotification();
            }
        }
        else
        {
            addMainPageFragment();
        }


    }

    private void setupWindowProperties()
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            getWindow().setSharedElementExitTransition(TransitionInflater.from(this)
                    .inflateTransition(R.transition.shared_product_transition));
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this)
                    .inflateTransition(R.transition.shared_product_transition));
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void updateUserDetails()
    {
        mRootDbRef = FirebaseDatabase.getInstance().getReference();
        UserUtil.getInstance().updateToken();
        DatabaseReference databaseReference = mRootDbRef.child(Accounts.OWNERS);
        Log.d(TAG, "databaseReference : " + databaseReference);
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
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
                    final DatabaseReference usersDbRef = mRootDbRef.child(Accounts.USERS).child(googleId);
                    usersDbRef.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            Accounts ownerAccount = dataSnapshot.getValue(Accounts.class);
                            Set<String> ownerTokens = mPreferences.getStringSet(Accounts.OWNERS_TOKENS, new HashSet<String>());
                            Set<String> ownerGids = mPreferences.getStringSet(Accounts.OWNERS_GOOGLE_IDS, new HashSet<String>());

                            ownerTokens.add(ownerAccount.fcmToken);
                            ownerGids.add(ownerAccount.googleId);

                            mPreferences.edit()
                                    .putStringSet(Accounts.OWNERS_TOKENS, ownerTokens)
                                    .putStringSet(Accounts.OWNERS_GOOGLE_IDS, ownerGids)
                                    .apply();
                            Log.d(TAG, "ownerTokens : " + ownerTokens + ", ownerGids : " + ownerGids);
                            usersDbRef.removeEventListener(this);
                            Otto.post(OWNER_PROFILE_UPDATED);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }
                mPreferences.edit().putBoolean(Accounts.IS_OWNER, isOwner).apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void registerCountUpdateListener()
    {
        Log.d(CART_LIST, "googleId : " + mGoogleId);
        DatabaseReference cartDbRef = mRootDbRef.child(mGoogleId).child(CART_LIST);

        DatabaseReference favDbRef = mRootDbRef.child(mGoogleId).child(FAV_LIST);

        favDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                updateFavNo(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        cartDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                updateCartNo(dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void updateFavNo(long size)
    {
        if (size > 0)
        {
            mFavCount.setText(String.valueOf(size));
        }
        else
        {
            mFavCount.setText("");
        }
    }

    @OnClick(R.id.home)
    public void addMainPageFragment()
    {
        addFragment(MainPageFragment.TAG);
    }

    @Override
    public void onBackPressed()
    {
        if (mNavigationDrawerUtil.onBackPressed())
        {
            if (mHome.isShown())
            {
                super.onBackPressed();
            }
            else
            {
                finish();
            }
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }

    @OnClick(R.id.drawer)
    public void openDrawer()
    {
        mNavigationDrawerUtil.openDrawer();
    }

    public void logout(View view)
    {
        if (ConnectivityUtil.isConnected())
        {
            mPreferences.edit()
                    .putBoolean(LoginActivity.LOGIN_STATUS, false).apply();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else
        {
            toast(R.string.noInternet);
        }
    }

    public void setToolBarTitle(String title)
    {
        if (title.equals(getString(R.string.app_name)))
        {
            mHome.setVisibility(View.GONE);
            mDrawer.setVisibility(View.VISIBLE);
        }
        else
        {
            mHome.setVisibility(View.VISIBLE);
            mDrawer.setVisibility(View.GONE);
        }

        if (mTitle != null)
        {
            mTitle.setText(title);
        }
    }

    @OnClick(R.id.favorite)
    public void showFavoriteList()
    {
        addFragment(FavoritesFragment.TAG);
    }

    @OnClick(R.id.cart)
    public void showCartList()
    {
        addFragment(CartFragment.TAG);
    }

    public void addFragment(String tag)
    {
        Fragment fragment;
        if (isNewObjectRequired(tag))
        {
            switch (tag)
            {
                case CartFragment.TAG:
                    fragment = mFragmentManager.findFragmentByTag(CartFragment.TAG);
                    if (fragment == null)
                    {
                        fragment = new CartFragment();
                    }
                    break;
                case FavoritesFragment.TAG:
                    fragment = mFragmentManager.findFragmentByTag(FavoritesFragment.TAG);
                    if (fragment == null)
                    {
                        fragment = new FavoritesFragment();
                    }
                    break;
                case MainPageFragment.TAG:
                    fragment = mFragmentManager.findFragmentByTag(MainPageFragment.TAG);
                    if (fragment == null)
                    {
                        fragment = new MainPageFragment();
                    }
                    break;
                default:
                    fragment = ProductsFragment.getInstance(tag);
                    break;
            }
            animateFragment(fragment, tag);
        }
    }

    private void animateFragment(Fragment fragment, String tag)
    {
        int enterTransition = R.anim.enter_directly;
        if (tag.equals(MainPageFragment.TAG))
        {
            mHome.setVisibility(View.GONE);
            mDrawer.setVisibility(View.VISIBLE);
        }
        else
        {
            enterTransition = R.anim.enter_from_bottom;
        }
        mFragmentManager.beginTransaction()
                .setCustomAnimations(enterTransition, R.anim.exit_to_top,
                        R.anim.enter_from_top, R.anim.exit_to_bottom)
                .replace(R.id.content_main, fragment)
                .addToBackStack(tag)
                .commit();
    }

    public void updateCartNo(long size)
    {
        if (size > 0)
        {
            mCartCount.setText(String.valueOf(size));
        }
        else
        {
            mCartCount.setText("");
        }
    }

    public void showNotification(View view)
    {
        showNotification();
    }

    private void showNotification()
    {
        if (isNewObjectRequired(NotificationListFragment.TAG))
        {
            Fragment fragment = mFragmentManager.findFragmentByTag(NotificationListFragment.TAG);
            if (fragment == null)
            {
                fragment = new NotificationListFragment();
            }
            animateFragment(fragment, NotificationListFragment.TAG);
        }
        mNavigationDrawerUtil.closeDrawer();
    }

    public void myOrders(View view)
    {
        showOrdersFragment();
    }

    private void showOrdersFragment()
    {
        if (ConnectivityUtil.isConnected())
        {
            showUserOrdersFragment(mPreferences.getString(Accounts.GOOGLE_ID, ""));
            mNavigationDrawerUtil.closeDrawer();
        }
        else
        {
            toast(R.string.noInternet);
        }
    }

    public void showAddressEditorFragment()
    {
        if (ConnectivityUtil.isConnected())
        {
            EditAddressDialogFragment fragment = (EditAddressDialogFragment) mFragmentManager
                    .findFragmentByTag(EditAddressDialogFragment.TAG);

            if (fragment == null)
            {
                fragment = new EditAddressDialogFragment();
            }
            fragment.show(mFragmentManager, EditAddressDialogFragment.TAG);

            mNavigationDrawerUtil.closeDrawer();
        }
        else
        {
            toast(R.string.noInternet);
        }
    }

    public void userList(View view)
    {
        if (isNewObjectRequired(UserListFragment.TAG))
        {
            Fragment fragment = mFragmentManager.findFragmentByTag(UserListFragment.TAG);
            if (fragment == null)
            {
                fragment = new UserListFragment();
            }
            animateFragment(fragment, UserListFragment.TAG);
        }
        mNavigationDrawerUtil.closeDrawer();
    }

    public void showUserOrdersFragment(String googleId)
    {
        if (isNewObjectRequired(UsersOrdersFragment.TAG))
        {
            Fragment fragment = mFragmentManager.findFragmentByTag(UsersOrdersFragment.TAG);
            if (fragment == null)
            {
                fragment = UsersOrdersFragment.getInstance(googleId);
            }
            animateFragment(fragment, UsersOrdersFragment.TAG);
        }
        mNavigationDrawerUtil.closeDrawer();
    }

    private boolean isNewObjectRequired(String tag)
    {
        if (ConnectivityUtil.isConnected())
        {
            if (!mCurrentFragment.equals(tag))
            {
                boolean popped = mFragmentManager.popBackStackImmediate(tag, 0);
                if (!popped)
                {
                    return true;
                }
            }
        }
        else
        {
            toast(R.string.noInternet);
        }
        return false;
    }
}