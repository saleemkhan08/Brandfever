package co.thnki.brandfever;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.adapters.SectionsPagerAdapter;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.ProductBundle;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.fragments.EditProductDialogFragment;
import co.thnki.brandfever.singletons.Otto;
import co.thnki.brandfever.utils.CartUtil;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.utils.FavoritesUtil;
import co.thnki.brandfever.utils.SizesUtil;

import static co.thnki.brandfever.Brandfever.toast;
import static co.thnki.brandfever.R.layout.indicator;
import static co.thnki.brandfever.firebase.database.models.Products.PHOTO_NAME;
import static co.thnki.brandfever.firebase.database.models.Products.PHOTO_URL;
import static co.thnki.brandfever.fragments.ProductsFragment.PICK_IMAGE_MULTIPLE;
import static co.thnki.brandfever.utils.CartUtil.CART_LIST;

public class ProductActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener
{
    private static final String EDITOR = "productEditor";
    private static final String XS = "XS";
    private static final String S = "S";
    private static final String M = "M";
    private static final String L = "L";
    private static final String XL = "XL";
    private static final String XXL = "XXL";
    private static final long DELAY = 750;

    @Bind(R.id.pageIndicatorContainer)
    LinearLayout mPageIndicatorContainer;

    @Bind(R.id.brandText)
    TextView mBrandText;

    @Bind(R.id.priceTextAfter)
    TextView mPriceTextAfter;

    @Bind(R.id.priceTextBefore)
    TextView mPriceTextBefore;

