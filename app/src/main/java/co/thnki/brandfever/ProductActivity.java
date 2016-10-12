package co.thnki.brandfever;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.BindDrawable;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.adapters.SectionsPagerAdapter;
import co.thnki.brandfever.firebase.database.models.ProductBundle;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.fragments.EditProductDialogFragment;
import co.thnki.brandfever.singletons.Otto;

import static co.thnki.brandfever.Brandfever.toast;
import static co.thnki.brandfever.R.layout.indicator;
import static co.thnki.brandfever.firebase.database.models.Products.PHOTO_NAME;
import static co.thnki.brandfever.firebase.database.models.Products.PHOTO_URL;
import static co.thnki.brandfever.fragments.UploadCategoriesFragment.PICK_IMAGE_MULTIPLE;

public class ProductActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener
{
    private static final String EDITOR = "productEditor";

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

    //private String mCurrentCategory;
    private boolean mIsFavorite;

    @BindColor(R.color.background)
    int statusBarColor;
    private ProductBundle mProductBundle;
    private ProgressDialog mProgressDialog;

    private ArrayList<String> mPhotoUrlList;
    private ArrayList<String> mPhotoNameList;

    private DatabaseReference mProductDbRef;
    private StorageReference mProductStorageRef;
    private SectionsPagerAdapter mAdapter;

    private String mCurrentPhotoUrl = "";
    private String mCurrentPhotoName = "";

    @Bind(R.id.favorite)
    ImageView mFavoriteImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        Otto.register(this);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mProductBundle = getIntent().getParcelableExtra(Products.PRODUCT_MODEL);
        mAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        updateUi(mProductBundle);
        mProductImagePager.setAdapter(mAdapter);
        mProductImagePager.addOnPageChangeListener(this);
        mProductDbRef = FirebaseDatabase.getInstance().getReference()
                .child(mProductBundle.getCategoryId()).child(mProductBundle.getProductId());
        mProductStorageRef = FirebaseStorage.getInstance().getReference()
                .child(mProductBundle.getCategoryId())
                .child(mProductBundle.getProductId());
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


    @OnClick({R.id.xs, R.id.s, R.id.m, R.id.l, R.id.xl, R.id.xxl,
            R.id.xsWrapper, R.id.sWrapper, R.id.mWrapper, R.id.lWrapper, R.id.xlWrapper, R.id.xxlWrapper})
    public void selectSize(View view)
    {
        updateSizesUi();
        switch (view.getId())
        {
            case R.id.xs:
            case R.id.xsWrapper:
                highlight(xs);
                break;

            case R.id.s:
                highlight(s);
            case R.id.sWrapper:
                break;

            case R.id.m:
            case R.id.mWrapper:
                highlight(m);
                break;

            case R.id.l:
            case R.id.lWrapper:
                highlight(l);
                break;

            case R.id.xl:
            case R.id.xlWrapper:
                highlight(xl);
                break;

            case R.id.xxl:
            case R.id.xxlWrapper:
                highlight(xxl);
                break;
        }
    }

    private void updateSizesUi()
    {
        unHighlight(xs);
        unHighlight(s);
        unHighlight(m);
        unHighlight(l);
        unHighlight(xl);
        unHighlight(xxl);
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
    public void saveToFavorite(ImageView favorite)
    {
        mIsFavorite = ! mIsFavorite;
        updateFavoriteUi();
    }

    private void updateFavoriteUi()
    {

        if (mIsFavorite)//if it was not favorite
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

    private void updatePageIndicator(int size)
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

    @OnClick(R.id.edit)
    public void launchProductEditor()
    {
        EditProductDialogFragment mEditProductDialogFragment = EditProductDialogFragment.getInstance(mProductBundle);
        mEditProductDialogFragment.show(getSupportFragmentManager(), EDITOR);
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
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
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
                uploadMorePhotos(mImagesEncodedList);
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
    public void onPageSelected(int position)
    {
        highLightPageIndicator(position);
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
                for(String name : mPhotoNameList)
                {
                    mProductStorageRef.child(name).delete();
                }
                mProductStorageRef.delete();
                finish();
            }
        }).show();
    }

    @OnClick(R.id.deletePhoto)
    public void deleteImage()
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
}