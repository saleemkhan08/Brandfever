package co.thnki.brandfever.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.ViewHolders.SimpleCategoryViewHolder;
import co.thnki.brandfever.firebase.database.models.Category;

import static co.thnki.brandfever.interfaces.Const.AVAILABLE_FIRST_LEVEL_CATEGORIES;

/**
 * Fragment handles the drawer menu.
 */

public class DrawerFragment extends Fragment implements AdapterView.OnItemClickListener
{
    @Bind(R.id.categoryRecyclerView)
    RecyclerView mCategoriesRecyclerView;
    private DatabaseReference mAvailableCategoriesRef;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View layout = inflater.inflate(R.layout.fragment_drawer, container, false);
        ButterKnife.bind(this, layout);
        mCategoriesRecyclerView.setHasFixedSize(true);
        mCategoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mAvailableCategoriesRef = FirebaseDatabase.getInstance().getReference().child(AVAILABLE_FIRST_LEVEL_CATEGORIES);

        FirebaseRecyclerAdapter<Category, SimpleCategoryViewHolder> adapter = new FirebaseRecyclerAdapter<Category, SimpleCategoryViewHolder>(
                Category.class,
                R.layout.simple_list_item,
                SimpleCategoryViewHolder.class,
                getCategories())
        {
            @Override
            protected void populateViewHolder(SimpleCategoryViewHolder viewHolder, Category model, int position)
            {
                viewHolder.mCategory.setText(model.getCategory());
            }
        };
        mCategoriesRecyclerView.setAdapter(adapter);
        return layout;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        Brandfever.toast("Pos : "+i);
    }

    public void setUp(DrawerLayout drawer, Toolbar toolbar)
    {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    public Query getCategories()
    {
        return  mAvailableCategoriesRef.orderByChild("categoryId");
    }
}
