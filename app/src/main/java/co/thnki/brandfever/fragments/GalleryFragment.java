package co.thnki.brandfever.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.adapters.ShopGalleryPagerAdapter;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.singletons.Otto;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.utils.ImageUtil;
import co.thnki.brandfever.utils.UserUtil;

import static android.app.Activity.RESULT_OK;
import static co.thnki.brandfever.Brandfever.toast;
import static co.thnki.brandfever.R.layout.indicator;
import static co.thnki.brandfever.fragments.ProductsFragment.PICK_IMAGE_MULTIPLE;

public class GalleryFragment extends Fragment implements ViewPager.OnPageChangeListener
{
    private static final String SHOP_GALLERY_IMAGES = "shopGallery";
    @Bind(R.id.galleryImagePager)
    ViewPager mGalleryImagePager;

    private RemindTask mTask;
    @Bind(R.id.pageIndicatorContainer)
    LinearLayout mPageIndicatorContainer;

    @Bind(R.id.deletePhoto)
    ImageView mDeletePhotoImageView;

    @Bind(R.id.uploadMorePhotos)
    ImageView mUploadPhotoImageView;

    private ShopGalleryPagerAdapter mAdapter;
    public ArrayList<String> mPhotoUrlList;
    public ArrayList<String> mPhotoNameList;

    public Map<String, String> mGalleryPhotosMap;

    private String mCurrentPhotoUrl = "";
    private String mCurrentPhotoName = "";
    private ProgressDialog mProgressDialog;
    private StorageReference mStorageRef;
    private DatabaseReference mDbRef;
    private volatile boolean mIsCancelled = false;

    private Timer mTimer;
    private int mPageNum = 1;

    private Handler mHandler = new Handler();
    private StoreActivity mActivity;

