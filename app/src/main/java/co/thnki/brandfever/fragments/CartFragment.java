package co.thnki.brandfever.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashSet;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.adapters.CartAdapter;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.Addresses;
import co.thnki.brandfever.firebase.database.models.NotificationModel;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.interfaces.ResultListener;
import co.thnki.brandfever.utils.CartUtil;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.utils.NotificationsUtil;
import co.thnki.brandfever.utils.OrdersUtil;
import co.thnki.brandfever.view.holders.CartListProductViewHolder;

import static co.thnki.brandfever.Brandfever.toast;
import static co.thnki.brandfever.R.string.orderPlaced;
import static co.thnki.brandfever.firebase.database.models.Accounts.ADDRESS_LIST;

//Todo

/**
 * 1. show address above place order and show an edit icon
 * 2. on clicking edit icon show address dialog
 * 3. if address is not saved show address dialog on clicking place order.
 * 4. when save is clicked perform place order
 * <p>
 * Next Release integrate payment option
 */
public class CartFragment extends Fragment
{
    public static final String TAG = "CartFragment";
    private DatabaseReference mRootDbRef;
    private DatabaseReference mUserReference;
    private DatabaseReference mAddressDbRef;
    private String mGoogleId;

    @Bind(R.id.cartRecyclerView)
    RecyclerView mCartRecyclerView;

    @Bind(R.id.recyclerProgress)
    View mProgress;

    @Bind(R.id.noProductFoundContainer)
    View mNoProductFoundContainer;

    @Bind(R.id.placeAnOrder)
    View mPlaceAnOrder;

    @Bind(R.id.totalContainer)
    View mTotalContainer;

    @Bind(R.id.contactPerson)
    TextView mContactPerson;

    @Bind(R.id.savingsTextView)
    TextView mSavingsTextView;

    @Bind(R.id.totalTextView)
    TextView mTotalTextView;


    @Bind(R.id.deliveryAddress)
    TextView mDeliveryAddress;

    @Bind(R.id.contactNumber)
    TextView mContactNumber;

    @Bind(R.id.addressContainer)
    View mAddressContainer;


    private DatabaseReference mCartDbRef;
    private FirebaseRecyclerAdapter<Products, CartListProductViewHolder> mAdapter;
    private DataSnapshot mCartData;
    private Addresses mAddress;
    private boolean mIsOrderPlaced;
    private ProgressDialog mDialog;
    private SharedPreferences mPreferences;

