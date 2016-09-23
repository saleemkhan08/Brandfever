package co.thnki.brandfever.interfaces;

public interface SettingsResultListener
{
    void onLocationSettingsTurnedOn();
    void onLocationSettingsCancelled();
    void onLocationSettingsPermanentlyDenied();
}
