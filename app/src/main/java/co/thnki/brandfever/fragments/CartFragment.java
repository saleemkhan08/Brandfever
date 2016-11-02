package co.thnki.brandfever.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.VolleyError;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.R;
import co.thnki.brandfever.StoreActivity;
import co.thnki.brandfever.ViewHolders.CartListProductViewHolder;
import co.thnki.brandfever.adapters.CartAdapter;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.Products;
import co.thnki.brandfever.interfaces.ResultListener;
import co.thnki.brandfever.singletons.VolleyUtil;
import co.thnki.brandfever.utils.CartUtil;
import co.thnki.brandfever.utils.ConnectivityUtil;

import static co.thnki.brandfever.Brandfever.toast;
import static co.thnki.brandfever.singletons.VolleyUtil.RECEIVER_TOKENS;

public class CartFragment extends Fragment
{
    public static final String TAG = "CartFragment";
    @Bind(R.id.cartRecyclerView)
    RecyclerView mCartRecyclerView;

    @Bind(R.id.recyclerProgress)
    View mProgress;

    @Bind(R.id.noProductFoundContainer)
    View mNoProductFoundContainer;

    @Bind(R.id.placeAnOrder)
    View mPlaceAnOrder;

    private DatabaseReference mCartDbRef;
    private FirebaseRecyclerAdapter<Products, CartListProductViewHolder> mAdapter;

    public CartFragment()
    {
        // Required empty public constructor
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        SharedPreferences mPreference = Brandfever.getPreferences();
        mCartDbRef = rootRef.child(mPreference.getString(Accounts.GOOGLE_ID, "")).child(CartUtil.CART_LIST);
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
        mCartDbRef.addValueEventListener(new ValueEventListener()
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
            ((StoreActivity) activity).setToolBarTitle(getString(R.string.shoppingCart));
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
            mPlaceAnOrder.setVisibility(View.GONE);
        }
        else
        {
            mNoProductFoundContainer.setVisibility(View.GONE);
            mPlaceAnOrder.setVisibility(View.VISIBLE);
        }
        Log.d("updateUi", "updateUi" + size + ", visible : " + mNoProductFoundContainer.isShown());
    }

    @OnClick(R.id.placeAnOrder)
    public void placeOrder()
    {
        if (ConnectivityUtil.isConnected())
        {
            SharedPreferences preferences = Brandfever.getPreferences();
            Map<String, String> data = new HashMap<>();
            data.put(Accounts.GOOGLE_ID, preferences.getString(Accounts.GOOGLE_ID,""));
            data.put(VolleyUtil.ACTION, VolleyUtil.PLACE_AN_ORDER);
            data.put(VolleyUtil.APP_ID, preferences.getString(VolleyUtil.APP_ID, getString(R.string.serverKey)));

            @SuppressLint("UseSparseArrays")
            Map<Integer, String> ownersTokenList = new HashMap<>();
            int index = 0;
            for(String token : preferences.getStringSet(Accounts.GOOGLE_ID, new HashSet<String>()))
            {
                ownersTokenList.put(index++, token);
            }
            data.put(RECEIVER_TOKENS, new JSONObject(ownersTokenList).toString());
            VolleyUtil.getInstance().sendPostData(data, new ResultListener<String>()
            {
                @Override
                public void onSuccess(String result)
                {
                    //Todo add the product to MyOrders and remove it from Cart.
                    toast("Result : "+result);
                }

                @Override
                public void onError(VolleyError error)
                {
                    toast(R.string.please_try_again);
                }
            });
        }
        else
        {
            toast(R.string.noInternet);
        }
    }
}