    public CartFragment()
    {
        // Required empty public constructor
        mPreferences = Brandfever.getPreferences();
        mGoogleId = mPreferences.getString(Accounts.GOOGLE_ID, "");
        mRootDbRef = FirebaseDatabase.getInstance().getReference();
        mUserReference = mRootDbRef.child(mGoogleId);
        mAddressDbRef = mUserReference.child(ADDRESS_LIST);
        mCartDbRef = mUserReference.child(CartUtil.CART_LIST);
        updateOwnerDetails();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View parent = inflater.inflate(R.layout.fragment_cart, container, false);
        ButterKnife.bind(this, parent);
        mAdapter = CartAdapter.getInstance(mCartDbRef, getActivity());
        mCartRecyclerView.setAdapter(mAdapter);
        mCartRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateAddress();
        mCartDbRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                updateCartUi();
                mCartData = dataSnapshot;
                int totalPriceBefore = 0;
                int totalPriceAfter = 0;
                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                for(DataSnapshot snapshot : snapshots)
                {
                    Products product = snapshot.getValue(Products.class);
                    totalPriceAfter += product.getActualPriceAfter();
                    totalPriceBefore += product.getActualPriceBefore();
                }
                String total = getString(R.string.total) + " "+'\u20B9' + totalPriceAfter;
                String saved = getString(R.string.youSave)+ " "+'\u20B9' + (totalPriceBefore - totalPriceAfter);

                mTotalTextView.setText(total);
                mSavingsTextView.setText(saved);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
        return parent;
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
                    if (mIsOrderPlaced)
                    {
                        submitOrder();
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
        mAddress = address;
        if (mAddress != null)
        {
            mAddressContainer.setVisibility(View.VISIBLE);
            String addressText = address.getAddress() + " - " + address.getPinCode();
            mDeliveryAddress.setText(addressText);
            mContactPerson.setText(address.getName());
            mContactNumber.setText(address.getPhoneNo());
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof StoreActivity)
        {
            ((StoreActivity) activity).setToolBarTitle(getString(R.string.shoppingCart));
        }
    }

    private void updateOwnerDetails()
    {
        mRootDbRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference databaseReference = mRootDbRef.child(Accounts.OWNERS);
        Log.d(TAG, "databaseReference : "+databaseReference);
        databaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                boolean isOwner = false;
                for (DataSnapshot snapshot : children)
                {
                    String googleId = "";
                    try
                    {
                        googleId = snapshot.getValue(String.class);
                    }
                    catch (Exception e)
                    {
                        return;
                    }
                    if (mPreferences.getString(Accounts.GOOGLE_ID, "").equals(googleId))
                    {
                        isOwner = true;
                    }
                    final DatabaseReference usersDbRef = mRootDbRef.child(Accounts.USERS).child(googleId);
                    usersDbRef.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            Accounts ownerAccount = dataSnapshot.getValue(Accounts.class);
                            Set<String> ownerTokens = mPreferences.getStringSet(Accounts.OWNERS_TOKENS, new HashSet<String>());
                            Set<String> ownerGids = mPreferences.getStringSet(Accounts.OWNERS_GOOGLE_IDS, new HashSet<String>());

                            ownerTokens.add(ownerAccount.fcmToken);
                            ownerGids.add(ownerAccount.googleId);

                            mPreferences.edit()
                                    .putStringSet(Accounts.OWNERS_TOKENS, ownerTokens)
                                    .putStringSet(Accounts.OWNERS_GOOGLE_IDS, ownerGids)
                                    .apply();
                            Log.d(TAG, "ownerTokens : " + ownerTokens+", ownerGids : "+ownerGids);
                            usersDbRef.removeEventListener(this);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });
                }
                mPreferences.edit().putBoolean(Accounts.IS_OWNER, isOwner).apply();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mIsOrderPlaced = false;
    }

    private void updateCartUi()
    {
        mProgress.setVisibility(View.GONE);
        int size = mAdapter.getItemCount();
        Log.d("updateUi", "updateUi" + size + ", visible : " + mNoProductFoundContainer.isShown());
        if (size < 1)
        {
            mNoProductFoundContainer.setVisibility(View.VISIBLE);
            mPlaceAnOrder.setVisibility(View.GONE);
            mAddressContainer.setVisibility(View.GONE);
            mTotalContainer.setVisibility(View.GONE);
        }
        else
        {
            mNoProductFoundContainer.setVisibility(View.GONE);
            mPlaceAnOrder.setVisibility(View.VISIBLE);
            mTotalContainer.setVisibility(View.VISIBLE);
        }
        Log.d("updateUi", "updateUi" + size + ", visible : " + mNoProductFoundContainer.isShown());
    }

    @OnClick(R.id.placeAnOrder)
    public void placeOrder()
    {
        mIsOrderPlaced = true;
        submitOrder();
    }

    private void submitOrder()
    {
        /**
         *  1. check if address is available otherwise show address dialog.
         *  2. //TODO check if that product is available then allow the placement of order.
         *  3. save the order in DB
         *  4. save a notification in all the owners profile
         *  5. send notification through FCM to all the owners
         *  6. //TODO reduce the item count in server
         */
        if (ConnectivityUtil.isConnected())
        {
            if (mAddress != null)
            {
                Log.d(TAG, "submitOrder");
                mDialog = new ProgressDialog(getActivity());
                mDialog.setMessage(getString(R.string.placingAnOrder));
                mDialog.show();

                saveOrderInDb();
                NotificationsUtil.getInstance().sendNotificationToAllOwners(getNotification(), mGoogleId, new ResultListener<String>()
                {
                    @Override
                    public void onSuccess(String result)
                    {
                        toast(orderPlaced);
                        Log.d(TAG, "Response : " + result);
                        if (mDialog != null)
                        {
                            mDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(VolleyError error)
                    {
                        toast(R.string.please_try_again);
                        if (mDialog != null)
                        {
                            mDialog.dismiss();
                        }
                    }
                });
            }
            else
            {
                showAddressEditorFragment();
            }
        }
        else
        {
            toast(R.string.noInternet);
        }
    }

    private NotificationModel getNotification()
    {
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.googleId = mGoogleId;
        notificationModel.action = StoreActivity.ORDER_PLACED;
        notificationModel.photoUrl = mPreferences.getString(Accounts.PHOTO_URL, "");
        notificationModel.notification = getString(R.string.placedAnOrder);
        notificationModel.username = mPreferences.getString(Accounts.NAME, getString(R.string.anUser));
        return notificationModel;
    }

    private void saveOrderInDb()
    {
        Log.d(TAG, "saveOrderInDb");
        DatabaseReference myOrdersDbRef = mUserReference.child(OrdersUtil.ORDERS);
        for (DataSnapshot snapshot : mCartData.getChildren())
        {
            Products products = snapshot.getValue(Products.class);
            products.setOrderStatus(OrdersUtil.ORDER_PLACED);
            myOrdersDbRef.child(snapshot.getKey()).setValue(products);
        }
        mCartDbRef.removeValue();
    }

    @OnClick(R.id.editAddressIcon)
    public void showAddressEditorFragment()
    {
        ((StoreActivity) getActivity()).showAddressEditorFragment();
    }
}