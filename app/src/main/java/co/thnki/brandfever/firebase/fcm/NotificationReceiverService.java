package co.thnki.brandfever.firebase.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class NotificationReceiverService extends FirebaseMessagingService
{
    private static final String TAG = "FbNotificationService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Data : "+remoteMessage.getData());

    }
}
