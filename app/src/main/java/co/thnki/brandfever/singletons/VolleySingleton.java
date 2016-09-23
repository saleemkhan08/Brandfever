package co.thnki.brandfever.singletons;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import co.thnki.brandfever.Brandfever;


public class VolleySingleton
{
    private static VolleySingleton sInstance = null;
    private RequestQueue mRequestQueue;
    private VolleySingleton()
    {
        mRequestQueue = Volley.newRequestQueue(Brandfever.getAppContext());
    }
    public static VolleySingleton getInstance()
    {
        if(sInstance == null)
        {
            sInstance = new VolleySingleton();
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue()
    {
        return mRequestQueue;
    }
}
