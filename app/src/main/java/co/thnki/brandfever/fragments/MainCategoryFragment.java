package co.thnki.brandfever.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.ViewHolders.MainCategoryViewHolder;
import co.thnki.brandfever.firebase.database.models.Category;

import static co.thnki.brandfever.interfaces.Const.ALL_CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.ARE_CATEGORIES_ADDED;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_FIRST_LEVEL_CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.BOYS_WEAR;
import static co.thnki.brandfever.interfaces.Const.FASHION_ACCESSORIES;
import static co.thnki.brandfever.interfaces.Const.FIRST_LEVEL_CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.GIRLS_WEAR;
import static co.thnki.brandfever.interfaces.Const.HOME_FURNISHING;
import static co.thnki.brandfever.interfaces.Const.MENS_WEAR;
import static co.thnki.brandfever.interfaces.Const.WOMENS_WEAR;


public class MainCategoryFragment extends Fragment
{
    private static final String UPLOAD_CATEGORIES_FRAGMENT = "uploadCategoriesFragment";
    CategoriesDialogFragment mCategoriesDialogFragment;
    FragmentManager mFragmentManager;
    UploadCategoriesFragment mUploadCategoriesFragment;
    @Bind(R.id.mainCategoryRecyclerView)
    RecyclerView mMainCategoryRecyclerView;
    private DatabaseReference mCategoriesRef;
    private RecyclerView.Adapter mAdapter;

    @Bind(R.id.mainCategoryProgress)
    ProgressBar mMainCategoryProgress;
    private UploadCategoriesDialogFragment mUploadCategoriesDialogFragment;

