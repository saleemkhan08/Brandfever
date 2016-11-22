package co.thnki.brandfever;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.ProductBundle;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.fragments.EditProductDialogFragment;
import co.thnki.brandfever.fragments.ProductPagerFragment;
import co.thnki.brandfever.singletons.Otto;
import co.thnki.brandfever.utils.CartUtil;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.utils.FavoritesUtil;

import static co.thnki.brandfever.Brandfever.toast;

public class ProductActivity extends AppCompatActivity
{
    private static final String EDITOR = "productEditor";
    private static final String XS = "XS";
    private static final String S = "S";
    private static final String M = "M";
    private static final String L = "L";
    private static final String XL = "XL";
    private static final String XXL = "XXL";
    private static final long DELAY = 500;
    private static final String PRODUCT_PAGER_FRAGMENT = "productPagerFragment";

    @Bind(R.id.brandText)
    TextView mBrandText;

    @Bind(R.id.priceTextAfter)
    TextView mPriceTextAfter;

    @Bind(R.id.priceTextBefore)
    TextView mPriceTextBefore;

    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Bind(R.id.addToCartText)
    TextView mAddToCartText;

    @Bind(R.id.addToCartImg)
    ImageView mAddToCartImg;

    @Bind(R.id.transitionImage)
    ImageView mTransitionImage;

    @Bind(R.id.favorite)
    ImageView mFavoriteImageView;

    @Bind(R.id.editProductDetails)
    ImageView mEditProductDetailsImageView;

    @Bind(R.id.deleteProduct)
    ImageView mDeleteProductImageView;

    @BindDrawable(R.drawable.sizes_button_bg_inverse)
    Drawable mInverseSizeButtonDrawable;

    @BindDrawable(R.drawable.sizes_button_disabled)
    Drawable mDisableSizeButtonDrawable;

    @BindDrawable(R.drawable.sizes_button_bg)
    Drawable mSizeButtonDrawable;

    @BindColor(R.color.colorAccent)
    int colorAccent;

    @BindColor(R.color.white)
    int colorWhite;

    @BindColor(R.color.grey)
    int colorGrey;

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

    @BindColor(R.color.background)
    int statusBarColor;

    private ProductBundle mProductBundle;

    private Map<String, Boolean> mSelectedSizes;
    private DatabaseReference mProductDbRef;
    private StorageReference mProductStorageRef;

    //private String mCurrentCategory;
    private boolean mIsFavorite;

