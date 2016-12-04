package co.thnki.brandfever.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.otto.Subscribe;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.Category;
import co.thnki.brandfever.interfaces.Const;
import co.thnki.brandfever.singletons.Otto;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.utils.ImageUtil;
import co.thnki.brandfever.utils.InitialSetupUtil;
import co.thnki.brandfever.view.holders.MainCategoryViewHolder;

import static android.app.Activity.RESULT_OK;
import static co.thnki.brandfever.Brandfever.toast;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_FASHION_ACCESSORIES;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_FIRST_LEVEL_CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_HOME_FURNISHING;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_KIDS_WEAR;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_MENS_WEAR;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_WOMENS_WEAR;
import static co.thnki.brandfever.interfaces.Const.CATEGORY_ID;


public class MainPageFragment extends Fragment
{
    public static final String TAG = "MainPageFragment";
    private static final int PICK_CATEGORY_IMAGE = 199;

    @Bind(R.id.mensWearRecyclerView)
    RecyclerView mMensWearRecyclerView;

    @Bind(R.id.womensWearRecyclerView)
    RecyclerView mWomensWearRecyclerView;

    @Bind(R.id.kidsWearRecyclerView)
    RecyclerView mKidsWearRecyclerView;

    @Bind(R.id.fashionAccessoriesRecyclerView)
    RecyclerView mFashionAccessoriesRecyclerView;

    @Bind(R.id.homeFurnishingRecyclerView)
    RecyclerView mHomeFurnishingRecyclerView;

    @Bind(R.id.mensWearTextView)
    TextView mMensWearTextView;

    @Bind(R.id.womensWearTextView)
    TextView mWomensWearTextView;

    @Bind(R.id.kidsWearTextView)
    TextView mKidsWearTextView;

    @Bind(R.id.fashionAccessoriesTextView)
    TextView mFashionAccessoriesTextView;

    @Bind(R.id.homeFurnishingTextView)
    TextView mHomeFurnishingTextView;

    @Bind(R.id.mensWear)
    View mMensWear;

    @Bind(R.id.womensWear)
    View mWomensWear;

    @Bind(R.id.kidsWear)
    View mKidsWear;

    @Bind(R.id.fashionAccessories)
    View mFashionAccessories;

    @Bind(R.id.homeFurnishing)
    View mHomeFurnishing;

    private DatabaseReference mRootRef;

    @Bind(R.id.mainCategoryProgress)
    ProgressBar mMainCategoryProgress;

    String[] mFirstLevelCategories;
    private SharedPreferences mPreference;
    private Category mSelectedCategory;

    public MainPageFragment()
    {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_main_category, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        mPreference = Brandfever.getPreferences();
        mFirstLevelCategories = Brandfever.getResStringArray(R.array.firstLevelCategoriesId);
        mRootRef = FirebaseDatabase.getInstance().getReference();
        InitialSetupUtil.updateUi();
        return parentView;
    }

    private void hideAllCategories()
    {
        for (String category : mFirstLevelCategories)
        {
            hideCategory(category);
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof StoreActivity)
        {
            ((StoreActivity) activity).setToolBarTitle(getString(R.string.app_name));

        }
    }

