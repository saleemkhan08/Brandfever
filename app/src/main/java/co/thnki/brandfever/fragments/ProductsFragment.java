package co.thnki.brandfever.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.ProductActivity;
import co.thnki.brandfever.R;
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.ViewHolders.ProductViewHolder;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.ProductBundle;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.utils.ConnectivityUtil;

import static android.app.Activity.RESULT_OK;
import static co.thnki.brandfever.Brandfever.getResString;
import static co.thnki.brandfever.Brandfever.toast;
import static co.thnki.brandfever.interfaces.Const.AVAILABLE_;


public class ProductsFragment extends Fragment
{
    public static final int PICK_IMAGE_MULTIPLE = 102;

    @Bind(R.id.productsRecyclerView)
    RecyclerView mProductRecyclerView;

    @Bind(R.id.uploadProducts)
    View mUploadButton;

    @Bind(R.id.recyclerProgress)
    View mProgress;


    @Bind(R.id.noProductFoundContainer)
    View mNoProductFoundContainer;
    private String mCurrentCategory;
    //private Toolbar mToolbar;
    private Resources mResources;
    StorageReference mStorageRef;
    private DatabaseReference mCategoryDbRef;
    private ProgressDialog mProgressDialog;
    private StorageReference mCategoryStorageRef;
    private FirebaseRecyclerAdapter mAdapter;

    public ProductsFragment()
    {
    }

