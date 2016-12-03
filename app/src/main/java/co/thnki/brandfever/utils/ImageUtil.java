package co.thnki.brandfever.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;

import static android.os.Environment.DIRECTORY_PICTURES;

public class ImageUtil
{
    //public ImageLoader mImageLoader;
    public static final int LOAD_GALLERY_REQUEST = 303;
    private AppCompatActivity mActivity;
    public static Uri imageUri;
    private static final int MEDIA_TYPE_IMAGE = 1;
    private static final int MEDIA_TYPE_VIDEO = 2;
    public static final int CAPTURE_IMAGE_REQUEST = 301;
    public static final int RECORD_VIDEO_REQUEST = 302;
    public static final String IS_PHOTO = "IS_PHOTO";
    public static final String IMAGE_DIRECTORY_NAME = "WhistleBlower";
    private File mediaStorageDir;
    public static final String STORAGE_PATH = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES)
            .getAbsolutePath() + "/Shoppon/";
    //DisplayImageOptions dpOptions, issueOptions;

    public ImageUtil(AppCompatActivity activity)
    {
        mActivity = activity;
    }

    public void displayRoundedImage(String photo_url, final ImageView imageView)
    {
        Glide.with(mActivity)
                .load(photo_url)
                .asBitmap()
                .placeholder(R.mipmap.user_icon_accent)
                .centerCrop()
                .into(new BitmapImageViewTarget(imageView)
                {
                    @Override
                    protected void setResource(Bitmap resource)
                    {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(mActivity.getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        imageView.setImageDrawable(circularBitmapDrawable);
                    }
                });
    }

    public void displayImage(String photo_url, ImageView view)
    {
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(view);
        Glide.with(mActivity).load(photo_url).crossFade().into(imageViewTarget);
    }

    public void displayGif(int photo_url_resource, ImageView view)
    {
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(view);
        Glide.with(mActivity).load(photo_url_resource).crossFade().into(imageViewTarget);
    }

    public static int pixels(Context mContext, double dp)
    {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        double pixels = metrics.density * dp;
        return (int) pixels;
    }

    private static int dp(Context mContext, double pixels)
    {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        double dp = pixels / metrics.density;
        return (int) dp;
    }

    public static int getAdWidth(AppCompatActivity context)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthInPix = metrics.widthPixels;
        int width = dp(context, widthInPix);
        if (width < 1200)
        {
            return width;
        }
        return 1200;
    }

    public File getMediaStorageDir()
    {
        mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists())
        {
            if (mediaStorageDir.mkdirs())
            {
                mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            }
        }
        return mediaStorageDir;
    }

    public void pickImage()
    {

    }

    private Uri getOutputMediaFileUri(int type)
    {
        imageUri = Uri.fromFile(getOutputMediaFile(type));
        return imageUri;
    }

    private File getOutputMediaFile(int type)
    {
        // Create a media file name
        if (mediaStorageDir == null)
        {
            mediaStorageDir = getMediaStorageDir();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE)
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        }
        else if (type == MEDIA_TYPE_VIDEO)
        {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        }
        else
        {
            return null;
        }
        return mediaFile;
    }

    public static File saveBitmapToFile(Uri uri, String key)
    {
        try
        {
            // BitmapFactory options to downsize the image
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 6;
            // factor of downsizing the image

            InputStream inputStream = Brandfever.getAppContext().getContentResolver().openInputStream(uri);
            Log.d("SizeReduction", "destFile : "+inputStream);
            //Bitmap selectedBitmap = null;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // The new size we want to scale to
            final int REQUIRED_SIZE = 150;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (options.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    options.outHeight / scale / 2 >= REQUIRED_SIZE)
            {
                scale *= 2;
            }

            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = scale;

            inputStream = Brandfever.getAppContext().getContentResolver().openInputStream(uri);

            Bitmap selectedBitmap = BitmapFactory.decodeStream(inputStream, null, options2);
            inputStream.close();

            File destFile = new File(STORAGE_PATH + key+".jpg");
            File storageDir = new File(STORAGE_PATH);
            Log.d("SizeReduction", "storageDir : "+storageDir);
            Log.d("SizeReduction", "destFile : "+destFile);

            if (!destFile.exists())
            {
                try
                {
                    if (!storageDir.exists())
                    {
                        if (!storageDir.mkdir())
                        {
                            return null;
                        }
                    }
                    if (!destFile.createNewFile())
                    {
                        return null;
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return null;
                }
            }
            // here i override the original image file
            FileOutputStream outputStream = new FileOutputStream(destFile);
            if(selectedBitmap.getAllocationByteCount() > 200000)
            {
                selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            }else{
                selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            }
            return destFile;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d("SizeReduction", "saveFile : "+e.getMessage());
            return null;
        }
    }
}
