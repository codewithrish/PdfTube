package com.searchresults.fragcom.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.searchresults.fragcom.MainActivity;
import com.searchresults.fragcom.R;
import com.searchresults.fragcom.TextChangedEvent;
import com.searchresults.fragcom.youtube.AppConstants;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import static com.searchresults.fragcom.youtube.AppConstants.PICKFILE_RESULT_CODE;

/**
 * Created by risha on 3/9/2017.
 */

public class TwoFragment extends Fragment {

    public static String VIDEO_ID = "J8EzIdaCRVg";


    View view = null;

    PDFView pdfView;

    EventBus bus = EventBus.getDefault();

    private YouTubePlayer YPlayer;
    private static final int RECOVERY_DIALOG_REQUEST = 1;


    YouTubePlayerSupportFragment mYoutubePlayerFragment;

    RelativeLayout youtube;
    View youtube_player;

    public TwoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //youTubePlayerFragment = (YouTubePlayerFragment)getFragmentManager().findFragmentById(R.id.youtube_player);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        bus.register(this);

        if(view == null) {
            view = inflater.inflate(R.layout.fragment_two, container, false);

            youtube = (RelativeLayout) view.findViewById(R.id.youtube);
            youtube_player = view.findViewById(R.id.youtube_player);
            setHasOptionsMenu(true);

            pdfView = (PDFView)view.findViewById(R.id.pdfView);
            pdfView.fromAsset("sample.pdf")
                    .load();

            mYoutubePlayerFragment = new YouTubePlayerSupportFragment();

            mYoutubePlayerFragment.initialize(AppConstants.KEY, new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestore) {
                    YPlayer = youTubePlayer;

                    YPlayer.setPlayerStateChangeListener(playerStateChangeListener);
                    YPlayer.setPlaybackEventListener(playbackEventListener);

                    if(!wasRestore) {
                        YPlayer.cueVideo(VIDEO_ID);
                    }
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult errorReason) {
                    if (errorReason.isUserRecoverableError()) {
                        errorReason.getErrorDialog(getActivity(), RECOVERY_DIALOG_REQUEST).show();
                    } else {
                        String errorMessage = String.format(
                                "There was an error initializing the YouTubePlayer",
                                errorReason.toString());
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
            });

            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.youtube, mYoutubePlayerFragment);
            fragmentTransaction.commit();
            return view;
        }
        else {
            return view;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(TextChangedEvent event) {
        //tv.setText(event.newText);

        final String vidId = event.newText.toString();

        if(event.newText.equals(vidId)) {
            //Toast.makeText(getActivity(), VIDEO_ID, Toast.LENGTH_SHORT).show();
            VIDEO_ID = vidId;
            youtube.setVisibility(View.VISIBLE);
            youtube_player.setVisibility(View.VISIBLE);
            YPlayer.cueVideo(VIDEO_ID);
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_two, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_browse:
                // do something based on first item click
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("application/pdf");
                startActivityForResult(intent,PICKFILE_RESULT_CODE);

                break;

            case R.id.action_expand_collapse:
                if(youtube.getVisibility() == View.VISIBLE) {
                    youtube.setVisibility(View.GONE);
                    youtube_player.setVisibility(View.GONE);
                    item.setIcon(R.drawable.ic_expand_more_white_24px);


                } else {
                    youtube.setVisibility(View.VISIBLE);
                    youtube_player.setVisibility(View.VISIBLE);
                    item.setIcon(R.drawable.ic_expand_less_white_24px);
                }
                break;

        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2

        if(data != null) {
            Uri pdfPath = data.getData();
            Toast.makeText(getActivity(), pdfPath.toString(), Toast.LENGTH_SHORT).show();
            pdfView.fromUri(pdfPath).load();


        } else {
            //Toast.makeText(getContext(), "No File Choosen", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private YouTubePlayer.PlaybackEventListener playbackEventListener = new YouTubePlayer.PlaybackEventListener() {
        @Override
        public void onPlaying() {

        }

        @Override
        public void onPaused() {

        }

        @Override
        public void onStopped() {

        }

        @Override
        public void onBuffering(boolean b) {

        }

        @Override
        public void onSeekTo(int i) {

        }
    };

    private YouTubePlayer.PlayerStateChangeListener playerStateChangeListener = new YouTubePlayer.PlayerStateChangeListener() {
        @Override
        public void onLoading() {

        }

        @Override
        public void onLoaded(String s) {
            YPlayer.play();
        }

        @Override
        public void onAdStarted() {

        }

        @Override
        public void onVideoStarted() {

        }

        @Override
        public void onVideoEnded() {

        }

        @Override
        public void onError(YouTubePlayer.ErrorReason errorReason) {

        }
    };
}


