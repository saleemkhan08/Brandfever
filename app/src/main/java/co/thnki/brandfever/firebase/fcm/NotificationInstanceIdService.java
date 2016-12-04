package co.thnki.brandfever.firebase.fcm;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import co.thnki.brandfever.utils.UserUtil;

public class NotificationInstanceIdService extends FirebaseInstanceIdService
{
    public static final String NOTIFICATION_INSTANCE_ID = "NotificationInstanceId";

    @Override
    public void onTokenRefresh()
    {
        String notificationInstanceId = FirebaseInstanceId.getInstance().getToken();
        new UserUtil().updateNotificationInstanceId(notificationInstanceId);
    }
}