    public MainCategoryFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_main_category, container, false);
        ButterKnife.bind(this, parentView);
        mCategoriesDialogFragment = new CategoriesDialogFragment();
        mUploadCategoriesDialogFragment = new UploadCategoriesDialogFragment();
        mUploadCategoriesFragment = new UploadCategoriesFragment();

        saveCategories();
        initializeDatabaseRefs();
        initializeRecyclerView();
        mFragmentManager = getActivity().getSupportFragmentManager();
        return parentView;
    }

    private void initializeRecyclerView()
    {
        mMainCategoryRecyclerView.setHasFixedSize(true);
        mMainCategoryRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new FirebaseRecyclerAdapter<Category, MainCategoryViewHolder>(
                Category.class,
                R.layout.main_categories_row,
                MainCategoryViewHolder.class,
                getCategories())
        {
            @Override
            protected void populateViewHolder(MainCategoryViewHolder viewHolder, final Category model, int position)
            {
                int resId = R.mipmap.mens_wear;
                viewHolder.setTitleTextView(model.getCategory());
                GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(viewHolder.mBackgroundImageView);
                Glide.with(getActivity()).load(model.getCategoryImage()).crossFade().into(imageViewTarget);
               /*
                switch (model.getCategoryId())
                {
                    case 0 : resId = R.mipmap.mens_wear;
                        break;
                    case 1 : resId = R.mipmap.womens_wear;
                        break;
                    case 2 : resId = R.mipmap.boys_wear;
                        break;
                    case 3 : resId = R.mipmap.girls_wear;
                        break;
                    case 4 : resId = R.mipmap.fashion_accesories;
                        break;
                    case 5 : resId = R.mipmap.home_furnishing;
                        break;
                }
                viewHolder.setBackgroundImageView(resId);*/

                viewHolder.mBackgroundImageView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Bundle bundle = new Bundle();
                        bundle.putString(AVAILABLE_CATEGORIES, "available_" + model.getCategoryName());
                        mUploadCategoriesFragment.setArguments(bundle);
                        mFragmentManager.beginTransaction()
                                .replace(R.id.content_main, mUploadCategoriesFragment, UPLOAD_CATEGORIES_FRAGMENT)
                                .addToBackStack(null)
                                .commit();
                    }
                });

                viewHolder.mEditImageView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Bundle bundle = new Bundle();
                        String categoryName = model.getCategoryName();
                        bundle.putString(ALL_CATEGORIES, categoryName);
                        bundle.putString(AVAILABLE_CATEGORIES, "available_" + categoryName);
                        mCategoriesDialogFragment.setArguments(bundle);
                        mCategoriesDialogFragment.show(mFragmentManager, categoryName);
                    }
                });

                viewHolder.mUploadImageView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Bundle bundle = new Bundle();
                        String categoryName = model.getCategoryName();
                        bundle.putString(ALL_CATEGORIES, categoryName);
                        bundle.putString(AVAILABLE_CATEGORIES, "available_" + categoryName);
                        mUploadCategoriesDialogFragment.setArguments(bundle);
                        mUploadCategoriesDialogFragment.show(mFragmentManager, categoryName);
                    }
                });
            }
        };

        mMainCategoryRecyclerView.setAdapter(mAdapter);
        setupProgress();
    }

    private void setupProgress()
    {
        mMainCategoryProgress.setVisibility(View.VISIBLE);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
        {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount)
            {
                super.onItemRangeInserted(positionStart, itemCount);
                mMainCategoryProgress.setVisibility(View.GONE);
                mAdapter.unregisterAdapterDataObserver(this);
            }
        });
    }

    private void initializeDatabaseRefs()
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        mCategoriesRef = rootRef.child(AVAILABLE_FIRST_LEVEL_CATEGORIES);
    }

    private void saveCategories()
    {
        Log.d("Categories", "saveCategories");
        SharedPreferences preferences = Brandfever.getPreferences();
        if (!preferences.getBoolean(ARE_CATEGORIES_ADDED, false))
        {
            Log.d("Categories", "ARE_CATEGORIES_ADDED : " + false);
            preferences.edit().putBoolean(ARE_CATEGORIES_ADDED, true).apply();
            String[] firstLevelCategories = getResources().getStringArray(R.array.firstLevel);
            String[] firstLevelCategoriesId = getResources().getStringArray(R.array.firstLevelId);
            addToDatabase(firstLevelCategories, FIRST_LEVEL_CATEGORIES, firstLevelCategoriesId);
            addToDatabase(firstLevelCategories, AVAILABLE_FIRST_LEVEL_CATEGORIES, firstLevelCategoriesId);

            String[] mensWear = getResources().getStringArray(R.array.mensWear);
            String[] mensWearId = getResources().getStringArray(R.array.mensWearId);
            addToDatabase(mensWear, MENS_WEAR, mensWearId);

            String[] womensWear = getResources().getStringArray(R.array.womensWear);
            String[] womensWearId = getResources().getStringArray(R.array.womensWearId);
            addToDatabase(womensWear, WOMENS_WEAR, womensWearId);

            String[] boysWear = getResources().getStringArray(R.array.boysWear);
            String[] boysWearId = getResources().getStringArray(R.array.boysWearId);

            addToDatabase(boysWear, BOYS_WEAR, boysWearId);

            String[] girlsWear = getResources().getStringArray(R.array.girlsWear);
            String[] girlsWearId = getResources().getStringArray(R.array.girlsWearId);
            addToDatabase(girlsWear, GIRLS_WEAR, girlsWearId);

            String[] fashionAccessories = getResources().getStringArray(R.array.fashionAccessories);
            String[] fashionAccessoriesId = getResources().getStringArray(R.array.fashionAccessoriesId);

            addToDatabase(fashionAccessories, FASHION_ACCESSORIES, fashionAccessoriesId);

            String[] homeFurnishing = getResources().getStringArray(R.array.homeFurnishing);
            String[] homeFurnishingId = getResources().getStringArray(R.array.homeFurnishingId);
            addToDatabase(homeFurnishing, HOME_FURNISHING, homeFurnishingId);
        }
    }

    private void addToDatabase(String[] categories, String name, String[] childIds)
    {
        Log.d("Categories", "addToDatabase : " + name);
        DatabaseReference mCategoriesRef = FirebaseDatabase.getInstance().getReference().child(name);
        mCategoriesRef.removeValue();
        for (int i = 0; i < categories.length; i++)
        {
            Category category = new Category(categories[i], i, childIds[i]);
            if (name.equals(FIRST_LEVEL_CATEGORIES) || name.equals(AVAILABLE_FIRST_LEVEL_CATEGORIES))
            {
                category.setCategorySelected(true);
            }
            DatabaseReference childRef = mCategoriesRef.child(i + "");
            childRef.setValue(category);
        }
    }

    public Query getCategories()
    {
        return mCategoriesRef.orderByChild("categoryId");
    }
}
