package co.thnki.brandfever.receivers;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import co.thnki.brandfever.services.NotificationJobService;
import co.thnki.brandfever.utils.ConnectivityUtil;

public class ChargerConnectionReceiver extends BroadcastReceiver
{
    private static final int NOTIFICATION_JOB_NUM = 109;

    public ChargerConnectionReceiver()
    {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (Build.VERSION.SDK_INT >= 21)
        {
            ComponentName componentName = new ComponentName(context, NotificationJobService.class);
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo jobInfo = new JobInfo.Builder(NOTIFICATION_JOB_NUM, componentName)
                    .setPersisted(true)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .build();
            jobScheduler.schedule(jobInfo);
        }
        else
        {
            if (ConnectivityUtil.isConnected())
            {
                //TODO context.startService(new Intent(context, ShowNotificationService.class));
            }
        }
    }
}