    @Bind(R.id.productImagePager)
    ViewPager mProductImagePager;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.addToCartText)
    TextView mAddToCartText;

    @Bind(R.id.addToCartImg)
    ImageView mAddToCartImg;

    @Bind(R.id.transitionImage)
    ImageView mTransitionImage;

    //private String mCurrentCategory;
    private boolean mIsFavorite;

    @BindColor(R.color.background)
    int statusBarColor;
    private ProductBundle mProductBundle;
    private ProgressDialog mProgressDialog;

    private ArrayList<String> mPhotoUrlList;
    private ArrayList<String> mPhotoNameList;

    private Map<String, Boolean> mSelectedSizes;
    private DatabaseReference mProductDbRef;
    private StorageReference mProductStorageRef;
    private SectionsPagerAdapter mAdapter;

    private String mCurrentPhotoUrl = "";
    private String mCurrentPhotoName = "";

    @Bind(R.id.favorite)
    ImageView mFavoriteImageView;
    private FavoritesUtil mFavoritesUtil;
    private CartUtil mCartUtil;
    private boolean mIsAddedToCart;
    private SizesUtil mSizesUtil;

    @Bind(R.id.deletePhoto)
    ImageView mDeletePhotoImageView;

    @Bind(R.id.uploadMorePhotos)
    ImageView mUploadPhotoImageView;

    @Bind(R.id.editProductDetails)
    ImageView mEditProductDetailsImageView;

    @Bind(R.id.deleteProduct)
    ImageView mDeleteProductImageView;

    private boolean mIsPagerLoaded;
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
        setContentView(R.layout.activity_product);
        Otto.register(this);
        ButterKnife.bind(this);

        checkOwnerProfile();
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mProductBundle = getIntent().getParcelableExtra(Products.PRODUCT_MODEL);
        mAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        updateUi(mProductBundle);
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        mProductDbRef = root.child(mProductBundle.getCategoryId()).child(mProductBundle.getProductId());

        mSizesUtil = new SizesUtil(this);
        //mOrdersProductDbRef = userRef.child(ORDER_LIST);

        mProductStorageRef = FirebaseStorage.getInstance().getReference()
                .child(mProductBundle.getCategoryId())
                .child(mProductBundle.getProductId());

        mFavoritesUtil = FavoritesUtil.getsInstance();
        mCartUtil = CartUtil.getsInstance();

        mIsFavorite = mFavoritesUtil.isFavorite(mProductBundle);
        mIsAddedToCart = mCartUtil.isAddedToCart(mProductBundle);
        mSelectedSizes = initializeSizes();
        updateFavoriteUi();
        if (mIsAddedToCart)
        {
            FirebaseDatabase.getInstance()
                    .getReference().child(Brandfever.getPreferences().getString(Accounts.GOOGLE_ID, ""))
                    .child(CART_LIST)
                    .child(CartUtil.getsInstance().getKey(mProductBundle))
                    .addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            Products product = dataSnapshot.getValue(Products.class);
                            if (product != null)
                            {
                                mProductBundle = new ProductBundle(product);
                                updateAddToCartUi();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
        }
    }

    private void checkOwnerProfile()
    {
        if(Brandfever.getPreferences().getBoolean(Accounts.IS_OWNER, false))
        {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mDeletePhotoImageView.setVisibility(View.VISIBLE);
                    mDeleteProductImageView.setVisibility(View.VISIBLE);
                    mEditProductDetailsImageView.setVisibility(View.VISIBLE);
                    mUploadPhotoImageView.setVisibility(View.VISIBLE);
                }
            }, DELAY);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(!mIsPagerLoaded)
        {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mProductImagePager.setVisibility(View.VISIBLE);
                    mProductImagePager.setAdapter(mAdapter);
                    mProductImagePager.addOnPageChangeListener(ProductActivity.this);
                    mIsPagerLoaded = true;
                }
            }, DELAY);
        }
    }

    @Override
    public void onBackPressed()
    {
        hideAllIcons();
        super.onBackPressed();
    }

    @Override
    protected void onPause()
    {
        if(isFinishing())
        {
            hideAllIcons();
        }
        super.onPause();
    }

    private void hideAllIcons()
    {
        mProductImagePager.setVisibility(View.INVISIBLE);
        mPageIndicatorContainer.setVisibility(View.GONE);
        mDeletePhotoImageView.setVisibility(View.GONE);
        mDeleteProductImageView.setVisibility(View.GONE);
        mEditProductDetailsImageView.setVisibility(View.GONE);
        mUploadPhotoImageView.setVisibility(View.GONE);
    }

    private Map<String, Boolean> initializeSizes()
    {
        Map<String, Boolean> map = new HashMap<>();
        map.put(XS, false);
        map.put(S, false);
        map.put(M, false);
        map.put(L, false);
        map.put(XL, false);
        map.put(XXL, false);
        return map;
    }

    @BindDrawable(R.drawable.sizes_button_bg_inverse)
    Drawable mInverseSizeButtonDrawable;

    @BindDrawable(R.drawable.sizes_button_bg)
    Drawable mSizeButtonDrawable;

    @BindColor(R.color.colorAccent)
    int colorAccent;

    @BindColor(R.color.white)
    int colorWhite;

    @Bind(R.id.xs)
    TextView xs;

    @Bind(R.id.s)
    TextView s;

    @Bind(R.id.m)
    TextView m;

    @Bind(R.id.l)
    TextView l;

    @Bind(R.id.xl)
    TextView xl;

    @Bind(R.id.xxl)
    TextView xxl;

    @Bind(R.id.xsSpinner)
    Spinner xsSpinner;

    @Bind(R.id.sSpinner)
    Spinner sSpinner;

    @Bind(R.id.mSpinner)
    Spinner mSpinner;

    @Bind(R.id.lSpinner)
    Spinner lSpinner;

    @Bind(R.id.xlSpinner)
    Spinner xlSpinner;

    @Bind(R.id.xxlSpinner)
    Spinner xxlSpinner;

    @OnClick({R.id.xs, R.id.s, R.id.m, R.id.l, R.id.xl, R.id.xxl,
            R.id.xsWrapper, R.id.sWrapper, R.id.mWrapper, R.id.lWrapper, R.id.xlWrapper, R.id.xxlWrapper})
    public void selectSize(View view)
    {
        resetSizesUi();
        String selectedSize = view.getTag().toString();
        switch (selectedSize)
        {
            case XS:
                highlight(xs);
                //xsSpinner.performClick();
                break;
            case S:
                highlight(s);
                //sSpinner.performClick();
                break;
            case M:
                highlight(m);
                //mSpinner.performClick();
                break;
            case L:
                highlight(l);
                //lSpinner.performClick();
                break;
            case XL:
                highlight(xl);
                //xlSpinner.performClick();
                break;
            case XXL:
                highlight(xxl);
                //xxlSpinner.performClick();
                break;
        }
        showCountSelectionDialog(selectedSize);
    }

    private void showCountSelectionDialog(String selectedSize)
    {
        mSelectedSizes.put(selectedSize, true);
        // mSizesUtil.showSizesDialog(10);
    }

    private void resetSizesUi()
    {
        unHighlight(xs);
        unHighlight(s);
        unHighlight(m);
        unHighlight(l);
        unHighlight(xl);
        unHighlight(xxl);
        for (String key : mSelectedSizes.keySet())
        {
            mSelectedSizes.put(key, false);
        }
    }

    private void unHighlight(TextView textView)
    {
        textView.setBackground(mSizeButtonDrawable);
        textView.setTextColor(colorAccent);
    }

    private void highlight(TextView view)
    {
        view.setBackground(mInverseSizeButtonDrawable);
        view.setTextColor(colorWhite);

    }

    @OnClick(R.id.favorite)
    public void addToFavorite(ImageView favorite)
    {
        if (ConnectivityUtil.isConnected())
        {
            mIsFavorite = !mIsFavorite;
            updateFavoriteUi();
            if (mIsFavorite)
            {
                mFavoritesUtil.addToFavorites(mProductBundle);
            }
            else
            {
                mFavoritesUtil.removeFromFavorites(mProductBundle);
            }
        }
        else
        {
            toast(R.string.noInternet);
        }
    }

    @OnClick(R.id.addToCart)
    public void addToCart(View favorite)
    {
        if (ConnectivityUtil.isConnected())
        {
            if (!mIsAddedToCart)
            {
                String selectedSize = getSelectedSize(true);
                if (selectedSize != null)
                {
                    mCartUtil.addToCart(mProductBundle);
                    toast(R.string.addedToCart);
                    finish();
                }
                else
                {
                    toast(R.string.pleaseSelectSize);
                }
            }
            else
            {
                mCartUtil.removeFromCart(mProductBundle);
                mIsAddedToCart = false;
                updateAddToCartUi();
            }
        }
        else
        {
            toast(R.string.noInternet);
        }

    }

    private String getSelectedSize(boolean isAddToCart)
    {
        String selectedKey = null;
        Bundle selectedSizes;
        Log.d("selectedSizes", "isAddToCart : " + isAddToCart);
        if (isAddToCart)
        {
            selectedSizes = new Bundle();
            for (String key : mSelectedSizes.keySet())
            {
                if (mSelectedSizes.get(key))
                {
                    selectedKey = key;
                    selectedSizes.putInt(key, 1);
                }
                else
                {
                    selectedSizes.putInt(key, 0);
                }
            }
            mProductBundle.setSizesMap(selectedSizes);
        }
        else
        {
            selectedSizes = mProductBundle.getSizesMap();
            Log.d("selectedSizes", "selectedSizes : " + selectedSizes.toString());
            for (String key : selectedSizes.keySet())
            {
                if (selectedSizes.getInt(key) > 0)
                {
                    selectedKey = key;
                    mSelectedSizes.put(key, true);
                }
                else
                {
                    mSelectedSizes.put(key, false);
                }
            }
        }
        Log.d("selectedSizes", "selectedKey : " + selectedKey);
        return selectedKey;
    }

    private void updateAddToCartUi()
    {
        if (mIsAddedToCart)
        {
            mAddToCartImg.setImageResource(R.mipmap.shopping_cart_cancel_button);
            mAddToCartText.setText(R.string.removeFromCart);
            updateSizesUi();
        }
        else
        {
            mAddToCartImg.setImageResource(R.mipmap.shopping_cart_add_button);
            mAddToCartText.setText(R.string.addToCart);
        }
    }

    private void updateFavoriteUi()
    {
        if (mIsFavorite)
        {
            mFavoriteImageView.setImageResource(R.mipmap.favorite_fill);
        }
        else
        {
            mFavoriteImageView.setImageResource(R.mipmap.favorite);
        }
    }

    @Subscribe
    public void updateProduct(Products product)
    {
        mProductBundle = new ProductBundle(product);
        updateUi(mProductBundle);
    }

    private void updateUi(ProductBundle productBundle)
    {
        if (productBundle != null)
        {
            mPhotoUrlList = mProductBundle.getPhotoUrlList();
            mPhotoNameList = mProductBundle.getPhotoNameList();
            mAdapter.updateDataSet(mPhotoUrlList);

            Glide.with(this).load(mPhotoUrlList.get(0))
                    .asBitmap().placeholder(R.mipmap.price_tag)
                    .centerCrop().into(mTransitionImage);

            mBrandText.setText(productBundle.getBrand());
            mPriceTextAfter.setText(productBundle.getPriceAfter());
            String discountText = productBundle.getPriceBefore();
            if (discountText != null && !discountText.isEmpty())
            {
                mPriceTextBefore.setText(discountText);
                mPriceTextBefore.setPaintFlags(mPriceTextBefore.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            updatePageIndicator(mPhotoUrlList.size());
            mProductImagePager.setCurrentItem(0, true);
        }
        else
        {
            toast(R.string.invalid_product);
            finish();
        }
    }

    private void updatePageIndicator(final int size)
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mPageIndicatorContainer.removeAllViews();
                if (size > 1)
                {
                    for (int i = 0; i < size; i++)
                    {
                        addIndicator();
                    }
                    highLightPageIndicator(0);
                }
            }
        }, DELAY);
    }

    @OnClick(R.id.editProductDetails)
    public void launchProductEditor()
    {
        if (ConnectivityUtil.isConnected())
        {
            EditProductDialogFragment mEditProductDialogFragment = EditProductDialogFragment.getInstance(mProductBundle);
            mEditProductDialogFragment.show(getSupportFragmentManager(), EDITOR);
        }
        else
        {
            toast(R.string.noInternet);
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }

    @OnClick(R.id.uploadMorePhotos)
    public void uploadMorePhotos()
    {
        if (ConnectivityUtil.isConnected())
        {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
        }
        else
        {
            toast(R.string.noInternet);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        try
        {
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK && null != data)
            {
                ArrayList<String> mImagesEncodedList = new ArrayList<>();
                if (data.getData() != null)
                {
                    Uri mImageUri = data.getData();
                    mImagesEncodedList.add(mImageUri.toString());
                }
                else
                {
                    if (data.getClipData() != null)
                    {
                        ClipData mClipData = data.getClipData();
                        for (int i = 0; i < mClipData.getItemCount(); i++)
                        {
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri mImageUri = item.getUri();
                            mImagesEncodedList.add(mImageUri.toString());
                        }
                    }
                }
                if (ConnectivityUtil.isConnected())
                {
                    uploadMorePhotos(mImagesEncodedList);
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
            Brandfever.toast("Something went wrong");
            e.printStackTrace();
            mProgressDialog.dismiss();
        }
    }


    private void uploadMorePhotos(ArrayList<String> mImagesEncodedList)
    {
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(this);
        }

        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        final int currentSize = mPhotoUrlList.size();
        final int noOfUploadingPhoto = mImagesEncodedList.size();
        mProgressDialog.setMessage("Uploading " + 1 + " of " + noOfUploadingPhoto);
        Log.d("mImagesEncodedList", "mImagesEncodedList : " + mImagesEncodedList.size());
        Log.d("mImagesEncodedList", "currentSize : " + currentSize);


        for (int productIndex = 0; productIndex < noOfUploadingPhoto; productIndex++)
        {
            String uri = mImagesEncodedList.get(productIndex);
            int uploadIndex = productIndex + currentSize;
            Log.d("mImagesEncodedList", "uploadIndex : " + uploadIndex);

            /**
             * Put the photo using random key name does not matter as it will be directly accessed using the download URL
             */
            final String photoName = Products.generateRandomKey();

            StorageReference reference = mProductStorageRef.child(photoName);
            reference.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    int listSize = mPhotoUrlList.size();
                    int size = listSize - currentSize + 1;

                    if (downloadUri != null)
                    {
                        String url = downloadUri.toString();
                        if (!url.trim().isEmpty())
                        {
                            mPhotoUrlList.add(url);
                            mPhotoNameList.add(photoName);

                            mProductDbRef.child(Products.PHOTO_URL).setValue(mPhotoUrlList);
                            mProductDbRef.child(Products.PHOTO_NAME).setValue(mPhotoNameList);

                            mProductBundle.setPhotoUrlList(mPhotoUrlList);
                            mProductBundle.setPhotoNameList(mPhotoNameList);

                            updateUi(mProductBundle);
                        }
                    }
                    Log.d("SizesIssue", "listSize before : " + listSize + ", after : " + mPhotoUrlList.size());
                    Log.d("SizesIssue", "Size : " + size + ",currentSize : " + currentSize + ", noOfUploadingPhoto : " + noOfUploadingPhoto);

                    if (size >= noOfUploadingPhoto)
                    {
                        mProgressDialog.dismiss();
                        mProductImagePager.setCurrentItem(0, false);
                        mProductImagePager.setCurrentItem(mPhotoUrlList.size() - 1, true);
                    }
                    mProgressDialog.setMessage("Uploading " + (size + 1) + " of " + noOfUploadingPhoto);
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    mProgressDialog.dismiss();
                    toast(R.string.please_try_again);
                }
            });
        }
    }


    public View addIndicator()
    {
        return LayoutInflater.from(this).inflate(indicator, mPageIndicatorContainer);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {

    }

    @Override
    public void onPageSelected(final int position)
    {
        if(mPageIndicatorContainer.getChildCount() <= position)
        {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    highLightPageIndicator(position);
                }
            }, DELAY);
        }
        else
        {
            highLightPageIndicator(position);
        }
        mCurrentPhotoUrl = mAdapter.getItemUrl(position);
        mCurrentPhotoName = mPhotoNameList.get(position);
    }

    private void highLightPageIndicator(int position)
    {
        for (int i = 0; i < mPageIndicatorContainer.getChildCount(); i++)
        {
            ImageView indicator = (ImageView) ((LinearLayout) (mPageIndicatorContainer.getChildAt(i))).getChildAt(0);
            indicator.setImageResource(R.drawable.dot);
        }

        Log.d("NullCheck","mPageIndicatorContainer : "+ mPageIndicatorContainer );
        Log.d("NullCheck","mPageIndicatorContainer.getChildCount() : "+ mPageIndicatorContainer.getChildCount() );
        Log.d("NullCheck","position : "+ position);
        Log.d("NullCheck","mPageIndicatorContainer.getChildAt(position) : "+ mPageIndicatorContainer.getChildAt(position) );
        Log.d("NullCheck","((LinearLayout) (mPageIndicatorContainer.getChildAt(position))).getChildAt(0) : "
                + ((LinearLayout) (mPageIndicatorContainer.getChildAt(position))).getChildAt(0) );

        ImageView indicator = (ImageView) ((LinearLayout) (mPageIndicatorContainer.getChildAt(position))).getChildAt(0);
        indicator.setImageResource(R.drawable.accent_dot);
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {

    }

    @OnClick(R.id.deleteProduct)
    public void deleteProduct()
    {
        if (ConnectivityUtil.isConnected())
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.areYouSureYouWantToDeleteThisProduct);
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {

                }
            }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    mProductDbRef.removeValue();
                    for (String name : mPhotoNameList)
                    {
                        mProductStorageRef.child(name).delete();
                    }
                    mProductStorageRef.delete();
                    finish();
                }
            }).show();
        }
        else
        {
            toast(R.string.noInternet);
        }
    }

    @OnClick(R.id.deletePhoto)
    public void deleteImage()
    {
        if (ConnectivityUtil.isConnected())
        {
            if (mPhotoUrlList.size() > 1)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.areYouSureYouWantToDeleteThisImage);
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {

                    }
                }).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        if (mCurrentPhotoUrl == null || mCurrentPhotoUrl.isEmpty())
                        {
                            mCurrentPhotoUrl = mAdapter.getItemUrl(0);
                            mCurrentPhotoName = mPhotoNameList.get(0);
                        }

                        mProductStorageRef.child(mCurrentPhotoName).delete();

                        mPhotoNameList.remove(mCurrentPhotoName);
                        mPhotoUrlList.remove(mCurrentPhotoUrl);

                        mProductBundle.setPhotoUrlList(mPhotoUrlList);
                        mProductBundle.setPhotoNameList(mPhotoNameList);

                        mProductDbRef.child(PHOTO_URL).setValue(mPhotoUrlList);
                        mProductDbRef.child(PHOTO_NAME).setValue(mPhotoNameList);

                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                }).show();
            }
            else
            {
                toast(R.string.youCannotDeleteAllTheImages);
            }
        }
        else
        {
            toast(R.string.noInternet);
        }
    }

    private void updateSizesUi()
    {
        String selectedSize = getSelectedSize(false);
        resetSizesUi();
        if (selectedSize != null)
        {
            switch (selectedSize)
            {
                case XS:
                    highlight(xs);
                    break;
                case S:
                    highlight(s);
                    break;
                case M:
                    highlight(m);
                    break;
                case L:
                    highlight(l);
                    break;
                case XL:
                    highlight(xl);
                    break;
                case XXL:
                    highlight(xxl);
                    break;
            }
        }
    }
}