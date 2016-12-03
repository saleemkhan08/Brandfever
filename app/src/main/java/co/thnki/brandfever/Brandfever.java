package co.thnki.brandfever;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class Brandfever extends MultiDexApplication
{
    public static String APP_NAME = "";
    private static Context context;
    @Override
    public void onCreate()
    {
        super.onCreate();
        context = this.getApplicationContext();

        if(!FirebaseApp.getApps(this).isEmpty())
        {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        APP_NAME = context.getString(R.string.app_name);
    }

    @Override
    public void onTerminate()
    {
        super.onTerminate();
        if(Build.VERSION.SDK_INT > 23)
        {
            Log.d("ConnectivityListener", "un - Registered" );
        }
    }

    public static SharedPreferences getPreferences()
    {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void toast(String str)
    {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }

    public static void toast(int str)
    {
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
    }
    public static Typeface getTypeFace()
    {
        return Typeface.createFromAsset(context.getAssets(), "Gabriola.ttf");
    }
    public static Context getAppContext()
    {
        return context;
    }


    public static String getResString(int resId)
    {
        return context.getResources().getString(resId);
    }

    public static String[] getResStringArray(int resId)
    {
        return context.getResources().getStringArray(resId);
    }

    public static String getResString(String stringName)
    {
        return context.getResources().getString(context.getResources().getIdentifier(stringName, "string", context.getPackageName()));
    }
}