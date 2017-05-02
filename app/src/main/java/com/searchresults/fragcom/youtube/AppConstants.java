package com.searchresults.fragcom.youtube;

import com.google.android.gms.common.Scopes;
import com.google.api.services.youtube.YouTubeScopes;

public interface AppConstants {

    public static final String pdfText = "";

    public static final int SEARCH_VIDEO = 1;

    public static final int PICKFILE_RESULT_CODE = 1;

    public static final String SEARCH_VIDEO_MSG = "Searching Videos";

    public static final int REQ_CODE_SPEECH_INPUT = 100;

    public static final String DIALOG_TITLE = "Loading";

    public static final long NUMBER_OF_VIDEOS_RETURNED = 25;
    public static final String APP_NAME = LatentLoaderApplication.appName();

    // Register an API key here: https://code.google.com/apis/console
    // Note : This is the browser key instead of android key as Android key was generating Service config errors (403)
    public static final String KEY = "AIzaSyC81bC6TCYo_y7TvPo4Mi441EXfm58OrB8";
    public static final String[] SCOPES = {Scopes.PROFILE, YouTubeScopes.YOUTUBE};
}
