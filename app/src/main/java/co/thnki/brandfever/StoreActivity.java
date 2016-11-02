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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.fragments.CartFragment;
import co.thnki.brandfever.fragments.EditAddressDialogFragment;
import co.thnki.brandfever.fragments.FavoritesFragment;
import co.thnki.brandfever.fragments.MainPageFragment;
import co.thnki.brandfever.fragments.ProductsFragment;
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
    @Bind(R.id.content_main)
    RelativeLayout mContainer;
    private FragmentManager mFragmentManager;
    private NavigationDrawerUtil mNavigationDrawerUtil;

    @Bind(R.id.title)
    TextView mTitle;


    @Bind(R.id.cartCount)
    TextView mCartCount;

    @Bind(R.id.favCount)
    TextView mFavCount;

    private String mCurrentFragment = "";
    private String mGoogleId;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21)
        {
            getWindow().setSharedElementExitTransition(TransitionInflater.from(this)
                    .inflateTransition(R.transition.shared_product_transition));
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this)
                    .inflateTransition(R.transition.shared_product_transition));
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        updateUserDetails();
        setContentView(R.layout.activity_store);
        ButterKnife.bind(this);
        Otto.register(this);
        mFragmentManager = getSupportFragmentManager();
        mNavigationDrawerUtil = new NavigationDrawerUtil(mFragmentManager, this);
        mNavigationDrawerUtil.onFirstLevelItemClick(AVAILABLE_FIRST_LEVEL_CATEGORIES, ENTER);
        addMainPageFragment();
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
                Log.d("CurrentFrag", "mCurrentFragment : " + mCurrentFragment);
            }
        });

        SharedPreferences mPreferences = Brandfever.getPreferences();
        mGoogleId = mPreferences.getString(Accounts.GOOGLE_ID, "dummyId");
        registerCountUpdateListener();
    }

    private void updateUserDetails()
    {
        UserUtil.getInstance().updateToken();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child(Accounts.OWNERS);
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                SharedPreferences preferences = Brandfever.getPreferences();
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : children)
                {
                    String emailId = snapshot.getValue(String.class);
                    if(preferences.getString(Accounts.EMAIL, "").equals(emailId))
                    {
                        preferences.edit().putBoolean(Accounts.IS_OWNER, true).apply();
                        return;
                    }
                }
                preferences.edit().putBoolean(Accounts.IS_OWNER, false).apply();
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
        DatabaseReference cartDbRef = FirebaseDatabase.getInstance().getReference()
                .child(mGoogleId).child(CART_LIST);

        DatabaseReference favDbRef = FirebaseDatabase.getInstance().getReference()
                .child(mGoogleId).child(FAV_LIST);

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

    private void addMainPageFragment()
    {
        Fragment fragment = new MainPageFragment();
        mFragmentManager.beginTransaction()
                .replace(R.id.content_main, fragment)
                .commit();
    }

    @Override
    public void onBackPressed()
    {
        if (mNavigationDrawerUtil.onBackPressed())
        {
            super.onBackPressed();
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
            Brandfever.getPreferences().edit()
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
        if (ConnectivityUtil.isConnected())
        {
            if (!mCurrentFragment.equals(tag))
            {
                boolean popped = mFragmentManager.popBackStackImmediate(tag, 0);
                Fragment fragment;
                if (!popped)
                {
                    switch (tag)
                    {
                        case CartFragment.TAG:
                            fragment = new CartFragment();
                            break;
                        case FavoritesFragment.TAG:
                            fragment = new FavoritesFragment();
                            break;
                        default:
                            fragment = ProductsFragment.getInstance(tag);
                            break;
                    }
                    mFragmentManager.beginTransaction()
                            .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_top,
                                    R.anim.enter_from_top, R.anim.exit_to_bottom)
                            .replace(R.id.content_main, fragment)
                            .addToBackStack(tag)
                            .commit();
                }
            }
        }
        else
        {
            toast(R.string.noInternet);
        }
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

    public void editAddress(View view)
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
        }
        else
        {
            toast(R.string.noInternet);
        }
    }

    public void myOrders(View view)
    {
        if (ConnectivityUtil.isConnected())
        {
            toast("my orders");
        }
        else
        {
            toast(R.string.noInternet);
        }
    }
}