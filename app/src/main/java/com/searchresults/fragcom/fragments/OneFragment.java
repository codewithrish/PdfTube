package com.searchresults.fragcom.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.searchresults.fragcom.R;
import com.searchresults.fragcom.RecyclerItemClickListener;
import com.searchresults.fragcom.events.SearchKeyWordEvent;
import com.searchresults.fragcom.events.SendUriData;
import com.searchresults.fragcom.nlp.AccessTokenLoader;
import com.searchresults.fragcom.nlp.ApiFragment;
import com.searchresults.fragcom.nlp.EntityInfo;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;

import static com.searchresults.fragcom.R.id.viewpager;


/**
 * Created by risha on 5/1/2017.
 */

public class OneFragment extends Fragment implements ApiFragment.Callback, View.OnClickListener{

    private static final String FRAGMENT_API = "api";

    private static final int LOADER_ACCESS_TOKEN = 1;

    private static final String STATE_SHOWING_RESULTS = "showing_results";

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                // The icon button is clicked; start analyzing the input.
                case R.id.analyze:
                    startAnalyze();
                    break;
            }
        }
    };

    private View mIntroduction;

    private View mResults;

    private View mProgress;

    private View view;

    private OneFragment.EntitiesAdapter mAdapter;

    private boolean mHidingResult;

    private EntityInfo entities[];

    private TextView status;

    private RecyclerView list;

    private static EntityInfo[] mEntities;

    EventBus bus = EventBus.getDefault();

    private Uri pdfUri = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if(!bus.isRegistered(this)) {
            bus.register(this);
        }

        if(view == null) {
            view = inflater.inflate(R.layout.fragment_one, container, false);
            view.findViewById(R.id.analyze).setOnClickListener(mOnClickListener);

            mIntroduction = view.findViewById(R.id.introduction);
            mResults = view.findViewById(R.id.results);
            mProgress = view.findViewById(R.id.progress);
            status = (TextView) view.findViewById(R.id.status);

            list = (RecyclerView)view.findViewById(R.id.list);
            list.setLayoutManager(new LinearLayoutManager(getActivity()));


            mAdapter = new OneFragment.EntitiesAdapter(getActivity(), entities);
            list.setAdapter(mAdapter);

            list.addOnItemTouchListener(
                    new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
                        @Override public void onItemClick(View view, int position) {

                            String s = mEntities[position].name;
                            //Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();

                            EventBus bus = EventBus.getDefault();
                            bus.post(new SearchKeyWordEvent(s));

                            ViewPager vp=(ViewPager) getActivity().findViewById(viewpager);
                            vp.setCurrentItem(2);
                            //Toast.makeText(getContext(), "Hello Dude", Toast.LENGTH_SHORT).show();
                            //ViewHolder item= ;
                            //Toast.makeText(getContext(), item.name + " is selected!", Toast.LENGTH_SHORT).show();
                        }
                    })
            );


            FragmentManager fm = getChildFragmentManager();

            if (savedInstanceState == null) {
                // The app has just launched; handle share intent if it is necessary
                handleShareIntent();
            } else {
                // Configuration changes; restore UI states
                boolean results = savedInstanceState.getBoolean(STATE_SHOWING_RESULTS);
                if (results) {
                    mIntroduction.setVisibility(View.GONE);
                    mResults.setVisibility(View.VISIBLE);
                    mProgress.setVisibility(View.INVISIBLE);
                } else {
                    mResults.setVisibility(View.INVISIBLE);
                }
            }

            if (getApiFragment() == null) {
                fm.beginTransaction().add(new ApiFragment(), FRAGMENT_API).commit();
            }
            prepareApi();
        }
        return view;
    }

    private ApiFragment getApiFragment() {
        return (ApiFragment) getChildFragmentManager().findFragmentByTag(FRAGMENT_API);
    }

    private void handleShareIntent() {
        final Intent intent = getActivity().getIntent();
        if (TextUtils.equals(intent.getAction(), Intent.ACTION_SEND)
                && TextUtils.equals(intent.getType(), "text/plain")) {
            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (text != null) {
                //mInput.setText(text);
            }
        }
    }

    private void prepareApi() {
        // Initiate token refresh
        getActivity().getSupportLoaderManager().initLoader(LOADER_ACCESS_TOKEN, null,
                new LoaderManager.LoaderCallbacks<String>() {
                    @Override
                    public Loader<String> onCreateLoader(int id, Bundle args) {
                        return new AccessTokenLoader(getActivity());
                    }

                    @Override
                    public void onLoadFinished(Loader<String> loader, String token) {
                        getApiFragment().setAccessToken(token);
                    }

                    @Override
                    public void onLoaderReset(Loader<String> loader) {
                    }
                });
    }

    @Override
    public void onEntitiesReady(EntityInfo[] entities) {
        showResults();
        mAdapter.setEntities(entities);
        this.entities = entities;
    }

    private String evaluate;

    private void startAnalyze() {

        showProgress();
        new ExtractTextFromPdf().execute();
        //getApiFragment().analyzeEntities("");
    }

    @Override
    public void onClick(View v) {
        int itemPosition = list.indexOfChild(v);
        Log.e("Clicked",String.valueOf(itemPosition));
    }

    private class ExtractTextFromPdf extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... urls) {
            return stripText();
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            status.setText("Extracting Text ...");
        }

        protected void onPostExecute(String result) {
            status.setText("Getting Suggestions ...");
            getApiFragment().analyzeEntities(result);
            status.setText("Click To Get Suggestions");
            //status.setText("Result Calculated");
            Toast.makeText(getContext(), "Result Calculated", Toast.LENGTH_SHORT).show();
        }
    }



    private void showResults() {
        mIntroduction.setVisibility(View.GONE);
        if (mProgress.getVisibility() == View.VISIBLE) {
            ViewCompat.animate(mProgress)
                    .alpha(0.f)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            view.setVisibility(View.INVISIBLE);
                        }
                    });
        }
        if (mHidingResult) {
            ViewCompat.animate(mResults).cancel();
        }
        if (mResults.getVisibility() == View.INVISIBLE) {
            mResults.setVisibility(View.VISIBLE);
            ViewCompat.setAlpha(mResults, 0.01f);
            ViewCompat.animate(mResults)
                    .alpha(1.f)
                    .setListener(null)
                    .start();
        }
    }
    private void showProgress() {
        mIntroduction.setVisibility(View.GONE);
        if (mResults.getVisibility() == View.VISIBLE) {
            mHidingResult = true;
            ViewCompat.animate(mResults)
                    .alpha(0.f)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            mHidingResult = false;
                            view.setVisibility(View.INVISIBLE);
                        }
                    });
        }
        if (mProgress.getVisibility() == View.INVISIBLE) {
            mProgress.setVisibility(View.VISIBLE);
            ViewCompat.setAlpha(mProgress, 0.f);
            ViewCompat.animate(mProgress)
                    .alpha(1.f)
                    .setListener(null)
                    .start();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name;
        public TextView type;
        public TextView salience;
        public TextView wikipediaUrl;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_entity, parent, false));
            name = (TextView) itemView.findViewById(R.id.name);
            type = (TextView) itemView.findViewById(R.id.type);
            salience = (TextView) itemView.findViewById(R.id.salience);
            wikipediaUrl = (TextView) itemView.findViewById(R.id.wikipedia_url);
        }

    }

    private static class EntitiesAdapter extends RecyclerView.Adapter<OneFragment.ViewHolder> {

        private final Context mContext;


        public EntitiesAdapter(Context context, EntityInfo[] entities) {
            mContext = context;
            mEntities = entities;
        }

        @Override
        public OneFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new OneFragment.ViewHolder(LayoutInflater.from(mContext), parent);
        }

        @Override
        public void onBindViewHolder(OneFragment.ViewHolder holder, int position) {
            EntityInfo entity = mEntities[position];
            holder.name.setText(entity.name);
            holder.type.setText(entity.type);
            holder.salience.setText(mContext.getString(R.string.salience_format, entity.salience));
            holder.wikipediaUrl.setText(entity.wikipediaUrl);
            Linkify.addLinks(holder.wikipediaUrl, Linkify.WEB_URLS);
        }

        @Override
        public int getItemCount() {
            return mEntities == null ? 0 : mEntities.length;
        }

        public void setEntities(EntityInfo[] entities) {
            mEntities = entities;
            notifyDataSetChanged();
        }
    }

    public String stripText() {
        String parsedText = null;
        PDDocument document = null;

        try {
            if(pdfUri == null) {
                document = PDDocument.load(getActivity().getAssets().open("resume.pdf"));
            } else  {
                // pdfUri.getPath()).toString()
                document = PDDocument.load(new File(pdfUri.getPath().toString()).getAbsoluteFile());
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(0);
            pdfStripper.setEndPage(1);
            parsedText = "Parsed text: " + pdfStripper.getText(document);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (document != null) document.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Log.i("Parsed Text", parsedText);
        return parsedText;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SendUriData event) {
        //tv.setText(event.newText);

        Uri pdfPath = event.uri;

        pdfUri = pdfPath;

        Toast.makeText(getContext(), new File(pdfUri.getPath()).toString(), Toast.LENGTH_LONG).show();

    }
}
