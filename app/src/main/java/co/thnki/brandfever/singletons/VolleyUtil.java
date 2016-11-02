package co.thnki.brandfever.singletons;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Map;

import co.thnki.brandfever.Brandfever;
import co.thnki.brandfever.interfaces.ResultListener;


public class VolleyUtil
{
    public static final String DEFAULT_URL = "http://www.thnki.com/fcm.php";
    public static final String PLACE_AN_ORDER = "placeAnOrder";
    private static VolleyUtil sInstance = null;
    private RequestQueue mRequestQueue;
    private static final int MY_SOCKET_TIMEOUT_MS = 10000;
    public static final String REQUEST_HANDLER_URL = "requestHandlerUrl";
    public static final String ACTION = "action";
    public static final String APP_ID = "appId";
    public static final String RECEIVER_TOKENS = "receiverToken";

    private VolleyUtil()
    {
        mRequestQueue = Volley.newRequestQueue(Brandfever.getAppContext());
    }
    public static VolleyUtil getInstance()
    {
        if(sInstance == null)
        {
            sInstance = new VolleyUtil();
        }
        return sInstance;
    }

    private RequestQueue getRequestQueue()
    {
        return mRequestQueue;
    }

    public void sendPostData(final Map<String, String> postData, final ResultListener<String> listener)
    {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                Brandfever.getPreferences().getString(REQUEST_HANDLER_URL, DEFAULT_URL),
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response)
                    {
                        if(listener != null)
                        {
                            listener.onSuccess(response);
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        if(listener != null)
                        {
                            listener.onError(error);
                        }
                    }
                })
        {
            @Override
            protected Map<String, String> getParams()
            {
                return postData;
            }
        };
        RequestQueue requestQueue = VolleyUtil.getInstance().getRequestQueue();
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        requestQueue.add(stringRequest);
    }
}
