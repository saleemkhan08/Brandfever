package co.thnki.brandfever.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.R;
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.ViewHolders.MainCategoryViewHolder;
import co.thnki.brandfever.firebase.database.models.Category;

import static co.thnki.brandfever.interfaces.Const.ALL_CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_FASHION_ACCESSORIES;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_FIRST_LEVEL_CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_HOME_FURNISHING;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_KIDS_WEAR;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_MENS_WEAR;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_WOMENS_WEAR;
import static co.thnki.brandfever.interfaces.Const.FASHION_ACCESSORIES;
import static co.thnki.brandfever.interfaces.Const.FIRST_LEVEL_CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.HOME_FURNISHING;
import static co.thnki.brandfever.interfaces.Const.KIDS_WEAR;
import static co.thnki.brandfever.interfaces.Const.MENS_WEAR;
import static co.thnki.brandfever.interfaces.Const.WOMENS_WEAR;


public class MainPageFragment extends Fragment implements ValueEventListener
{
    //private static final String UPLOAD_CATEGORIES_FRAGMENT = "uploadCategoriesFragment";

    private static final String[] FIRST_LEVEL_CATEGORIES_IMAGE_URLS = new String[]{
            "https://firebasestorage.googleapis.com/v0/b/brandfever---aqua.appspot.com/o/appImages%2Fmens_wear_square.jpg?alt=media&token=71abe086-f37f-4b69-9b40-a080aeccfcbc",
            "https://firebasestorage.googleapis.com/v0/b/brandfever---aqua.appspot.com/o/appImages%2Fwomens_wear_square.jpg?alt=media&token=4571b68f-ce20-4438-b3ae-9e05a4cacb4e",
            "https://firebasestorage.googleapis.com/v0/b/brandfever---aqua.appspot.com/o/appImages%2Fkids_wear_square.jpg?alt=media&token=743fb027-347f-451d-a696-ec5aff556608",
            "https://firebasestorage.googleapis.com/v0/b/brandfever---aqua.appspot.com/o/appImages%2Ffashion_accesories_square.jpg?alt=media&token=9422f201-b728-41a3-9acd-5338ac7c6f5d",
            "https://firebasestorage.googleapis.com/v0/b/brandfever---aqua.appspot.com/o/appImages%2Fhome_furnishing_square.jpg?alt=media&token=cf006386-5e96-49f9-af94-04f2d1191cc3"
    };

    //private Toolbar mToolbar;

    @Bind(R.id.mainCategoryRecyclerView)
    RecyclerView mMainCategoryRecyclerView;
    private DatabaseReference mCategoriesRef;
    private RecyclerView.Adapter mAdapter;
    private DatabaseReference mRootRef;

    @Bind(R.id.mainCategoryProgress)
    ProgressBar mMainCategoryProgress;

    public MainPageFragment()
    {
    }

    public static Fragment getInstance(Toolbar toolbar)
    {
        MainPageFragment fragment = new MainPageFragment();
        //fragment.mToolbar = toolbar;
        fragment.mRootRef = FirebaseDatabase.getInstance().getReference();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_main_category, container, false);
        ButterKnife.bind(this, parentView);
        if(mRootRef != null)
        {
            mRootRef.addValueEventListener(this);
        }
        return parentView;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Activity activity = getActivity();
        if(activity instanceof StoreActivity)
        {
            ((StoreActivity)activity).setToolBarTitle(getString(R.string.app_name));
        }
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
                /*GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(viewHolder.mBackgroundImageView);
                Glide.with(getActivity()).load(model.getCategoryImage()).crossFade().into(imageViewTarget);
               */
                switch (model.getCategoryId())
                {
                    case 0:
                        resId = R.mipmap.mens_wear;
                        break;
                    case 1:
                        resId = R.mipmap.womens_wear;
                        break;
                    case 2:
                        resId = R.mipmap.boys_wear;
                        break;
                    case 3:
                        resId = R.mipmap.fashion_accesories;
                        break;
                    case 4:
                        resId = R.mipmap.home_furnishing;
                        break;
                }
                viewHolder.setBackgroundImageView(resId);

