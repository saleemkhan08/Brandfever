package co.thnki.brandfever.firebase.database.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Addresses implements Parcelable
{
    public static final String ADDRESS = "address";
    public static final String NAME = "name";
    public static final String PHONE_NO = "phoneNo";
    public static final String PIN_CODE = "pinCode";
    private String name;
    private String address;
    private String pinCode;
    private String phoneNo;

    public Addresses()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public String getPinCode()
    {
        return pinCode;
    }

    public void setPinCode(String pinCode)
    {
        this.pinCode = pinCode;
    }

    public String getPhoneNo()
    {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo)
    {
        this.phoneNo = phoneNo;
    }


    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(this.name);
        dest.writeString(this.address);
        dest.writeString(this.pinCode);
        dest.writeString(this.phoneNo);
    }

    protected Addresses(Parcel in)
    {
        this.name = in.readString();
        this.address = in.readString();
        this.pinCode = in.readString();
        this.phoneNo = in.readString();
    }

    public static final Parcelable.Creator<Addresses> CREATOR = new Parcelable.Creator<Addresses>()
    {
        @Override
        public Addresses createFromParcel(Parcel source)
        {
            return new Addresses(source);
        }

        @Override
        public Addresses[] newArray(int size)
        {
            return new Addresses[size];
        }
    };
}