    public static ProductsFragment getInstance(String category)
    {
        ProductsFragment fragment = new ProductsFragment();
        fragment.mCurrentCategory = category;
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        fragment.mCategoryDbRef = rootRef.child(category);
        fragment.mCategoryStorageRef = FirebaseStorage.getInstance().getReference().child(category);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_products, container, false);
        mResources = getResources();
        if (mCurrentCategory != null && !mCurrentCategory.isEmpty())
        {
            mStorageRef = FirebaseStorage.getInstance().getReference().child(mCurrentCategory);
        }
        ButterKnife.bind(this, parentView);
        mProgressDialog = new ProgressDialog(getActivity());
        mAdapter = getAdapter();
        mProductRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mProductRecyclerView.setAdapter(mAdapter);
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver()
        {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount)
            {
                super.onItemRangeInserted(positionStart, itemCount);
                updateUi();
            }
        });
        mCategoryDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                updateUi();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        return parentView;
    }

    private void updateUi()
    {
        mProgress.setVisibility(View.GONE);
        int size = mAdapter.getItemCount();
        Log.d("updateUi", "updateUi" + size + ", visible : " + mNoProductFoundContainer.isShown());
        if (size < 1)
        {
            mNoProductFoundContainer.setVisibility(View.VISIBLE);
        }
        else
        {
            mNoProductFoundContainer.setVisibility(View.GONE);
        }
        Log.d("updateUi", "updateUi" + size + ", visible : " + mNoProductFoundContainer.isShown());
    }


    @Override
    public void onResume()
    {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof StoreActivity)
        {
            ((StoreActivity) activity).setToolBarTitle(getCategoryName());
        }
        if(Brandfever.getPreferences().getBoolean(Accounts.IS_OWNER, false))
        {
            mUploadButton.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.uploadProducts)
    public void uploadProducts()
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

    /**
     * Uploading
     * 1. set url in product object
     * 2. save the product object in data base
     * <p>
     * showing :
     * 1. setup recycler view
     * 2. setup adapter
     * 3. setup database reference
     */

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
                    uploadIndividualProducts(mImagesEncodedList);
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
        }
    }

    private void uploadIndividualProducts(ArrayList<String> mImagesEncodedList)
    {
        //upload(mAdapter.mSelectImageMap);
        /**

         * 2. using the key create a folder in storage
         * 3. save the url in the database.
         */
        final int currentSize = mAdapter.getItemCount();
        final int noOfUploadingPhoto = mImagesEncodedList.size();
        mProgressDialog.setMessage("Uploading " + 1 + " of " + noOfUploadingPhoto);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        for (String uri : mImagesEncodedList)
        {
            /**
             * 1. push an empty product to create a key
             */

            final DatabaseReference product = mCategoryDbRef.push();
            final String key = product.getKey();

            /**
             * 2. create a folder in Storage using the key and
             * create a file reference using generateRandomKey Generator defined in Products Model
             */

            final String photoName = Products.generateRandomKey();
            StorageReference reference = mCategoryStorageRef.child(key).child(photoName);
            reference.putFile(Uri.parse(uri)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {

                    /**
                     * 3. put a file and get the download Url
                     */

                    Uri downloadUri = taskSnapshot.getDownloadUrl();
                    if (downloadUri != null)
                    {
                        /**
                         * 4. Create a product using download Url, Current Category and the key generated previously
                         */

                        Products products = new Products(downloadUri.toString(), photoName, mCurrentCategory, key);
                        product.setValue(products);
                    }
                    else
                    {
                        product.removeValue();
                    }

                    int size = mAdapter.getItemCount() - currentSize + 1;
                    Log.d("SizesIssue", "Size : " + size + ",currentSize : " + currentSize + ", noOfUploadingPhoto : " + noOfUploadingPhoto);
                    if (size >= noOfUploadingPhoto)
                    {
                        mProgressDialog.dismiss();
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
                    product.removeValue();
                }
            });
        }
    }

    private String getCategoryName()
    {
        Log.d("getCategoryName", mCurrentCategory);
        String categoryName = mCurrentCategory.replace(AVAILABLE_, "");
        List<String> menCategory = Arrays.asList(mResources.getStringArray(R.array.mensWearId));
        Log.d("getCategoryName", "" + menCategory);
        int index = menCategory.indexOf(categoryName);
        if (index >= 0)
        {
            return mResources.getStringArray(R.array.mensWear)[index];
        }
        else
        {
            List<String> womenCategory = Arrays.asList(mResources.getStringArray(R.array.womensWearId));
            Log.d("getCategoryName", "" + womenCategory);
            index = womenCategory.indexOf(categoryName);
            if (index >= 0)
            {
                return mResources.getStringArray(R.array.womensWear)[index];
            }
            else
            {
                List<String> kidsCategory = Arrays.asList(mResources.getStringArray(R.array.kidsWearId));
                Log.d("getCategoryName", "" + kidsCategory);
                index = kidsCategory.indexOf(categoryName);
                if (index >= 0)
                {
                    return mResources.getStringArray(R.array.kidsWear)[index];
                }
                else
                {
                    List<String> fashionCategory = Arrays.asList(mResources.getStringArray(R.array.fashionAccessoriesId));
                    Log.d("getCategoryName", "" + fashionCategory);
                    index = fashionCategory.indexOf(categoryName);
                    if (index >= 0)
                    {
                        return mResources.getStringArray(R.array.fashionAccessories)[index];
                    }
                    else
                    {
                        List<String> furnishingCategory = Arrays.asList(mResources.getStringArray(R.array.homeFurnishingId));
                        Log.d("getCategoryName", "" + furnishingCategory);
                        index = furnishingCategory.indexOf(categoryName);
                        if (index >= 0)
                        {
                            return mResources.getStringArray(R.array.homeFurnishing)[index];
                        }
                    }
                }
            }
        }
        return null;
    }

    private FirebaseRecyclerAdapter<Products, ProductViewHolder> getAdapter()
    {
        return new FirebaseRecyclerAdapter<Products, ProductViewHolder>(
                Products.class,
                R.layout.product_images_row,
                ProductViewHolder.class,
                mCategoryDbRef)
        {
            @Override
            protected void populateViewHolder(final ProductViewHolder viewHolder, final Products model, int position)
            {
                updateUi();
                /**
                 * get the zeroth item to display in the list.
                 */
                String imageUrl = model.getPhotoUrlList().get(0);
                Glide.with(getActivity()).load(imageUrl)
                        .asBitmap().placeholder(R.mipmap.price_tag)
                        .centerCrop().into(viewHolder.mImageView);

                viewHolder.mBrand.setText(model.getBrand());
                viewHolder.mPriceAfter.setText(model.getPriceAfter());

                String discountText = model.getPriceBefore();
                if (discountText != null && !discountText.isEmpty())
                {
                    viewHolder.mPriceBefore.setText(discountText);
                    viewHolder.mPriceBefore.setPaintFlags(viewHolder.mPriceBefore.getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG);
                }

                viewHolder.itemView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if (ConnectivityUtil.isConnected())
                        {
                            Intent intent = new Intent(getActivity(), ProductActivity.class);
                            ProductBundle bundle = new ProductBundle(model);
                            intent.putExtra(Products.PRODUCT_MODEL, bundle);
                            if(Build.VERSION.SDK_INT >= 21)
                            {
                                String transitionName = getResString(R.string.productTransitionImage);
                                viewHolder.mImageView.setTransitionName(transitionName);

                                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat
                                        .makeSceneTransitionAnimation(getActivity(), viewHolder.mImageView,
                                                transitionName);
                                startActivity(intent, optionsCompat.toBundle());
                            }
                            else
                            {
                                startActivity(intent);
                            }
                        }
                        else
                        {
                            toast(R.string.noInternet);
                        }
                    }
                });
            }
        };
    }
}
