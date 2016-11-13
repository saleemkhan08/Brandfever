package co.thnki.brandfever.firebase.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import co.thnki.brandfever.firebase.database.models.NotificationModel;
import co.thnki.brandfever.utils.NotificationsUtil;

public class NotificationReceiverService extends FirebaseMessagingService
{
    private static final String TAG = "FbNotificationService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();

        NotificationModel model = new NotificationModel();

        model.photoUrl = data.get(NotificationModel.PHOTO_URL);
        model.username = data.get(NotificationModel.USERNAME);
        model.googleId = data.get(NotificationModel.GOOGLE_ID);
        model.action = data.get(NotificationModel.ACTION);
        model.notification = data.get(NotificationModel.NOTIFICATION);

        NotificationsUtil.getInstance().showNotification(model);
        Log.d(TAG, "Data : "+data);

    }
}
