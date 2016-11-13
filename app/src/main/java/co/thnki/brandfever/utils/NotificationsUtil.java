package co.thnki.brandfever.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.NotificationModel;
import co.thnki.brandfever.interfaces.ResultListener;
import co.thnki.brandfever.singletons.VolleyUtil;

import static co.thnki.brandfever.firebase.fcm.NotificationInstanceIdService.NOTIFICATION_INSTANCE_ID;
import static co.thnki.brandfever.singletons.VolleyUtil.RECEIVER_TOKENS;

public class NotificationsUtil
{
    public static final String TAG = "Notifications";
    private NotificationManager mNotificationManager;
    private static NotificationsUtil sInstance;
    private SharedPreferences mPreferences;
    private DatabaseReference mRootDbRef;

    private NotificationsUtil()
    {
        mNotificationManager = (NotificationManager) Brandfever.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
        mPreferences = Brandfever.getPreferences();
        mRootDbRef = FirebaseDatabase.getInstance().getReference();
    }

    public static NotificationsUtil getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new NotificationsUtil();
        }
        return sInstance;
    }

    public void showNotification(NotificationModel data)
    {
        if (data.photoUrl == null)
        {
            showNotification(data, null);
        }
        else
        {
            new ShowNormalNotification().execute(data);
        }
    }

    private void showNotification(NotificationModel model, Bitmap mLargeIcon)
    {
        Context mAppContext = Brandfever.getAppContext();
        Intent contentIntent = new Intent(Brandfever.getAppContext(), StoreActivity.class);

        contentIntent.putExtra(StoreActivity.NOTIFICATION_ACTION, model.action);
        PendingIntent contentPendingIntent = PendingIntent.getBroadcast(Brandfever.getAppContext(),
                (int) System.currentTimeMillis(), contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mAppContext);
        mBuilder.setContentTitle(model.username)
                .setSmallIcon(R.drawable.ic_shopping_cart_white_24dp)
                .setAutoCancel(true)
                .setContentText(model.notification)
                .setContentIntent(contentPendingIntent);

        if (mLargeIcon != null)
        {
            mBuilder.setLargeIcon(mLargeIcon);
        }

        Notification notificationDefault = new Notification();
        notificationDefault.defaults |= Notification.DEFAULT_LIGHTS; // LED
        notificationDefault.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
        notificationDefault.defaults |= Notification.DEFAULT_SOUND; // Sound
        mBuilder.setDefaults(notificationDefault.defaults);
        mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
    }

    private Bitmap getCircleBitmapFromUrl(String photoUrl)
    {
        try
        {
            URL url = new URL(photoUrl);
            return getCircleBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, " Error : " + e.getMessage());
        }
        return BitmapFactory.decodeResource(Brandfever.getAppContext().getResources(), R.mipmap.app_icon);
    }

    private class ShowNormalNotification extends AsyncTask<NotificationModel, Void, Void>
    {
        NotificationModel mModel;
        Bitmap mLargeIcon;

        @Override
        protected Void doInBackground(NotificationModel... params)
        {
            mModel = params[0];
            mLargeIcon = getCircleBitmapFromUrl(mModel.photoUrl);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            showNotification(mModel, mLargeIcon);
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap)
    {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();
        return output;
    }

    public void removeNotification(int id)
    {
        mNotificationManager.cancel(id);
    }

    private void saveNotificationInAllOwnersProfile(NotificationModel notificationModel, String userGoogleId)
    {
        /**
         *  //TODO check if notificationModel already exits and then save it.
         */
        Log.d(TAG, "saveNotificationInAllOwnersProfile : " + notificationModel.toString());
        saveNotification(notificationModel, userGoogleId);
        for (String googleId : Brandfever.getPreferences().getStringSet(Accounts.OWNERS_GOOGLE_IDS, new HashSet<String>()))
        {
            saveNotification(notificationModel, googleId);
        }
    }

    private void saveNotification(NotificationModel notificationModel, String googleId)
    {
        if (googleId != null && !googleId.equals(mPreferences.getString(Accounts.GOOGLE_ID, "")))
        {
            Log.d(TAG, "Owner googleId : " + googleId);
            DatabaseReference ownersNotificationDbRef = mRootDbRef.child(googleId).child(NotificationsUtil.TAG);
            ownersNotificationDbRef.push().setValue(notificationModel);
        }
    }

    public void sendNotificationToAllOwners(NotificationModel model, String googleId, ResultListener<String> listener)
    {
        saveNotificationInAllOwnersProfile(model, googleId);
        Log.d(TAG, "sendNotificationToAllOwners");
        Map<String, String> data = new HashMap<>();
        data.put(NotificationModel.GOOGLE_ID, model.googleId);
        data.put(NotificationModel.ACTION, model.action);
        data.put(NotificationModel.APP_ID, mPreferences.getString(VolleyUtil.APP_ID, Brandfever.getResString(R.string.serverKey)));
        data.put(NotificationModel.NOTIFICATION, model.notification);
        data.put(NotificationModel.PHOTO_URL, model.photoUrl);
        data.put(NotificationModel.USERNAME, model.username);

        @SuppressLint("UseSparseArrays")
        Map<String, String> ownersTokenList = new HashMap<>();
        int index = 0;

        for (String token : mPreferences.getStringSet(Accounts.OWNERS_TOKENS, new HashSet<String>()))
        {
            if (token != null && !token.equals(mPreferences.getString(NOTIFICATION_INSTANCE_ID, "")))
            {
                ownersTokenList.put("" + (index++), token);
            }
        }

        data.put(RECEIVER_TOKENS, new JSONObject(ownersTokenList).toString());
        Log.d(TAG, "Request : " + data);
        VolleyUtil.getInstance().sendPostData(data, listener);
    }
}