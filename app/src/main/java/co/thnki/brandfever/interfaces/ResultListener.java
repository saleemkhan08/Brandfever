package co.thnki.brandfever.interfaces;

import com.android.volley.VolleyError;

public interface ResultListener<T>
{
    void onSuccess(T result);
    void onError(VolleyError error);
}
