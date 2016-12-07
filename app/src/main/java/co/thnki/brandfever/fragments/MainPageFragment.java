package co.thnki.brandfever.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.otto.Subscribe;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.Category;
import co.thnki.brandfever.singletons.Otto;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.utils.ImageUtil;
import co.thnki.brandfever.utils.InitialSetupUtil;
import co.thnki.brandfever.view.holders.MainCategoryViewHolder;

import static android.app.Activity.RESULT_OK;
import static co.thnki.brandfever.Brandfever.toast;
import static co.thnki.brandfever.fragments.ProductsFragment.REQUEST_CODE_SDCARD_PERMISSION;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_FASHION_ACCESSORIES;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_FIRST_LEVEL_CATEGORIES;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_HOME_FURNISHING;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_KIDS_WEAR;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_MENS_WEAR;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_WOMENS_WEAR;
import static co.thnki.brandfever.interfaces.Const.CATEGORY_ID;
import static co.thnki.brandfever.interfaces.Const.FASHION_ACCESSORIES;
import static co.thnki.brandfever.interfaces.Const.HOME_FURNISHING;
import static co.thnki.brandfever.interfaces.Const.KIDS_WEAR;
import static co.thnki.brandfever.interfaces.Const.MENS_WEAR;
import static co.thnki.brandfever.interfaces.Const.WOMENS_WEAR;
import static co.thnki.brandfever.utils.InitialSetupUtil.APP_IMAGES;


public class MainPageFragment extends Fragment
{
    public static final String TAG = "MainPageFragment";
    private static final int PICK_CATEGORY_IMAGE = 199;
    private static final String SHOP_GALLERY = "shopGallery";

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
    private ProgressDialog mDialog;
    private StoreActivity mActivity;

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
        //setupGallery();
        return parentView;
    }

    private void setupGallery()
    {
        /*GalleryFragment fragment = new GalleryFragment();
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.galleryContainer, fragment, SHOP_GALLERY)
                .commit();*/
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mActivity = (StoreActivity) getActivity();
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
        mActivity.setToolBarTitle(getString(R.string.app_name));
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
        DatabaseReference ref = mRootRef.child(AVAILABLE_FIRST_LEVEL_CATEGORIES);
        Log.d("updateUi", "ref : " + ref);
        ref.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    hideAllCategories();
                    Log.d("updateUi", "DataSnapshot : " + dataSnapshot);
                    Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                    for (DataSnapshot snapshot : snapshots)
                    {
                        Category category = snapshot.getValue(Category.class);
                        unHideCategory(category.getCategory(), category.getCategoryName());
                    }
                }
                catch (Exception e)
                {
                    if (mDialog != null)
                    {
                        mDialog.dismiss();
                    }
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

    private void unHideCategory(String category, String categoryName)
    {
        switch (category)
        {
            case MENS_WEAR:
                mMensWear.setVisibility(View.VISIBLE);
                mMensWearTextView.setText(categoryName);
                break;

            case WOMENS_WEAR:
                mWomensWear.setVisibility(View.VISIBLE);
                mWomensWearTextView.setText(categoryName);
                break;

            case KIDS_WEAR:
                mKidsWear.setVisibility(View.VISIBLE);
                mKidsWearTextView.setText(categoryName);
                break;

            case FASHION_ACCESSORIES:
                mFashionAccessories.setVisibility(View.VISIBLE);
                mFashionAccessoriesTextView.setText(categoryName);
                break;

            case HOME_FURNISHING:
                mHomeFurnishing.setVisibility(View.VISIBLE);
                mHomeFurnishingTextView.setText(categoryName);
                break;
        }
    }

    private void hideCategory(String categoryName)
    {
        switch (categoryName)
        {
            case MENS_WEAR:
                mMensWear.setVisibility(View.GONE);
                break;
            case WOMENS_WEAR:
                mWomensWear.setVisibility(View.GONE);
                break;
            case KIDS_WEAR:
                mKidsWear.setVisibility(View.GONE);
                break;
            case FASHION_ACCESSORIES:
                mFashionAccessories.setVisibility(View.GONE);
                break;
            case HOME_FURNISHING:
                mHomeFurnishing.setVisibility(View.GONE);
                break;
        }
    }

    private void initializeRecyclerView(RecyclerView recyclerView, Query dbRef)
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
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
                if (!mPreference.getBoolean(Accounts.IS_OWNER, false))
                {
                    viewHolder.mUploadCategoryImageIcon.setVisibility(View.INVISIBLE);
                }
                viewHolder.mUploadCategoryImageButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if (mPreference.getBoolean(Accounts.IS_OWNER, false))
                        {
                            mSelectedCategory = model;
                            if (mActivity.isSdcardPermissionAvailable())
                            {
                                getImages();
                            }
                            else
                            {
                                mActivity.requestSdCardPermission();
                            }
                        }
                        else
                        {
                            mActivity.addFragment(AVAILABLE_ + model.getCategory());
                        }
                    }
                });
                GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(viewHolder.mBackgroundImageView);
                Glide.with(mActivity).load(model.getCategoryImage()).crossFade().into(imageViewTarget);

                viewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        mActivity.addFragment(AVAILABLE_ + model.getCategory());
                    }
                });
            }
        };

        recyclerView.setAdapter(adapter);
        setupProgress(adapter);
    }

    private void getImages()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_CATEGORY_IMAGE);
    }

    @Subscribe
    public void getImages(String action)
    {
        if (action.equals(StoreActivity.ON_REQUEST_PERMISSION_RESULT))
        {
            getImages();
        }
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
                        if (mDialog != null)
                        {
                            mDialog.dismiss();
                        }
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
            switch (requestCode)
            {
                case PICK_CATEGORY_IMAGE:
                    if (resultCode == RESULT_OK && null != data)
                    {
                        Uri uri = null;
                        if (data.getData() != null)
                        {
                            uri = data.getData();
                        }
                        if (ConnectivityUtil.isConnected() && uri != null)
                        {
                            changeTheCategoryImage(uri);
                        }
                        else
                        {
                            toast(R.string.noInternet);
                        }
                    }
                    else
                    {
                        toast(R.string.youHaventPickedAnImage);
                    }
                    break;
                case REQUEST_CODE_SDCARD_PERMISSION:

            }
        }
        catch (Exception e)
        {
            if (mDialog != null)
            {
                mDialog.dismiss();
            }
            e.printStackTrace();
            toast(R.string.something_went_wrong);
        }
    }

    private void changeTheCategoryImage(Uri imageUri)
    {
        mDialog = new ProgressDialog(mActivity);
        mDialog.setMessage(getString(R.string.changing));
        mDialog.show();
        Log.d("ChangeCategoryImg", mSelectedCategory.toString());
        final File destFile = ImageUtil.saveBitmapToFile(imageUri, mSelectedCategory.getCategory());
        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference().child(APP_IMAGES).child(mSelectedCategory.getParentCategory());

        Log.d("ChangeCategoryImg", "StorageReference :" + storageReference.toString());
        storageReference.putFile(Uri.fromFile(destFile))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        if (taskSnapshot != null)
                        {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            mSelectedCategory.setCategoryImage(downloadUrl.toString());
                            DatabaseReference ref = mRootRef.child(AVAILABLE_ + mSelectedCategory.getParentCategory())
                                    .child(mSelectedCategory.getCategoryId() + "");
                            Log.d("ChangeCategoryImg", "DatabaseReference : " + ref.toString());
                            ref.setValue(mSelectedCategory);
                        }
                        if (destFile != null && destFile.exists())
                        {
                            destFile.delete();
                        }
                        mDialog.dismiss();
                    }
                });
    }
}