                viewHolder.mBackgroundImageView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Bundle bundle = new Bundle();
                        bundle.putString(AVAILABLE_CATEGORIES, "available_" + model.getCategoryName());
                        /*mUploadCategoriesFragment.setArguments(bundle);
                        mFragmentManager.beginTransaction()
                                .replace(R.id.content_main, mUploadCategoriesFragment, UPLOAD_CATEGORIES_FRAGMENT)
                                .addToBackStack(null)
                                .commit();*/
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
                        /*mCategoriesDialogFragment.setArguments(bundle);
                        mCategoriesDialogFragment.show(mFragmentManager, categoryName);*/
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
                        /*mUploadCategoriesDialogFragment.setArguments(bundle);
                        mUploadCategoriesDialogFragment.show(mFragmentManager, categoryName);*/
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
                Log.d("AdapterDataObserver", "onItemRangeInserted");
                if(mAdapter.hasObservers())
                {
                    try
                    {
                        mAdapter.unregisterAdapterDataObserver(this);
                    }catch (IllegalStateException e)
                    {
                        Log.d("AdapterDataObserver", "Not Registered");
                    }
                }
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
        String[] firstLevelCategories = getResources().getStringArray(R.array.firstLevel);
        String[] firstLevelCategoriesId = getResources().getStringArray(R.array.firstLevelId);
        addToDatabase(firstLevelCategories, FIRST_LEVEL_CATEGORIES, firstLevelCategoriesId, false);
        addToDatabase(firstLevelCategories, AVAILABLE_FIRST_LEVEL_CATEGORIES, firstLevelCategoriesId, false);

        String[] mensWear = getResources().getStringArray(R.array.mensWear);
        String[] mensWearId = getResources().getStringArray(R.array.mensWearId);
        addToDatabase(mensWear, MENS_WEAR, mensWearId, false);
        addToDatabase(mensWear, AVAILABLE_MENS_WEAR, mensWearId, false);
        //addToDatabase(mensWear, MENS_WEAR, mensWearId, true);


        String[] womensWear = getResources().getStringArray(R.array.womensWear);
        String[] womensWearId = getResources().getStringArray(R.array.womensWearId);
        addToDatabase(womensWear, WOMENS_WEAR, womensWearId, false);
        addToDatabase(womensWear, AVAILABLE_WOMENS_WEAR, womensWearId, false);
        //addToDatabase(womensWear, WOMENS_WEAR, womensWearId, true);

        String[] kidsWear = getResources().getStringArray(R.array.kidsWear);
        String[] kidsWearId = getResources().getStringArray(R.array.kidsWearId);
        addToDatabase(kidsWear, KIDS_WEAR, kidsWearId, false);
        addToDatabase(kidsWear, AVAILABLE_KIDS_WEAR, kidsWearId, false);
        //addToDatabase(kidsWear, KIDS_WEAR, kidsWearId, true);


        String[] fashionAccessories = getResources().getStringArray(R.array.fashionAccessories);
        String[] fashionAccessoriesId = getResources().getStringArray(R.array.fashionAccessoriesId);
        addToDatabase(fashionAccessories, FASHION_ACCESSORIES, fashionAccessoriesId, false);
        addToDatabase(fashionAccessories, AVAILABLE_FASHION_ACCESSORIES, fashionAccessoriesId, false);
        //addToDatabase(fashionAccessories, FASHION_ACCESSORIES, fashionAccessoriesId, true);

        String[] homeFurnishing = getResources().getStringArray(R.array.homeFurnishing);
        String[] homeFurnishingId = getResources().getStringArray(R.array.homeFurnishingId);
        addToDatabase(homeFurnishing, HOME_FURNISHING, homeFurnishingId, false);
        addToDatabase(homeFurnishing, AVAILABLE_HOME_FURNISHING, homeFurnishingId, false);
        //addToDatabase(homeFurnishing, HOME_FURNISHING, homeFurnishingId, true);
    }

    private void addToDatabase(String[] categories, String name, String[] childIds, boolean isSecondLevel)
    {
        Log.d("Categories", "addToDatabase : " + name);
        DatabaseReference mCategoriesRef = mRootRef.child(name);
        for (int i = 0; i < categories.length; i++)
        {
            Category category;
            if (name.equals(FIRST_LEVEL_CATEGORIES) || name.equals(AVAILABLE_FIRST_LEVEL_CATEGORIES))
            {
                category = new Category(categories[i], i, childIds[i], FIRST_LEVEL_CATEGORIES_IMAGE_URLS[i]);
            }
            else
            {
                category = new Category(categories[i], i, childIds[i]);
            }
            category.setCategorySelected(true);
            /*if (isSecondLevel)
            {
                DatabaseReference secondChildRef = mRootRef.child(AVAILABLE_ + category.getCategoryName());
                secondChildRef.setValue(category);
            }
            else
            {*/
                DatabaseReference childRef = mCategoriesRef.child(i + "");
                childRef.setValue(category);
            //}
        }
    }

    public Query getCategories()
    {
        return mCategoriesRef.orderByChild("categoryId");
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot)
    {
        if (dataSnapshot.getChildrenCount() <= 0)
        {
            saveCategories();
        }
        initializeDatabaseRefs();
        initializeRecyclerView();
    }

    @Override
    public void onCancelled(DatabaseError databaseError)
    {

    }
}
