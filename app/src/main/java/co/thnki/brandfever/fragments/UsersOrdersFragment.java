package co.thnki.brandfever.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
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
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.adapters.OrdersAdapter;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.Addresses;
import co.thnki.brandfever.firebase.database.models.Order;
import co.thnki.brandfever.singletons.Otto;
import co.thnki.brandfever.utils.OrdersUtil;
import co.thnki.brandfever.view.holders.OrderListProductViewHolder;

import static co.thnki.brandfever.StoreActivity.RESTART_ACTIVITY;
import static co.thnki.brandfever.utils.UserUtil.APP_DATA;
import static co.thnki.brandfever.utils.UserUtil.OWNER_PHONE_NUMBER;

public class UsersOrdersFragment extends Fragment
{
    public static final String TAG = "UsersOrdersFragment";
    @Bind(R.id.orderRecyclerView)
    RecyclerView mOrdersRecyclerView;

    @Bind(R.id.recyclerProgress)
    View mProgress;

    @Bind(R.id.noProductFoundContainer)
    View mNoProductFoundContainer;

    @Bind(R.id.contactPerson)
    TextView mContactPerson;

    @Bind(R.id.deliveryAddress)
    TextView mDeliveryAddress;

    @Bind(R.id.contactNumber)
    TextView mContactNumber;

    @Bind(R.id.addressContainer)
    View mAddressContainer;

    private FirebaseRecyclerAdapter<Order, OrderListProductViewHolder> mAdapter;
    private DatabaseReference mAddressDbRef;
    private String mPhoneNumber;

    public UsersOrdersFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parent = inflater.inflate(R.layout.fragment_orders, container, false);
        ButterKnife.bind(this, parent);
        String mGoogleId = getArguments().getString(Accounts.GOOGLE_ID);
        if (mGoogleId != null)
        {
            DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference()
                    .child(mGoogleId);
            mAddressDbRef = mUserReference.child(Accounts.ADDRESS_LIST);
            DatabaseReference mOrdersDbRef = mUserReference.child(OrdersUtil.ORDERS);

            updateAddress();
            mAdapter = OrdersAdapter.getInstance(mOrdersDbRef, mGoogleId, getActivity());
            mOrdersRecyclerView.setAdapter(mAdapter);
            mOrdersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mOrdersDbRef.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    try
                    {
                        updateUi();
                    }
                    catch (Exception e)
                    {
                        Log.d("Exception", e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
        else
        {
            Otto.post(RESTART_ACTIVITY);
        }
        return parent;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof StoreActivity)
        {
            ((StoreActivity) activity).setToolBarTitle(getString(R.string.orders));
        }
    }

    private void updateUi()
    {
        mProgress.setVisibility(View.GONE);
        int size = mAdapter.getItemCount();
        Log.d("updateUi", "updateUi" + size + ", visible : " + mNoProductFoundContainer.isShown());
        if (size < 1)
        {
            mNoProductFoundContainer.setVisibility(View.VISIBLE);
            mAddressContainer.setVisibility(View.GONE);
        }
        else
        {
            mNoProductFoundContainer.setVisibility(View.GONE);
            mAddressContainer.setVisibility(View.VISIBLE);
        }
        Log.d("updateUi", "updateUi" + size + ", visible : " + mNoProductFoundContainer.isShown());
    }

    private void updateAddress()
    {
        if (mAddressDbRef != null)
        {
            mAddressDbRef.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    try
                    {
                        updateAddressUi(dataSnapshot.getValue(Addresses.class));
                    }
                    catch (Exception e)
                    {
                        Log.d("Exception", e.getMessage());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
    }

    private void updateAddressUi(Addresses address)
    {
        if (address != null)
        {
            String addressText = address.getAddress() + " - " + address.getPinCode();
            mDeliveryAddress.setText(addressText);
            mContactPerson.setText(address.getName());
            mContactNumber.setText(address.getPhoneNo());
            if (!Brandfever.getPreferences().getBoolean(Accounts.IS_OWNER, false))
            {
                DatabaseReference appDataDbRef = FirebaseDatabase.getInstance().getReference()
                        .child(APP_DATA).child(OWNER_PHONE_NUMBER);
                appDataDbRef.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        try
                        {
                            mPhoneNumber = dataSnapshot.getValue(String.class);
                        }
                        catch (Exception e)
                        {
                            Log.d("Exception", e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
            }
            else
            {
                mPhoneNumber = address.getPhoneNo();
            }

            Handler handler = new Handler();
            handler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    int bottomPadding = mAddressContainer.getHeight();

                    int topPadding = mOrdersRecyclerView.getPaddingTop();
                    int rightPadding = mOrdersRecyclerView.getPaddingRight();
                    int leftPadding = mOrdersRecyclerView.getPaddingLeft();

                    mOrdersRecyclerView.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
                }
            });
        }
    }

    @OnClick(R.id.makeCall)
    public void makeACall()
    {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + mPhoneNumber));
        startActivity(intent);
    }
}
