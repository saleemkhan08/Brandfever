package co.thnki.brandfever.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;

import com.squareup.otto.Subscribe;

import co.thnki.brandfever.singletons.Otto;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NotificationJobService extends JobService
{
    public static final String JOB_FINISHED = "jobFinished";
    public static final String JOB_NOT_FINISHED = "jobNotFinished";
    JobParameters mJobParameters;
    @Override
    public boolean onStartJob(JobParameters jobParameters)
    {
        Otto.register(this);
        mJobParameters = jobParameters;
        //TODO startService(new Intent(this, ShowNotificationService.class));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                jobFinished(mJobParameters, false);
            }
        }, 1000);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters)
    {
        return true;
    }

    @Subscribe
    public void isJobFinished(String status)
    {
        switch (status)
        {
            case JOB_FINISHED:
                jobFinished(mJobParameters, false);
                break;
            case JOB_NOT_FINISHED:
                jobFinished(mJobParameters, true);
                break;
        }
    }
}
