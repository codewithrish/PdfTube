package com.searchresults.fragcom.nlp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.language.v1beta1.CloudNaturalLanguageAPIScopes;
import com.searchresults.fragcom.R;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by risha on 5/1/2017.
 */

public class AccessTokenLoader extends AsyncTaskLoader<String> {

    private static final String TAG = "AccessTokenLoader";

    private static final String PREFS = "AccessTokenLoader";
    private static final String PREF_ACCESS_TOKEN = "access_token";

    public AccessTokenLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public String loadInBackground() {

        final SharedPreferences prefs =
                getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String currentToken = prefs.getString(PREF_ACCESS_TOKEN, null);


        if (currentToken != null) {
            final GoogleCredential credential = new GoogleCredential()
                    .setAccessToken(currentToken)
                    .createScoped(CloudNaturalLanguageAPIScopes.all());
            final Long seconds = credential.getExpiresInSeconds();
            if (seconds != null && seconds > 3600) {
                return currentToken;
            }
        }

        final InputStream stream = getContext().getResources().openRawResource(R.raw.credential);
        try {
            final GoogleCredential credential = GoogleCredential.fromStream(stream)
                    .createScoped(CloudNaturalLanguageAPIScopes.all());
            credential.refreshToken();
            final String accessToken = credential.getAccessToken();
            prefs.edit().putString(PREF_ACCESS_TOKEN, accessToken).apply();
            return accessToken;
        } catch (IOException e) {
            Log.e(TAG, "Failed to obtain access token.", e);
        }
        return null;
    }
}
