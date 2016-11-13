package co.thnki.brandfever.fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import com.android.volley.VolleyError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.NotificationModel;
import co.thnki.brandfever.interfaces.ResultListener;
import co.thnki.brandfever.singletons.Otto;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.utils.NotificationsUtil;
import co.thnki.brandfever.utils.OrdersUtil;

import static co.thnki.brandfever.Brandfever.toast;

public class OrderCancellationDialogFragment extends DialogFragment
{
    public static final String TAG = "EditAddressDialogFragment";

    @Bind(R.id.cancellationReason)
    EditText mCancellationReason;

    private DatabaseReference mOrderDbRef;
    private SharedPreferences mPreference;
    private String mOrderId;
    private ProgressDialog mProgressDialog;
    private String mGoogleId;

    public static OrderCancellationDialogFragment getInstance(String orderId, String googleId)
    {
        OrderCancellationDialogFragment fragment = new OrderCancellationDialogFragment();
        fragment.mOrderId = orderId;
        fragment.mPreference = Brandfever.getPreferences();
        fragment.mOrderDbRef = FirebaseDatabase.getInstance().getReference()
                .child(googleId)
                .child(OrdersUtil.ORDERS).child(orderId);
        fragment.mGoogleId = googleId;
        return fragment;
    }

    public OrderCancellationDialogFragment()
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

        View parentView = inflater.inflate(R.layout.fragment_order_cancellation, container, false);
        ButterKnife.bind(this, parentView);
        Otto.register(this);
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

    @OnClick(R.id.submitCancellation)
    public void submitCancellation()
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
            model.notification = mCancellationReason.getText().toString();
            if (model.notification.trim().isEmpty())
            {
                toast(R.string.pleaseEnterReason);
            }
            else
            {
                mOrderDbRef.removeValue();
                model.action = StoreActivity.ORDER_CANCELLED;
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
                        toast(R.string.orderCancelled);
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