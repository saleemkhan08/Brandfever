package co.thnki.brandfever.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
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
import co.thnki.brandfever.firebase.database.models.Order;
import co.thnki.brandfever.interfaces.ResultListener;
import co.thnki.brandfever.utils.CartUtil;
import co.thnki.brandfever.utils.ConnectivityUtil;
import co.thnki.brandfever.utils.NotificationsUtil;
import co.thnki.brandfever.utils.OrdersUtil;
import co.thnki.brandfever.view.holders.CartListProductViewHolder;

import static co.thnki.brandfever.Brandfever.toast;
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

    @Bind(R.id.orderContainer)
    View mOrderContainer;


    private DatabaseReference mCartDbRef;
    private FirebaseRecyclerAdapter<Order, CartListProductViewHolder> mAdapter;
    private DataSnapshot mCartData;
    private Addresses mAddress;
    private boolean mIsOrderPlaced;
    private SharedPreferences mPreferences;

    public CartFragment()
    {
        // Required empty public constructor
        mPreferences = Brandfever.getPreferences();
        mGoogleId = mPreferences.getString(Accounts.GOOGLE_ID, "");
        DatabaseReference rootDbRef = FirebaseDatabase.getInstance().getReference();
        mUserReference = rootDbRef.child(mGoogleId);
        mAddressDbRef = mUserReference.child(ADDRESS_LIST);
        mCartDbRef = mUserReference.child(CartUtil.CART_LIST);
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
                try
                {
                    if (getActivity() != null)
                    {
                        updateCartUi();
                        mCartData = dataSnapshot;
                        int totalPriceBefore = 0;
                        int totalPriceAfter = 0;
                        Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                        for (DataSnapshot snapshot : snapshots)
                        {
                            Order product = snapshot.getValue(Order.class);
                            totalPriceAfter += product.getActualPriceAfter() * product.getNoOfProducts();
                            totalPriceBefore += product.getActualPriceBefore() * product.getNoOfProducts();
                        }
                        String total = getString(R.string.total) + " " + '\u20B9' + totalPriceAfter;
                        String saved = getString(R.string.youSave) + " " + '\u20B9' + (totalPriceBefore - totalPriceAfter);

                        mTotalTextView.setText(total);
                        mSavingsTextView.setText(saved);
                    }
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
                    try
                    {
                        updateAddressUi(dataSnapshot.getValue(Addresses.class));
                        if (mIsOrderPlaced)
                        {
                            submitOrder();
                        }
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
        mAddress = address;
        if (mAddress != null)
        {
            mAddressContainer.setVisibility(View.VISIBLE);
            String addressText = address.getAddress() + " - " + address.getPinCode();
            mDeliveryAddress.setText(addressText);
            mContactPerson.setText(address.getName());
            mContactNumber.setText(address.getPhoneNo());
        }
        Handler handler = new Handler();
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                int bottomPadding = mOrderContainer.getHeight();
                int topPadding = mTotalContainer.getHeight();
                int rightPadding = mCartRecyclerView.getPaddingRight();
                int leftPadding = mCartRecyclerView.getPaddingLeft();
                mCartRecyclerView.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
            }
        });
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
            mOrderContainer.setVisibility(View.GONE);
            mTotalContainer.setVisibility(View.GONE);
        }
        else
        {
            mNoProductFoundContainer.setVisibility(View.GONE);
            mOrderContainer.setVisibility(View.VISIBLE);
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
        if (ConnectivityUtil.isConnected())
        {
            if (mAddress != null)
            {
                Log.d(TAG, "submitOrder");
                saveOrderInDb();
                NotificationsUtil.getInstance().sendNotificationToAll(getNotification(), mGoogleId, new ResultListener<String>()
                {
                    @Override
                    public void onSuccess(String result)
                    {
                        Log.d(TAG, "Response : " + result);
                    }

                    @Override
                    public void onError(VolleyError error)
                    {
                        Log.d(TAG, "Response error : " + error.getMessage());
                    }
                });
                toast(R.string.orderPlaced);
                ((StoreActivity) getActivity()).showUserOrdersFragment(mPreferences.getString(Accounts.GOOGLE_ID, ""));
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
        //PUK : 11203510 0000 0000
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.googleId = mGoogleId;
        notificationModel.action = OrdersUtil.ORDER_PLACED;
        notificationModel.photoUrl = mPreferences.getString(Accounts.PHOTO_URL, "");
        notificationModel.notification = getString(R.string.placedAnOrder);
        notificationModel.username = mPreferences.getString(Accounts.NAME, getString(R.string.anUser));
        notificationModel.timeStamp = -System.currentTimeMillis();
        return notificationModel;
    }

    private void saveOrderInDb()
    {
        Log.d(TAG, "saveOrderInDb");
        DatabaseReference myOrdersDbRef = mUserReference.child(OrdersUtil.ORDERS);
        for (DataSnapshot snapshot : mCartData.getChildren())
        {
            Order order = snapshot.getValue(Order.class);

            order.setOrderStatus(OrdersUtil.ORDER_PLACED);
            order.setTimeStamp(-System.currentTimeMillis());
            myOrdersDbRef.child(snapshot.getKey()).setValue(order);
        }
        mCartDbRef.removeValue();
    }

    @OnClick(R.id.editAddressIcon)
    public void showAddressEditorFragment()
    {
        ((StoreActivity) getActivity()).showAddressEditorFragment();
    }

    private float getPaddingInPixel(int paddingInDp)
    {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        return paddingInDp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}