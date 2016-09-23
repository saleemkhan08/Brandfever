package co.thnki.brandfever.interfaces;

public interface GeoCodeListener
{
    void onAddressObtained(String result);
    void onGeoCodingFailed();
    void onCancelled();
}
