package co.thnki.brandfever.firebase.database.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Accounts implements Parcelable
{
    public static final String IS_OWNER = "isOwner";
    public static final String OWNERS = "owners";
    public String email;
    public String name;
    public String googleId;
    public String photoUrl;
    public String fcmToken;

    public static final String EMAIL = "email";
    public static final String NAME = "name";
    public static final String GOOGLE_ID = "googleId";
    public static final String PHOTO_URL = "photoUrl";
    public static final String FCM_TOKEN = "fcmToken";

    public Accounts()
    {

    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.email);
        dest.writeString(this.name);
        dest.writeString(this.googleId);
        dest.writeString(this.photoUrl);
        dest.writeString(this.fcmToken);
    }

    protected Accounts(Parcel in)
    {
        this.email = in.readString();
        this.name = in.readString();
        this.googleId = in.readString();
        this.photoUrl = in.readString();
        this.fcmToken = in.readString();
    }

    public static final Parcelable.Creator<Accounts> CREATOR = new Parcelable.Creator<Accounts>()
    {
        @Override
        public Accounts createFromParcel(Parcel source)
        {
            return new Accounts(source);
        }

        @Override
        public Accounts[] newArray(int size)
        {
            return new Accounts[size];
        }
    };
}
