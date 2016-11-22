package co.thnki.brandfever.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.NotificationModel;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.interfaces.ResultListener;
import co.thnki.brandfever.singletons.Otto;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.utils.NotificationsUtil;
import co.thnki.brandfever.utils.OrdersUtil;

import static co.thnki.brandfever.Brandfever.toast;

public class NotificationDialogFragment extends DialogFragment
{
    public static final String TAG = "EditAddressDialogFragment";

    @Bind(R.id.notificationMsgEditText)
    EditText mNotificationMsgEditText;

    @Bind(R.id.textInputHint)
    TextInputLayout mTextInputHint;


    @Bind(R.id.dialogTitle)
    TextView mDialogTitleTextView;

    private DatabaseReference mOrderDbRef;
    private SharedPreferences mPreference;
    private ProgressDialog mProgressDialog;
    private String mGoogleId;
    private int mNotificationMessageResId;
    private String mOrderStatus;

    public static NotificationDialogFragment getInstance(String orderId, String googleId, int msgResourceId, String status)
    {
        NotificationDialogFragment fragment = new NotificationDialogFragment();
        fragment.mPreference = Brandfever.getPreferences();
        fragment.mOrderDbRef = FirebaseDatabase.getInstance().getReference()
                .child(googleId)
                .child(OrdersUtil.ORDERS).child(orderId);
        fragment.mGoogleId = googleId;
        fragment.mNotificationMessageResId = msgResourceId;
        fragment.mOrderStatus = status;
        return fragment;
    }

    public NotificationDialogFragment()
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Window window = getDialog().getWindow();
        if (window != null)
        {
            window.requestFeature(Window.FEATURE_NO_TITLE);
        }
        View parentView = inflater.inflate(R.layout.fragment_notification_dialog, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
        updateNotificationEditText();

        return parentView;
    }

    private void updateNotificationEditText()
    {

        if(mNotificationMessageResId == 0)
        {
            mDialogTitleTextView.setText(R.string.orderCancellation);
            mTextInputHint.setHint(getString(R.string.cancellationReason));
            mNotificationMsgEditText.setText("");
        }
        else if(mNotificationMessageResId == -1)
        {
            mDialogTitleTextView.setText(R.string.returnOrder);
            mTextInputHint.setHint(getString(R.string.returnReason));
            mNotificationMsgEditText.setText("");
        }
        else
        {
            mDialogTitleTextView.setText(R.string.notification);
            mTextInputHint.setHint(getString(R.string.message));
            mNotificationMsgEditText.setText(mNotificationMessageResId);
        }
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

    @OnClick(R.id.sendNotification)
    public void sendNotification()
    {
        /**
         * 1. get the reason for cancellation
         * 2. if owner is cancelling then
         *      a. notify the user who's order is cancelled
         *      b. notify all other owners
         * 3. if user is cancelling then notify all the owners
         *
         */
        Log.d("OrderCancellation", ""+mOrderDbRef);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getString(R.string.cancellingTheOrder));
        mProgressDialog.show();
        getDialog().hide();
        if (ConnectivityUtil.isConnected())
        {
            NotificationModel model = new NotificationModel();
            model.notification = mNotificationMsgEditText.getText().toString();
            if (model.notification.trim().isEmpty())
            {
                toast(R.string.pleaseEnterReason);
            }
            else
            {
                mOrderDbRef.child(Products.ORDER_STATUS).setValue(mOrderStatus);
                model.action = mOrderStatus;
                model.googleId = mPreference.getString(Accounts.GOOGLE_ID, "");
                model.username = mPreference.getString(Accounts.NAME, "");
                NotificationsUtil notificationsUtil = NotificationsUtil.getInstance();
                if (mPreference.getBoolean(Accounts.IS_OWNER, false))
                {
                    model.photoUrl = "";
                }
                else
                {
                    model.photoUrl = mPreference.getString(Accounts.PHOTO_URL, "");
                    mGoogleId = null;
                }
                notificationsUtil.sendNotificationToAllOwners(model, mGoogleId, new ResultListener<String>()
                {
                    @Override
                    public void onSuccess(String result)
                    {
                        toast(R.string.sent);
                        mProgressDialog.dismiss();
                        dismiss();
                    }

                    @Override
                    public void onError(VolleyError error)
                    {
                        mProgressDialog.dismiss();
                        dismiss();
                    }
                });
            }
        }
        else
        {
            toast(R.string.noInternet);
        }
    }
}