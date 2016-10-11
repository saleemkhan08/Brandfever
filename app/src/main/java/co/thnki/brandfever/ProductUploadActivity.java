package co.thnki.brandfever;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindColor;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.adapters.ProductImagesAdapter;
import co.thnki.brandfever.firebase.database.models.Products;

import static co.thnki.brandfever.Brandfever.toast;
import static co.thnki.brandfever.interfaces.Const.CATEGORY_ID;

public class ProductUploadActivity extends AppCompatActivity
{
    public static final String PRODUCT_IMAGES = "productImages";

    @Bind(R.id.productRecyclerView)
    RecyclerView mProductRecyclerView;
    private ProductImagesAdapter mAdapter;
    private DatabaseReference mCategoryRef;
    private String mCurrentCategory;
    private StorageReference mCategoryStorageRef;
    private ArrayList<String> mProductImages;
    private ProgressDialog mProgressDialog;
    @BindColor(R.color.colorPrimaryDark)
    int COLOR_PRIMARY_DARK;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_upload);
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            getWindow().setStatusBarColor(COLOR_PRIMARY_DARK);
        }
        mProgressDialog = new ProgressDialog(this);
        Bundle bundle = getIntent().getBundleExtra(PRODUCT_IMAGES);
        if(bundle != null)
        {
            mProductImages = bundle.getStringArrayList(PRODUCT_IMAGES);
            mCurrentCategory = bundle.getString(CATEGORY_ID);
            if(mCurrentCategory != null)
            {
                mCategoryRef = FirebaseDatabase.getInstance().getReference().child(mCurrentCategory);
                mCategoryStorageRef = FirebaseStorage.getInstance().getReference().child(mCurrentCategory);
            }
            else
            {
                toast(R.string.youHaventSelectedAnyImages);
                finish();
            }
            mProductRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            mAdapter = new ProductImagesAdapter(mProductImages);
            mProductRecyclerView.setAdapter(mAdapter);
        }
        else
        {
            toast(R.string.youHaventSelectedAnyImages);
            finish();
        }

    }

    @OnClick(R.id.individual)
    public void individualProducts()
    {
        //upload(mAdapter.mSelectImageMap);
        /**
         * 1. push and create a database entry
         * 2. upload the image with the obtained key name
         * 3. save the url in the database.
         */
        mProgressDialog.setMessage("Uploading");
        mProgressDialog.show();
        for(String uri : mProductImages)
        {
            if(mAdapter.mSelectImageMap.get(uri))
            {
                final DatabaseReference product = mCategoryRef.push();
                final String key = product.getKey();
                StorageReference reference = mCategoryStorageRef.child(key);
                reference.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        mProgressDialog.dismiss();

                        Uri downloadUri = taskSnapshot.getDownloadUrl();
                        if(downloadUri !=null)
                        {
                            product.child(Products.PHOTO_URL).push().setValue(downloadUri.toString());
                        }
                        else
                        {
                            product.removeValue();
                        }
                        mAdapter.removeSelectedElements();
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        mProgressDialog.dismiss();
                        toast(R.string.please_try_again);
                        product.removeValue();
                    }
                });
            }
        }
    }

    @OnClick(R.id.group)
    public void groupProducts()
    {
        /*for(Uri uri : mImagesEncodedList)
        {
            *//*
            final DatabaseReference childRef = mCategoriesRef.push();
            String key = childRef.getKey();
            mStorageRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    taskSnapshot.getDownloadUrl();
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {

                }
            });
        }*/
    }
}
