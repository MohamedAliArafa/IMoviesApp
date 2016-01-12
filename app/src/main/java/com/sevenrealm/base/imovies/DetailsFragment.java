package com.sevenrealm.base.imovies;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sevenrealm.base.imovies.provider.Contract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;




public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    ImageView imageView;
    TextView title,genre,vote,tag,desc,reviewTxt,trailerText;
    loading loading;
    loadingReviews loadingReviews;
    loadingTrailers loadingTrailers;
    ListView reviewList,trailersList;
    Button favButton;
    int id = 0;

    Cursor cursorMovie,cursorReview,cursorVideo;

    private static final int MOVIES_LOADER = 0;
    private static final int REVIEWS_LOADER = 1;
    private static final int VIDEOS_LOADER = 2;

    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_UID = 1;
    static final int COL_MOVIE_FAV = 2;
    static final int COL_MOVIE_TITLE = 3;
    static final int COL_MOVIE_IMAGE = 4;
    static final int COL_MOVIE_RELEASE = 5;
    static final int COL_MOVIE_OVERVIEW = 6;

    static final int COL_REVIEW_MOVIE_ID = 0;
    static final int COL_REVIEW_ID = 1;
    static final int COL_AUTHOR = 2;
    static final int COL_CONTENT = 3;

    static final int COL_VIDEO_MOVIE_ID = 1;
    static final int COL_VIDEO_ID = 2;
    static final int COL_NAME = 3;
    static final int COL_KEY = 4;
    static final int COL_SITE = 5;

    private ShareActionProvider mShareActionProvider;
    private String mVideo,mTitle;

    JSONObject reviews;
    JSONArray results;
    reviewAdapter arrayAdapter;

    JSONObject trailers;
    JSONArray results1;
    videoAdapter arrayAdapter2;

    private static final String[] MOVIE_COLUMNS = {
            Contract.MovieEntry.TABLE_NAME + "." + Contract.MovieEntry._ID,
            Contract.MovieEntry.COLUMN_ID,
            Contract.MovieEntry.COLUMN_FAV,
            Contract.MovieEntry.COLUMN_TITLE,
            Contract.MovieEntry.COLUMN_IMAGE_PATH,
            Contract.MovieEntry.COLUMN_RELEASE_DATE,
            Contract.MovieEntry.COLUMN_OVERVIEW
    };

    private static final String[] REVIEW_COLUMNS = {
            Contract.ReviewEntry.TABLE_NAME + "." + Contract.ReviewEntry._ID,
            Contract.ReviewEntry.COLUMN_MOVIE_ID,
            Contract.ReviewEntry.COLUMN_REVIEW_ID,
            Contract.ReviewEntry.COLUMN_AUTHOR,
            Contract.ReviewEntry.COLUMN_CONTENT
    };

    private static final String[] VIDEO_COLUMNS = {
            Contract.VideoEntry.TABLE_NAME + "." + Contract.VideoEntry._ID,
            Contract.VideoEntry.COLUMN_MOVIE_ID,
            Contract.VideoEntry.COLUMN_VIDEO_ID,
            Contract.VideoEntry.COLUMN_NAME,
            Contract.VideoEntry.COLUMN_KEY,
            Contract.VideoEntry.COLUMN_SITE,
    };

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        getLoaderManager().initLoader(REVIEWS_LOADER, null, this);
        getLoaderManager().initLoader(VIDEOS_LOADER, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detail_fragment_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        MenuItemCompat.setActionProvider(menuItem, mShareActionProvider);
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        if (cursorMovie != null && cursorMovie.moveToFirst() && cursorReview != null && cursorVideo.moveToFirst()) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Watch That Trailer (" + cursorMovie.getString(COL_MOVIE_TITLE) + ") " + "http://www.youtube.com/watch?v=" + cursorVideo.getString(COL_KEY));
        }
        return shareIntent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        loading = new loading();
        loadingReviews = new loadingReviews();
        loadingTrailers = new loadingTrailers();
