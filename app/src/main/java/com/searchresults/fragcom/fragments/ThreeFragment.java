package com.searchresults.fragcom.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.api.services.youtube.model.SearchResult;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.searchresults.fragcom.R;
import com.searchresults.fragcom.TextChangedEvent;
import com.searchresults.fragcom.connections.ServerResponseListener;
import com.searchresults.fragcom.connections.ServiceTask;
import com.searchresults.fragcom.youtube.YtAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;
import static com.searchresults.fragcom.R.id.viewpager;


public class ThreeFragment extends Fragment implements AdapterView.OnItemClickListener, ServerResponseListener {



    private YtAdapter mYtAdapter = null;
    private ServiceTask mYtServiceTask = null;

    View view = null;

    private ListView mYtVideoLsv = null;

    private MaterialSearchView searchView;

    public ThreeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        if(view == null) {
            view = inflater.inflate(R.layout.fragment_three, container, false);

            mYtVideoLsv = (ListView)view.findViewById(R.id.yt_video_lsv);
            mYtVideoLsv.setOnItemClickListener(this);
            searchView = (MaterialSearchView)view.findViewById(R.id.search_view);

            search("google");
            initializeSearch();

            return view;
        }
        else {
            return view;
        }
    }

    public void initializeSearch() {

        searchView.setVoiceSearch(false);
        searchView.setCursorDrawable(R.drawable.custom_cursor);
        searchView.setEllipsize(true);
        searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));


        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Snackbar.make(getView().findViewById(R.id.threeFragment), "Query: " + query, Snackbar.LENGTH_LONG)
                        .show();
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
                EventBus bus = EventBus.getDefault();
                bus.post(new TextChangedEvent("hide"));
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
                EventBus bus = EventBus.getDefault();
                bus.post(new TextChangedEvent("show"));
            }
        });
    }

    private void search(String query) {
        if (query.length() > 0) {

            // Service to search video
            mYtServiceTask = new ServiceTask(SEARCH_VIDEO);
            mYtServiceTask.setmServerResponseListener(this);
            mYtServiceTask.execute(query);
        } else {
            //AppUtils.showToast("Empty field");
            Toast.makeText(getContext(), "Empty field", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_three, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_search_mic) {
            //Toast.makeText(getContext(), "MIC", Toast.LENGTH_SHORT).show();
            promptSpeechInput();
        }
        return super.onOptionsItemSelected(item);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //txtSpeechInput.setText(result.get(0));
                    //Toast.makeText(getContext(), result.get(0), Toast.LENGTH_SHORT).show();
                    Snackbar.make(getView().findViewById(R.id.threeFragment), "Query: " + result.get(0), Snackbar.LENGTH_LONG)
                            .show();

                    search(result.get(0));
                }
                break;
            }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        SearchResult obj = (SearchResult) mYtAdapter.getItem(position);
        String vidId = obj.getId().getVideoId();

        EventBus bus = EventBus.getDefault();
        bus.post(new TextChangedEvent(vidId));

        ViewPager vp=(ViewPager) getActivity().findViewById(viewpager);
        vp.setCurrentItem(1);

    }


    @Override
    public void prepareRequest(Object... objects) {


        Log.i("Pre","Entered");
        // Parse the response based upon type of request
        Integer reqCode = (Integer) objects[0];

        if(reqCode==null || reqCode == 0)
            throw new NullPointerException("Request Code's value is Invalid.");
        String dialogMsg = null;
        switch (reqCode)
        {
            case SEARCH_VIDEO:
                dialogMsg = SEARCH_VIDEO_MSG;
                break;
        }

    }

    @Override
    public void goBackground(Object... objects) {
        Log.i("Background","Entered");
    }

    @Override
    public void completedRequest(Object... objects) {

        Log.i("Pre","Complete");

        // Parse the response based upon type of request
        Integer reqCode = (Integer) objects[0];

        if(reqCode==null || reqCode == 0)
            throw new NullPointerException("Request Code's value is Invalid.");

        switch (reqCode) {
            case SEARCH_VIDEO:

                if (mYtAdapter == null) {
                    mYtAdapter = new YtAdapter(getActivity());
                    mYtAdapter.setmVideoList((List<SearchResult>) objects[1]);
                    mYtVideoLsv.setAdapter(mYtAdapter);
                } else {
                    mYtAdapter.setmVideoList((List<SearchResult>) objects[1]);
                    mYtAdapter.notifyDataSetChanged();
                }

                break;
        }
    }

}
