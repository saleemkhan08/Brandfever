package co.thnki.brandfever;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.fragments.MainPageFragment;
import co.thnki.brandfever.singletons.Otto;
import co.thnki.brandfever.utils.NavigationDrawerUtil;

import static co.thnki.brandfever.interfaces.Const.AVAILABLE_FIRST_LEVEL_CATEGORIES;
import static co.thnki.brandfever.interfaces.DrawerItemClickListener.ENTER;

public class StoreActivity extends AppCompatActivity
{
    private static final String TAG = "storeActivity";

    @Bind(R.id.content_main)
    RelativeLayout mContainer;
    private FragmentManager mFragmentManager;
    private NavigationDrawerUtil mNavigationDrawerUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setContentView(R.layout.activity_store);
        ButterKnife.bind(this);
        Otto.register(this);
        mFragmentManager = getSupportFragmentManager();
        mNavigationDrawerUtil = new NavigationDrawerUtil(mFragmentManager, this);
        mNavigationDrawerUtil.onFirstLevelItemClick(AVAILABLE_FIRST_LEVEL_CATEGORIES, ENTER);
        addMainPageFragment();
    }

    private void addMainPageFragment()
    {
        Fragment fragment = MainPageFragment.getInstance(null);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.store_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.drawer)
    public void openDrawer()
    {
        mNavigationDrawerUtil.openDrawer();
    }
}