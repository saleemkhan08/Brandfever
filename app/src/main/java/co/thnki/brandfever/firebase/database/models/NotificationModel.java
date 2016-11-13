package co.thnki.brandfever.firebase.database.models;

public class NotificationModel
{
    public String username;
    public String photoUrl;
    public String notification;
    public String googleId;
    public String action;
    public boolean isRead;

    public static final String USERNAME = "username";
    public static final String PHOTO_URL = "photoUrl";
    public static final String NOTIFICATION = "notification";
    public static final String GOOGLE_ID = "googleId";
    public static final String ACTION = "action";
    public static final String IS_READ = "isRead";
    public static final String APP_ID = "appId";


    @Override
    public String toString()
    {
        return "NotificationModel > username : " + username + ", googleId : " + googleId + ", action : " + action;
    }
}