    public GalleryFragment()
    {
        mStorageRef = FirebaseStorage.getInstance().getReference().child(SHOP_GALLERY_IMAGES);
        mDbRef = FirebaseDatabase.getInstance().getReference().child(UserUtil.APP_DATA).child(SHOP_GALLERY_IMAGES);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parentView = inflater.inflate(R.layout.fragment_gallery, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        return parentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mActivity = (StoreActivity) getActivity();
        updateEditingOptionsUi();
        mAdapter = new ShopGalleryPagerAdapter(mActivity.getSupportFragmentManager());
        mDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    if (dataSnapshot != null && dataSnapshot.getChildrenCount() >= 1 && getActivity() != null)
                    {
                        mGalleryPhotosMap = new LinkedHashMap<>();
                        mPhotoUrlList = new ArrayList<>();
                        mPhotoNameList = new ArrayList<>();
                        mGalleryPhotosMap = (Map<String, String>) dataSnapshot.getValue();
                        for (Map.Entry<String, String> set : mGalleryPhotosMap.entrySet())
                        {
                            mPhotoUrlList.add(set.getValue());
                            mPhotoNameList.add(set.getKey());
                        }
                        mAdapter.updateDataSet(mPhotoUrlList);
                        mGalleryImagePager.setAdapter(mAdapter);
                        mGalleryImagePager.addOnPageChangeListener(GalleryFragment.this);
                        mGalleryImagePager.setVisibility(View.VISIBLE);
                        updatePageIndicator(mPhotoUrlList.size());
                        pageSwitcher();
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mGalleryImagePager.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent)
            {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
                {
                    Log.d("TouchLog", "ACTION_BUTTON_PRESS");
                    if (mTimer != null)
                    {
                        mTimer.cancel();
                    }
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP)
                {

                    pageSwitcher();
                    Log.d("TouchLog", "ACTION_BUTTON_RELEASE");
                }
                return false;
            }
        });
    }

    private void pageSwitcher()
    {
        if (!Brandfever.getPreferences().getBoolean(Accounts.IS_OWNER, false))
        {
            mHandler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        mTimer = new Timer(); // At this line a new Thread will be created
                        if (mTask != null)
                        {
                            mTask.cancel();
                        }
                        mTask = new RemindTask();
                        mTimer.scheduleAtFixedRate(mTask, 0, 3000); // delay
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.d("Exception", "Exception : " + e.getMessage());
                    }
                }
            }, 6000);
        }
    }

    @OnClick(R.id.deletePhoto)
    public void deleteImage()
    {
        if (ConnectivityUtil.isConnected())
        {
            if (mPhotoUrlList.size() > 1)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
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

                        int position = mPageNum - 1;
                        if (position < 0)
                        {
                            position = 0;
                        }
                        else if (position >= mPhotoNameList.size())
                        {
                            position = mPhotoNameList.size() - 1;
                        }
                        Log.d("DeleteError", position + " : " + mCurrentPhotoName + " = " + mPhotoNameList.get(position));
                        mStorageRef.child(mPhotoNameList.get(position)).delete();
                        mPhotoNameList.remove(position);
                        mPhotoUrlList.remove(position);
                        mGalleryPhotosMap.remove(mPhotoNameList.get(position));
                        mDbRef.setValue(mGalleryPhotosMap);
                        mAdapter.notifyDataSetChanged();
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

    @OnClick(R.id.uploadMorePhotos)
    public void uploadMorePhotos()
    {
        if (ConnectivityUtil.isConnected())
        {
            if (mActivity.isSdcardPermissionAvailable())
            {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
            }
            else
            {
                mActivity.requestSdCardPermission();
            }
        }
        else
        {
            toast(R.string.noInternet);
        }
    }

    @Subscribe
    public void getImages(String action)
    {
        if (action.equals(StoreActivity.ON_REQUEST_PERMISSION_RESULT))
        {
            uploadMorePhotos();
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
        final int currentSize = mPhotoUrlList.size();
        final int noOfUploadingPhoto = mImagesEncodedList.size();
        setupProgressDialog(noOfUploadingPhoto);

        Log.d("PhotoUploadFlow", "mImagesEncodedList : " + mImagesEncodedList.size());
        Log.d("PhotoUploadFlow", "currentSize : " + currentSize);

        for (int productIndex = 0; productIndex < noOfUploadingPhoto && !mIsCancelled; productIndex++)
        {
            String uri = mImagesEncodedList.get(productIndex);
            int uploadIndex = productIndex + currentSize;
            Log.d("PhotoUploadFlow", "uploadIndex : " + uploadIndex);

            /**
             * Put the photo using random key name does not matter as it will be directly accessed using the download URL
             */
            final String photoName = Products.generateRandomKey();

            Uri fileUri = Uri.parse(uri);
            final File destFile = ImageUtil.saveBitmapToFile(fileUri, photoName);
            StorageReference reference = mStorageRef.child(photoName);
            reference.putFile(Uri.fromFile(destFile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
            {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                {
                    try
                    {
                        Log.d("PhotoUploadFlow", "onSuccess");
                        updateProgressDialog(taskSnapshot, currentSize,
                                photoName, noOfUploadingPhoto);
                        if (destFile != null && destFile.exists())
                        {
                            destFile.delete();
                        }
                    }
                    catch (Exception e)
                    {
                        Log.d("PhotoUploadFlow", "Exception " + e.getMessage());
                    }
                }
            }).addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                    mProgressDialog.dismiss();
                    Log.d("PhotoUploadFlow", "onFailure");
                    toast(R.string.please_try_again);
                }
            });
        }
    }

    private void updateProgressDialog(UploadTask.TaskSnapshot taskSnapshot, int currentSize,
                                      String photoName, int noOfUploadingPhoto)
    {
        Log.d("PhotoUploadFlow", "updateProgressDialog");
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

                mGalleryPhotosMap.put(photoName, url);
                mDbRef.setValue(mGalleryPhotosMap);
            }
        }
        Log.d("PhotoUploadFlow", "listSize before : " + listSize + ", after : " + mPhotoUrlList.size());
        Log.d("PhotoUploadFlow", "Size : " + size + ",currentSize : " + currentSize + ", noOfUploadingPhoto : " + noOfUploadingPhoto);

        if (size >= noOfUploadingPhoto)
        {
            if (mActivity != null)
            {
                if (!mIsCancelled)
                {
                    mProgressDialog.dismiss();
                }
                else
                {
                    toast(R.string.uploaded);
                }
                mGalleryImagePager.setCurrentItem(0, false);
                mGalleryImagePager.setCurrentItem(mPhotoUrlList.size() - 1, true);
            }
            return;
        }
        String msg = "Uploading " + (size + 1) + " of " + noOfUploadingPhoto;
        if (!mIsCancelled)
        {
            mProgressDialog.setMessage(msg);
        }
        else
        {
            toast(msg);
        }
    }

    private void setupProgressDialog(int noOfUploadingPhoto)
    {
        mIsCancelled = false;
        if (mProgressDialog == null)
        {
            mProgressDialog = new ProgressDialog(mActivity);
        }

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialogInterface)
            {
                mIsCancelled = true;
                toast(R.string.photosWillBeUploadedInBackground);
                mProgressDialog.dismiss();
            }
        });

        mProgressDialog.show();
        mProgressDialog.setMessage("Uploading " + 1 + " of " + noOfUploadingPhoto);
    }

    private void updatePageIndicator(final int size)
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

    class RemindTask extends TimerTask
    {

        @Override
        public void run()
        {
            // As the TimerTask run on a seprate thread from UI thread we have
            // to call runOnUiThread to do work on UI thread.
            mHandler.post(new Runnable()
            {
                public void run()
                {
                    mPageNum++;
                    if (mPageNum == mPhotoNameList.size())
                    {
                        mPageNum = 0;
                    }
                    mGalleryImagePager.setCurrentItem(mPageNum);
                }
            });
        }
    }

    public View addIndicator()
    {
        return LayoutInflater.from(mActivity).inflate(indicator, mPageIndicatorContainer);
    }

    private void highLightPageIndicator(int position)
    {
        int size = mPageIndicatorContainer.getChildCount();
        for (int i = 0; i < size; i++)
        {
            ImageView indicator = (ImageView) ((LinearLayout) (mPageIndicatorContainer.getChildAt(i))).getChildAt(0);
            indicator.setImageResource(R.drawable.dot);
        }

        if (position >= 0 && position < size)
        {
            ImageView indicator = (ImageView) ((LinearLayout) (mPageIndicatorContainer.getChildAt(position))).getChildAt(0);
            indicator.setImageResource(R.drawable.accent_dot);
        }
    }

    private void updateEditingOptionsUi()
    {
        if (Brandfever.getPreferences().getBoolean(Accounts.IS_OWNER, false))
        {
            mDeletePhotoImageView.setVisibility(View.VISIBLE);
            mUploadPhotoImageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
    {

    }

    @Override
    public void onPageSelected(int position)
    {
        highLightPageIndicator(position);
        mPageNum = position;
        mCurrentPhotoUrl = mAdapter.getItemUrl(position);
        mCurrentPhotoName = mPhotoNameList.get(position);
    }

    @Override
    public void onPageScrollStateChanged(int state)
    {

    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (mTimer != null)
        {
            mTimer.cancel();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Otto.unregister(this);
    }
}
