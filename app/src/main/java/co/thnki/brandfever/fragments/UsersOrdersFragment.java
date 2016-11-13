package co.thnki.brandfever.fragments;

import android.app.Activity;
import android.os.Bundle;
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
import co.thnki.brandfever.R;
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.adapters.OrdersAdapter;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.Addresses;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.utils.OrdersUtil;
import co.thnki.brandfever.view.holders.OrderListProductViewHolder;

public class UsersOrdersFragment extends Fragment
{
    public static final String TAG = "UsersOrdersFragment";
    private String mGoogleId;
    private DatabaseReference mUserReference;
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

    private DatabaseReference mOrdersDbRef;
    private FirebaseRecyclerAdapter<Products, OrderListProductViewHolder> mAdapter;
    private DatabaseReference mAddressDbRef;


    public static UsersOrdersFragment getInstance(String googleId)
    {
        UsersOrdersFragment fragment = new UsersOrdersFragment();
        fragment.mGoogleId = googleId;
        fragment.mUserReference = FirebaseDatabase.getInstance().getReference()
                .child(googleId);
        fragment.mAddressDbRef = fragment.mUserReference.child(Accounts.ADDRESS_LIST);
        fragment.mOrdersDbRef = fragment.mUserReference.child(OrdersUtil.ORDERS);
        return fragment;
    }
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
        updateAddress();
        mAdapter = OrdersAdapter.getInstance(mOrdersDbRef,mGoogleId, getActivity());
        mOrdersRecyclerView.setAdapter(mAdapter);
        mOrdersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mOrdersDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                updateUi();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

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
                    updateAddressUi(dataSnapshot.getValue(Addresses.class));
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
        }
    }
}
