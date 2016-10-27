package co.thnki.brandfever.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import static co.thnki.brandfever.Brandfever.toast;

public class SizesUtil
{
    private Activity mActivity;

    public SizesUtil(Activity mActivity)
    {
        this.mActivity = mActivity;
    }

    public void showSizesDialog(int availableCount)
    {
        String[] array = new String[availableCount];
        for (int i = 0; i < availableCount; i++)
        {
            array[i] = String.valueOf(i+1);
        }


        AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
        dialog.setSingleChoiceItems(array, 0, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int position)
            {
                toast("position : "+position);
            }
        });
        dialog.show();
    }
}