//        loading.execute();
        loadingReviews.execute();
        loadingTrailers.execute();
        mShareActionProvider = new ShareActionProvider(getContext());
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }

    public void setID(int id){
        this.id = id;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        imageView = (ImageView) view.findViewById(R.id.imageView);
        title = (TextView) view.findViewById(R.id.textView);
        genre = (TextView) view.findViewById(R.id.textView2);
        vote = (TextView) view.findViewById(R.id.textView3);
        tag = (TextView) view.findViewById(R.id.textView4);
        desc = (TextView) view.findViewById(R.id.textView5);
        trailerText = (TextView) view.findViewById(R.id.textView6);
        reviewTxt = (TextView) view.findViewById(R.id.textView7);
        reviewList = (ListView) view.findViewById(R.id.listView2);
        trailersList = (ListView) view.findViewById(R.id.listView);
        favButton = (Button) view.findViewById(R.id.favBtn);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MOVIES_LOADER:
                return new CursorLoader(getActivity(), Contract.MovieEntry.CONTENT_URI, MOVIE_COLUMNS, Contract.MovieEntry.COLUMN_ID + " = ?", new String[]{String.valueOf(this.id)}, Contract.MovieEntry._ID + " ASC");
            case REVIEWS_LOADER:
                return new CursorLoader(getActivity(), Contract.ReviewEntry.CONTENT_URI, REVIEW_COLUMNS, Contract.ReviewEntry.COLUMN_MOVIE_ID + " = ?", new String[]{String.valueOf(this.id)}, Contract.ReviewEntry._ID + " ASC");
            case VIDEOS_LOADER:
                return new CursorLoader(getActivity(), Contract.VideoEntry.CONTENT_URI, VIDEO_COLUMNS, Contract.VideoEntry.COLUMN_MOVIE_ID + " = ?", new String[]{String.valueOf(this.id)}, Contract.VideoEntry._ID + " ASC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch(loader.getId()) {
            case MOVIES_LOADER:
                cursorMovie = data;
                if (cursorMovie.moveToFirst()) {
                    String url = new Core(getActivity()).large_image_url + cursorMovie.getString(COL_MOVIE_IMAGE);
                    Picasso.with(getActivity()).load(url).into(imageView);
                    title.setText(cursorMovie.getString(COL_MOVIE_TITLE));
                    genre.setText(cursorMovie.getString(COL_MOVIE_RELEASE).substring(0, 4));
                    desc.setText(cursorMovie.getString(COL_MOVIE_OVERVIEW));
                    favButton.setVisibility(View.VISIBLE);
                    final Core core = new Core(getActivity());
                    favButton.setVisibility(View.VISIBLE);
                    if (cursorMovie.getInt(COL_MOVIE_FAV) == 0) {
                        favButton.setText("Add to Favorite");
                    } else {
                        favButton.setText("Remove from Favorite");
                    }
                    favButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (cursorMovie.getInt(COL_MOVIE_FAV) == 0) {
                                core.updateFavoriteDB(1, cursorMovie.getString(COL_MOVIE_ID));
                            } else {
                                core.updateFavoriteDB(0, cursorMovie.getString(COL_MOVIE_ID));
                            }
                        }
                    });
                }
                break;
            case REVIEWS_LOADER:
                cursorReview = data;
                if (cursorReview.moveToFirst()) {
                    reviewTxt.setText("Reviews");
                    try {
                        arrayAdapter = new reviewAdapter(getActivity(),R.layout.review_item,cursorReview,0);
                        reviewList.setAdapter(arrayAdapter);
                        setListViewHeightBasedOnChildren(reviewList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case VIDEOS_LOADER:
                cursorVideo = data;
                if (cursorVideo.moveToFirst()) {
                    trailerText.setText("Trailers");
                    do {
                        mVideo = "http://www.youtube.com/watch?v=" + cursorVideo.getString(COL_KEY);
                        trailersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + cursorVideo.getString(COL_KEY)));
                                startActivity(intent);
                            }
                        });
                    }while (cursorVideo.moveToNext());
                    try {
                        arrayAdapter2 = new videoAdapter(getActivity(),R.layout.review_item,cursorVideo,0);
                        trailersList.setAdapter(arrayAdapter2);
                        setListViewHeightBasedOnChildren(trailersList);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (cursorVideo != null && cursorMovie != null) {
                    mShareActionProvider.setShareIntent(createShareForecastIntent());
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private class loading extends AsyncTask {

        JSONObject movie;
//
//        @Override
//        protected void onPostExecute(Object o) {
//            String url;
////            String gen = "";
//            String v;
//            if (id != 0) {
//                try {
//                    url = new Core(getActivity()).large_image_url + movie.getString("poster_path");
//                    Picasso.with(getActivity()).load(url).into(imageView);
//                    title.setText(movie.getString("title"));
//                    genre.setText(movie.getString("release_date").substring(0,4));
//                    v = "Vote " + movie.getString("vote_average") + " / 10 ";
//                    vote.setText(v);
//                    tag.setText(movie.getString("tagline"));
//                    desc.setText(movie.getString("overview"));
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        @Override
        protected Object doInBackground(Object[] params) {
            Core core = new Core(getActivity());
            if (id != 0) {
                try {
                    movie = core.getMovieById(id);
//                    Log.d("movie", movie.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    private class loadingReviews extends AsyncTask {

//        ArrayList<String> Reviewlist;
//
//        @Override
//        protected void onPostExecute(Object o) {
//            reviewTxt.setText("Reviews");
//            String review = null;
//            if (results != null) {
//                for (int i = 0; i < results.length(); i++) {
//                    try {
//                        JSONObject movie = results.getJSONObject(i);
//                        review = movie.getString("author") + " : \n" + movie.getString("content");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    Reviewlist.add(review);
//                }
//                try {
//                    arrayAdapter = new reviewAdapter(getActivity(),R.layout.review_item,cursorReview,0);
//                    reviewList.setAdapter(arrayAdapter);
//                    setListViewHeightBasedOnChildren(reviewList);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }

        @Override
        protected Object doInBackground(Object[] params) {
            Core core = new Core(getActivity());
            if (id != 0) {
                try {
                    reviews = core.getMovieReview(id);
//                    results = reviews.getJSONArray("results");
//                    Log.d("movie", movie.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    private class loadingTrailers extends AsyncTask {

//        ArrayList<String> trailerList;

//        @Override
//        protected void onPostExecute(Object o) {
//            trailerText.setText("Trailers");
//            String trailer = null;
//            trailerList = new ArrayList<>();
//            if (results != null) {
//                try {
//                    mVideo = "http://www.youtube.com/watch?v=" + results.getJSONObject(0).getString("key");
//                    mTitle = results.getJSONObject(0).getString("name");
//
//                for (int i = 0; i < results.length(); i++) {
//                    final JSONObject movie = results.getJSONObject(i);
//                    trailer = movie.getString("type") + " : " + movie.getString("name") + " \n" + movie.getString("site");
//                    trailersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        @Override
//                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                            try {
//                                Toast.makeText(getActivity(), movie.getString("name"), Toast.LENGTH_SHORT).show();
//                                try {
//                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + movie.getString("key")));
//                                    startActivity(intent);
//                                } catch (ActivityNotFoundException ex) {
//                                    Intent intent = new Intent(Intent.ACTION_VIEW,
//                                            Uri.parse("http://www.youtube.com/watch?v=" + movie.getString("key")));
//                                    startActivity(intent);
//                                }
////                                    Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + movie.getString("key")));
////                                    startActivity(in);
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    });
//                    trailerList.add(trailer);
//                }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                try {
//                    arrayAdapter2 = new videoAdapter(getActivity(), R.layout.review_item, cursorVideo,0);
//                    trailersList.setAdapter(arrayAdapter2);
//                    setListViewHeightBasedOnChildren(trailersList);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        }

        @Override
        protected Object doInBackground(Object[] params) {
            Core core = new Core(getActivity());
            if (id != 0) {
                try {
                    trailers = core.getMovieTrailers(id);
                    if (trailers != null) {
                        results1 = trailers.getJSONArray("results");
                    }
//                    Log.d("movie", movie.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    protected void finalize() throws Throwable {
        if (loading!= null) {
            loading.cancel(true);
        }
        if (loadingReviews!= null) {
            loadingReviews.cancel(true);
        }
        if (loadingTrailers!= null) {
            loadingTrailers.cancel(true);
        }
        super.finalize();
    }
}