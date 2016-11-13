package co.thnki.brandfever.fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.Addresses;
import co.thnki.brandfever.interfaces.Const;
import co.thnki.brandfever.singletons.Otto;
import co.thnki.brandfever.utils.ConnectivityUtil;

import static co.thnki.brandfever.Brandfever.toast;
import static co.thnki.brandfever.firebase.database.models.Accounts.ADDRESS_LIST;

public class EditAddressDialogFragment extends DialogFragment implements Const
{
    public static final String TAG = "EditAddressDialogFragment";

    @Bind(R.id.addressName)
    EditText mAddressName;

    @Bind(R.id.addressText)
    EditText mAddressText;

    @Bind(R.id.phoneNumber)
    EditText mPhoneNumber;

    @Bind(R.id.pinCode)
    EditText mPinCode;

    private DatabaseReference mAddressDbRef;
    private Addresses mAddress;
    private SharedPreferences mPreference;

    public EditAddressDialogFragment()
    {
        mPreference = Brandfever.getPreferences();
        mAddressDbRef = FirebaseDatabase.getInstance().getReference()
                .child(mPreference.getString(Accounts.GOOGLE_ID, ""))
                .child(ADDRESS_LIST);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Window window = getDialog().getWindow();
        if(window != null)
        {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }

        View parentView = inflater.inflate(R.layout.fragment_edit_address, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);

        mAddressName.setText(mPreference.getString(Addresses.NAME, ""));
        mAddressText.setText(mPreference.getString(Addresses.ADDRESS, ""));
        mPhoneNumber.setText(mPreference.getString(Addresses.PHONE_NO, ""));
        mPinCode.setText(mPreference.getString(Addresses.PIN_CODE, ""));

        mAddressDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                mAddress = dataSnapshot.getValue(Addresses.class);
                if (mAddress != null)
                {
                    mAddressName.setText(mAddress.getName());
                    mAddressText.setText(mAddress.getAddress());
                    mPhoneNumber.setText(mAddress.getPhoneNo());
                    mPinCode.setText(mAddress.getPinCode());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        return parentView;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        Otto.unregister(this);
    }

    @OnClick(R.id.closeDialog)
    public void close()
    {
        dismiss();
    }

    @OnClick(R.id.saveProduct)
    public void save()
    {
        if (ConnectivityUtil.isConnected())
        {
            if(mAddress == null)
            {
                mAddress = new Addresses();
            }
            mAddress.setName(mAddressName.getText().toString());
            mAddress.setAddress(mAddressText.getText().toString());
            mAddress.setPhoneNo(mPhoneNumber.getText().toString());
            mAddress.setPinCode(mPinCode.getText().toString());

            if (mAddress.getName() == null || mAddress.getName().isEmpty())
            {
                toast(R.string.pleaseEnterValidName);
                mAddressName.requestFocus();
            }
            else if (mAddress.getAddress() == null || mAddress.getAddress().isEmpty())
            {
                toast(R.string.pleaseEnterValidAddress);
                mAddressText.requestFocus();
            }
            else if (mAddress.getPhoneNo() == null || mAddress.getPhoneNo().isEmpty())
            {
                toast(R.string.pleaseEnterValidPhoneNo);
                mPhoneNumber.requestFocus();
            }
            else if (mAddress.getPinCode() == null || mAddress.getPinCode().isEmpty())
            {
                toast(R.string.pleaseEnterValidPincode);
                mPinCode.requestFocus();
            }
            else
            {
                mAddressDbRef.setValue(mAddress);
                mPreference.edit().putString(Addresses.NAME, mAddress.getName())
                        .putString(Addresses.ADDRESS, mAddress.getAddress())
                        .putString(Addresses.PHONE_NO, mAddress.getPhoneNo())
                        .putString(Addresses.PIN_CODE, mAddress.getPinCode())
                        .apply();

                dismiss();
            }
        }
        else
        {
            toast(R.string.noInternet);
        }
    }
}