    private FavoritesUtil mFavoritesUtil;
    private boolean mIsPagerLoaded;
    private ProductPagerFragment mProductPagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mProductBundle = getIntent().getParcelableExtra(Products.PRODUCT_MODEL);
        if (mProductBundle != null)
        {
            setupTransitions();
            setContentView(R.layout.activity_product);

            Otto.register(this);
            ButterKnife.bind(this);

            setupFirebaseStorageDb();

            //Setting up the UI
            setUpActionBarUi();
            showProductsFirstImage();
            setupProductInfoUi();
            setupEditingOptionsUi();
            setupFavoriteUi();
            setupSizesUi();
        }
        else
        {
            toast(R.string.invalid_product);
            finish();
        }
    }

    private void disableUi(TextView view)
    {
        view.setBackground(mDisableSizeButtonDrawable);
        view.setTextColor(colorGrey);
    }

    private void setupTransitions()
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            getWindow().setSharedElementExitTransition(TransitionInflater.from(this)
                    .inflateTransition(R.transition.shared_product_transition));
            getWindow().setSharedElementEnterTransition(TransitionInflater.from(this)
                    .inflateTransition(R.transition.shared_product_transition));
        }
    }

    private void setupFirebaseStorageDb()
    {
        DatabaseReference root = FirebaseDatabase.getInstance().getReference();
        mProductDbRef = root.child(mProductBundle.getCategoryId()).child(mProductBundle.getProductId());
        mProductDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (!ProductActivity.this.isDestroyed() && dataSnapshot != null)
                {
                    mProductBundle = new ProductBundle(dataSnapshot.getValue(Products.class));
                    setupProductInfoUi();
                    resetSizesUi();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        mProductStorageRef = FirebaseStorage.getInstance().getReference()
                .child(mProductBundle.getCategoryId())
                .child(mProductBundle.getProductId());
    }

    private void setUpActionBarUi()
    {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void showProductsFirstImage()
    {
        if(mProductBundle.getPhotoUrlList() != null)
        {
            Glide.with(this).load(mProductBundle.getPhotoUrlList().get(0))
                    .crossFade()
                    .centerCrop().into(mTransitionImage);
        }
    }

    private void setupProductInfoUi()
    {
        mBrandText.setText(mProductBundle.getBrand());
        mPriceTextAfter.setText(mProductBundle.getPriceAfter());
        String discountText = mProductBundle.getPriceBefore();
        if (discountText != null && !discountText.isEmpty())
        {
            mPriceTextBefore.setText(discountText);
            mPriceTextBefore.setPaintFlags(mPriceTextBefore.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void setupEditingOptionsUi()
    {
        if (Brandfever.getPreferences().getBoolean(Accounts.IS_OWNER, false))
        {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mDeleteProductImageView.setVisibility(View.VISIBLE);
                    mEditProductDetailsImageView.setVisibility(View.VISIBLE);
                }
            }, DELAY);
        }
    }

    private void setupFavoriteUi()
    {
        mFavoritesUtil = FavoritesUtil.getsInstance();
        mIsFavorite = mFavoritesUtil.isFavorite(mProductBundle);
        updateFavoriteUi();
    }

    private void setupSizesUi()
    {
        mSelectedSizes = new HashMap<>();
        mSelectedSizes.put(XS, false);
        mSelectedSizes.put(S, false);
        mSelectedSizes.put(M, false);
        mSelectedSizes.put(L, false);
        mSelectedSizes.put(XL, false);
        mSelectedSizes.put(XXL, false);
        resetSizesUi();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (!mIsPagerLoaded)
        {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    updateProductImagesFragment();
                    mIsPagerLoaded = true;
                }
            }, DELAY);
        }
    }

    private void updateProductImagesFragment()
    {
        showProductsFirstImage();
        mProductPagerFragment = new ProductPagerFragment();
        mProductPagerFragment.updateFragmentData(mProductBundle, mProductStorageRef, mProductDbRef);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.pagerFragmentContainer, mProductPagerFragment, PRODUCT_PAGER_FRAGMENT)
                .commit();
    }

    @Override
    public void onBackPressed()
    {
        removePagerFragment();
        super.onBackPressed();
    }

    private void removePagerFragment()
    {
        getSupportFragmentManager().beginTransaction()
                .remove(mProductPagerFragment)
                .commit();
    }

    @OnClick({R.id.xs, R.id.s, R.id.m, R.id.l, R.id.xl, R.id.xxl,
            R.id.xsWrapper, R.id.sWrapper, R.id.mWrapper, R.id.lWrapper, R.id.xlWrapper, R.id.xxlWrapper})
    public void selectSize(View view)
    {
        resetSizesUi();
        String selectedSize = view.getTag().toString();
        switch (selectedSize)
        {
            case XS:
                checkAvailabilitySelect(XS, xs);
                //xsSpinner.performClick();
                break;
            case S:
                checkAvailabilitySelect(S, s);
                //sSpinner.performClick();
                break;
            case M:
                checkAvailabilitySelect(M, m);
                //mSpinner.performClick();
                break;
            case L:
                checkAvailabilitySelect(L, l);
                //lSpinner.performClick();
                break;
            case XL:
                checkAvailabilitySelect(XL, xl);
                //xlSpinner.performClick();
                break;
            case XXL:
                checkAvailabilitySelect(XXL, xxl);
                //xxlSpinner.performClick();
                break;
        }
    }

    private void checkAvailabilitySelect(String key, TextView view)
    {
        if (mProductBundle.getSizesMap().getInt(key) > 0)
        {
            highlight(view);
            mSelectedSizes.put(key, true);
        }
        else
        {
            toast(R.string.sizeNotAvailable);
        }
    }

    private void resetSizesUi()
    {
        checkAvailability(XS, xs);
        checkAvailability(S, s);
        checkAvailability(L, l);
        checkAvailability(M, m);
        checkAvailability(XL, xl);
        checkAvailability(XXL, xxl);

        for (String key : mSelectedSizes.keySet())
        {
            mSelectedSizes.put(key, false);
        }
    }

    private void checkAvailability(String key, TextView view)
    {
        Bundle bundle = mProductBundle.getSizesMap();
        if (bundle != null && bundle.getInt(key) > 0)
        {
            unHighlight(view);
        }
        else
        {
            disableUi(view);
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
        Log.d("AddToCart", "Clicked");
        if (ConnectivityUtil.isConnected())
        {
            Log.d("AddToCart", "isConnected");
            Log.d("AddToCart", "if mIsAddedToCart");
            String selectedSize = getSelectedSize();
            if (selectedSize != null)
            {
                mProductBundle.setSelectedSize(selectedSize);
                CartUtil.getsInstance().addToCart(mProductBundle);
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
            toast(R.string.noInternet);
        }
    }

    private String getSelectedSize()
    {
        for (String key : mSelectedSizes.keySet())
        {
            if (mSelectedSizes.get(key))
            {
                return key;
            }
        }
        return null;
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
        setupProductInfoUi();
    }

    @Subscribe
    public void reloadProductImagesFragment(String action)
    {
        if (action.equals(ProductPagerFragment.IMAGE_DELETED))
        {
            ProgressDialog dialog = new ProgressDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage(getString(R.string.deleting));
            Handler handler = new Handler();
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    updateProductImagesFragment();
                }
            }, DELAY);

        }
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
                    for (String name : mProductBundle.getPhotoNameList())
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
}