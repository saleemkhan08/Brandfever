package co.thnki.brandfever.utils;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.firebase.database.models.Accounts;
import co.thnki.brandfever.firebase.database.models.Order;
import co.thnki.brandfever.firebase.database.models.Products;

/**
 * 1. Login Activity
 * a. getInstance() $ updateCartList() - > onActivityResult()
 * b. clearInstance() -> setUpLogin()
 * <p>
 * 2. Product Activity
 * a. getInstance() -> onCreate()
 * b. isAddedToCart() -> onCreate() used to update UI
 * c. placeAnOrder() -> placeAnOrder()
 * d. removeFromCart() -> placeAnOrder()
 * <p>
 * 2. Product Activity
 * a. placeAnOrder() ->
 * b. removeFromCart()
 * c. isAddedToCart()
 * <p>
 * 3. Products Fragment
 * a. placeAnOrder()
 * b. removeFromCart()
 * c. isAddedToCart()
 */
public class OrdersUtil
{
    public static final String ORDERS = "Orders";
    public static final String ORDER_ADDED_TO_CART = "OrderAddedToCart";
    public static final String ORDER_PLACED = "OrderPlaced";
    public static final String ORDER_PACKED = "OrderPacked";
    public static final String ORDER_SHIPPED = "OrderShipped";
    public static final String ORDER_DELIVERED = "OrderDelivered";
    public static final String ORDER_CANCELLED = "OrderCancelled";
    public static final String ORDER_RETURNED = "OrderReturned";
    public static final String ORDER_REQUESTED_RETURN = "OrderRequestedReturn";
    public static final String ORDER_DELAYED = "orderDelayed";


    private DatabaseReference mMyOrdersRef;
    private static OrdersUtil sInstance;
    private SharedPreferences mPreferences;

    private OrdersUtil()
    {
        mPreferences = Brandfever.getPreferences();
        String googleId = mPreferences.getString(Accounts.GOOGLE_ID, "dummyId");
        Log.d(OrdersUtil.ORDERS, "googleId : " + googleId);
        mMyOrdersRef = FirebaseDatabase.getInstance().getReference()
                .child(googleId).child(ORDERS);
    }

    public static OrdersUtil getsInstance()
    {
        if (sInstance == null)
        {
            sInstance = new OrdersUtil();
        }
        return sInstance;
    }

    public static void clearInstance()
    {
        sInstance = null;
    }

    public String placeAnOrder(Order order)
    {
        String key = getKey(order);
        DatabaseReference reference = mMyOrdersRef.child(key);

        mPreferences.edit().putBoolean(key, true).apply();
        reference.setValue(order);
        return key;
    }

    public void cancelOrder(Products products)
    {
        String key = getKey(products);
        mPreferences.edit().remove(key).apply();
        mMyOrdersRef.child(key).removeValue();
    }

    public String placeAnOrder(Products products)
    {
        String key = getKey(products);
        DatabaseReference reference = mMyOrdersRef.child(key);

        mPreferences.edit().putBoolean(key, true).apply();
        reference.setValue(products);
        return key;
    }

    public void cancelOrder(Order order)
    {
        String key = getKey(order);

        mPreferences.edit().remove(key).apply();
        mMyOrdersRef.child(key).removeValue();
    }

    public void updateOrderList()
    {
        mMyOrdersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                try
                {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        Products product = snapshot.getValue(Products.class);
                        String key = getKey(product);
                        Log.d(OrdersUtil.ORDERS, "key : " + key);
                        mPreferences.edit()
                                .putBoolean(key, true)
                                .apply();
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

    public boolean isOrdered(Order order)
    {
        String key = getKey(order);
        boolean isFav = mPreferences.getBoolean(key, false);
        Log.d(OrdersUtil.ORDERS, "isAddedToCart : " + isFav);
        return isFav;
    }

    public boolean isOrdered(Products product)
    {
        String key = getKey(product);
        boolean isFav = mPreferences.getBoolean(key, false);
        Log.d(OrdersUtil.ORDERS, "isAddedToCart : " + isFav);
        return isFav;
    }

    public String getKey(Order order)
    {
        return order.getCategoryId() + "_" + order.getProductId() + "_" + order.getSelectedSize();
    }

    private String getKey(Products product)
    {
        return product.getCategoryId() + "*" + product.getProductId();
    }

    public boolean toggleOrder(Products model)
    {
        boolean isOrdered = isOrdered(model);
        if (isOrdered)
        {
            cancelOrder(model);
        }
        else
        {
            placeAnOrder(model);
        }

        return !isOrdered;
    }
}
