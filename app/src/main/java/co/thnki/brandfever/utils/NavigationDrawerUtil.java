package co.thnki.brandfever.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Stack;

import co.thnki.brandfever.R;
import co.thnki.brandfever.fragments.CategoryDrawerFragment;
import co.thnki.brandfever.fragments.CategoryEditorFragment;
import co.thnki.brandfever.fragments.ProductsFragment;
import co.thnki.brandfever.interfaces.DrawerItemClickListener;
import co.thnki.brandfever.interfaces.IOnBackPressedListener;

import static co.thnki.brandfever.interfaces.Const.AVAILABLE_;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_FIRST_LEVEL_CATEGORIES;

public class NavigationDrawerUtil implements DrawerItemClickListener, IOnBackPressedListener
{
    private static final String TAG = NavigationDrawerUtil.class.getSimpleName();
    private FragmentManager mFragmentManager;
    private AppCompatActivity mActivity;
    private Stack<String> mDrawerBackStack;
    //public Toolbar mToolbar;
    private DrawerLayout mDrawer;

    public NavigationDrawerUtil(FragmentManager fragmentManager, AppCompatActivity activity)
    {
        mFragmentManager = fragmentManager;
        mActivity = activity;
        mDrawerBackStack = new Stack<>();
        setupDrawer();
    }

    private void setupDrawer()
    {
        /*mToolbar = (Toolbar) mActivity.findViewById(R.id.toolbar);
        mActivity.setSupportActionBar(mToolbar);
        */
        mDrawer = (DrawerLayout) mActivity.findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                mActivity, mDrawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();
        mDrawer.setFocusableInTouchMode(false);
    }

    private void addFragmentToDrawer(Fragment fragment, String category, boolean isEntering)
    {
        if (mFragmentManager.findFragmentByTag(category) == null)
        {
            mDrawerBackStack.push(category);
            int enterAnimation = R.anim.enter_from_right;
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (category.equals(AVAILABLE_FIRST_LEVEL_CATEGORIES))
            {
                enterAnimation = R.anim.enter_directly;
            }
            if (category.contains(AVAILABLE_))
            {
                if(isEntering)
                {
                    transaction.setCustomAnimations(enterAnimation, R.anim.exit_to_left,
                            R.anim.enter_from_left, R.anim.exit_to_right);
                }
                else
                {
                    transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right,
                            R.anim.enter_from_right, R.anim.exit_to_left);
                }
            }

            transaction.replace(R.id.fragmentContainer, fragment, category)
                    .commit();
        }
    }

    @Override
    public void onFirstLevelItemClick(String category, boolean isEntering)
    {
        Log.d(TAG, "onFirstLevelItemClick : " + category);
        Fragment fragment = CategoryDrawerFragment.getInstance(category, this);
        if (fragment != null)
        {
            addFragmentToDrawer(fragment, category, isEntering);
        }
    }

    @Override
    public void onSecondLevelItemClick(String category)
    {
        Log.d(TAG, "onSecondLevelItemClick : " + category);
        Fragment fragment = ProductsFragment.getInstance(category, null);
        mFragmentManager.beginTransaction()
                .replace(R.id.content_main, fragment)
                .addToBackStack(category)
                .commit();
        mDrawer.closeDrawers();
    }

    @Override
    public void onEditClick(String category)
    {
        Log.d(TAG, "onEditClick : " + category);
        Fragment fragment = CategoryEditorFragment.getInstance(category, this);
        if (fragment != null)
        {
            addFragmentToDrawer(fragment, category, ENTER);
        }
    }

    @Override
    public void onBackClick()
    {
        onBackPressed();
    }

    @Override
    public boolean onBackPressed()
    {
        if (mDrawer.isDrawerOpen(GravityCompat.START))
        {
            int size = mDrawerBackStack.size();
            Log.d(TAG, "Back stack size : "+ size);
            if (size <= 1)
            {
                mDrawer.closeDrawer(GravityCompat.START);
            }
            else
            {
                mDrawerBackStack.pop();
                //To remove currently added fragment
                onFirstLevelItemClick(mDrawerBackStack.pop(), EXIT);
            }
            return false;
        }
        return true;
    }

    public void openDrawer()
    {
        mDrawer.openDrawer(GravityCompat.START);
    }
}
