package co.thnki.brandfever.interfaces;

import java.util.Map;

public interface IOnTokensUpdatedListener
{
    void onTokensUpdated(Map<String, String> ownersTokenList);
}