    @Subscribe
    public void initialSetUpComplete(String action)
    {
        if (action.equals(InitialSetupUtil.INITIAL_SETUP_COMPLETE))
        {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    updateUi();
                }
            }, 250);
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }

    private void updateUi()
    {
        mRootRef.child(AVAILABLE_FIRST_LEVEL_CATEGORIES).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    hideAllCategories();
                    Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                    for (DataSnapshot snapshot : snapshots)
                    {
                        Category category = snapshot.getValue(Category.class);
                        unHideCategory(category.getCategory(), category.getCategoryName());
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
        initializeRecyclerView(mFashionAccessoriesRecyclerView, getCategoryRef(AVAILABLE_FASHION_ACCESSORIES));
        initializeRecyclerView(mHomeFurnishingRecyclerView, getCategoryRef(AVAILABLE_HOME_FURNISHING));
        initializeRecyclerView(mKidsWearRecyclerView, getCategoryRef(AVAILABLE_KIDS_WEAR));
        initializeRecyclerView(mMensWearRecyclerView, getCategoryRef(AVAILABLE_MENS_WEAR));
        initializeRecyclerView(mWomensWearRecyclerView, getCategoryRef(AVAILABLE_WOMENS_WEAR));
    }

    private void unHideCategory(String categoryName, String category)
    {
        switch (categoryName)
        {
            case "mensWear":
                mMensWear.setVisibility(View.VISIBLE);
                mMensWearTextView.setText(category);
                break;

            case "womensWear":
                mWomensWear.setVisibility(View.VISIBLE);
                mWomensWearTextView.setText(category);
                break;

            case "kidsWear":
                mKidsWear.setVisibility(View.VISIBLE);
                mKidsWearTextView.setText(category);
                break;

            case "fashionAccessories":
                mFashionAccessories.setVisibility(View.VISIBLE);
                mFashionAccessoriesTextView.setText(category);
                break;

            case "homeFurnishing":
                mHomeFurnishing.setVisibility(View.VISIBLE);
                mHomeFurnishingTextView.setText(category);
                break;
        }
    }

    private void hideCategory(String categoryName)
    {
        switch (categoryName)
        {
            case "mensWear":
                mMensWear.setVisibility(View.GONE);
                break;

            case "womensWear":
                mWomensWear.setVisibility(View.GONE);
                break;

            case "kidsWear":
                mKidsWear.setVisibility(View.GONE);
                break;

            case "fashionAccessories":
                mFashionAccessories.setVisibility(View.GONE);
                break;

            case "homeFurnishing":
                mHomeFurnishing.setVisibility(View.GONE);
                break;

        }
    }

    private void initializeRecyclerView(RecyclerView recyclerView, Query dbRef)
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        FirebaseRecyclerAdapter<Category, MainCategoryViewHolder> adapter = new FirebaseRecyclerAdapter<Category, MainCategoryViewHolder>(
                Category.class,
                R.layout.main_categories_row,
                MainCategoryViewHolder.class,
                dbRef)
        {
            @Override
            protected void populateViewHolder(MainCategoryViewHolder viewHolder, final Category model, int position)
            {
                viewHolder.setTitleTextView(model.getCategoryName());

                viewHolder.mUploadCategoryImageButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if (mPreference.getBoolean(Accounts.IS_OWNER, false))
                        {
                            getImageFromGallery(model);
                        }
                        else
                        {
                            ((StoreActivity) getActivity()).addFragment(Const.AVAILABLE_ + model.getCategory());
                        }
                    }
                });
                GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(viewHolder.mBackgroundImageView);
                Glide.with(getActivity()).load(model.getCategoryImage()).crossFade().into(imageViewTarget);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        ((StoreActivity) getActivity()).addFragment(Const.AVAILABLE_ + model.getCategory());
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
        setupProgress(adapter);
    }

    private void getImageFromGallery(Category model)
    {
        mSelectedCategory = model;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_CATEGORY_IMAGE);
    }

    private void setupProgress(final RecyclerView.Adapter adapter)
    {
        mMainCategoryProgress.setVisibility(View.VISIBLE);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
        {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount)
            {
                super.onItemRangeInserted(positionStart, itemCount);
                mMainCategoryProgress.setVisibility(View.GONE);
                Log.d("AdapterDataObserver", "onItemRangeInserted");
                if (adapter.hasObservers())
                {
                    try
                    {
                        adapter.unregisterAdapterDataObserver(this);
                    }
                    catch (IllegalStateException e)
                    {
                        Log.d("AdapterDataObserver", "Not Registered");
                    }
                }
            }
        });
    }

    private Query getCategoryRef(String availableFashionAccessories)
    {
        return mRootRef.child(availableFashionAccessories).orderByChild(CATEGORY_ID);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        try
        {
            if (requestCode == PICK_CATEGORY_IMAGE && resultCode == RESULT_OK && null != data)
            {
                Uri mImageUri = null;
                if (data.getData() != null)
                {
                    mImageUri = data.getData();
                }
                if (ConnectivityUtil.isConnected() && mImageUri != null)
                {
                    final File destFile = ImageUtil.saveBitmapToFile(mImageUri, mSelectedCategory.getCategoryImage());
                    /*StorageReference storageReference = FirebaseStorage.getInstance()
                            .getReference().child(APP_IMAGES).child(mSelectedCategory..replace(AVAILABLE_, "")).child(childId + ".jpg");*/
                }
                else
                {
                    toast(R.string.noInternet);
                }
            }
            else
            {
                Brandfever.toast("You haven't picked Image");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Brandfever.toast("Something went wrong");
        }
    }
}
