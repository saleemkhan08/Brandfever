package co.thnki.brandfever;

import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.fragments.CategoriesDialogFragment;
import co.thnki.brandfever.fragments.DrawerFragment;
import co.thnki.brandfever.fragments.MainCategoryFragment;

import static co.thnki.brandfever.interfaces.Const.ALL_CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_FIRST_LEVEL_CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.FIRST_LEVEL_CATEGORIES;

public class StoreActivity extends AppCompatActivity
{
    private static final String MAIN_CATEGORY_FRAGMENT_TAG = "mainCategoryFragment";
    @Bind(R.id.content_main)
    RelativeLayout mContainer;

    MainCategoryFragment mMainCategoryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        DrawerFragment drawerFragment = new DrawerFragment();
        drawerFragment.setUp(drawer, toolbar);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if (mMainCategoryFragment == null)
        {
            mMainCategoryFragment = new MainCategoryFragment();
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, mMainCategoryFragment, MAIN_CATEGORY_FRAGMENT_TAG)
                .commit();
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.addCategory:
                Bundle bundle = new Bundle();
                bundle.putString(ALL_CATEGORIES, FIRST_LEVEL_CATEGORIES);
                bundle.putString(AVAILABLE_CATEGORIES, AVAILABLE_FIRST_LEVEL_CATEGORIES);
                CategoriesDialogFragment mCategoriesDialogFragment = new CategoriesDialogFragment();
                mCategoriesDialogFragment.setArguments(bundle);
                mCategoriesDialogFragment.show(getSupportFragmentManager(), CATEGORIES);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
