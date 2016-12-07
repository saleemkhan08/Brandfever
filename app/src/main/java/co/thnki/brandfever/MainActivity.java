package co.thnki.brandfever;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.BindColor;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
{

    @BindColor(R.color.colorPrimaryDark)
    int COLOR_PRIMARY_DARK;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (Build.VERSION.SDK_INT >= 21)
        {
            getWindow().setStatusBarColor(COLOR_PRIMARY_DARK);
        }
    }
}
