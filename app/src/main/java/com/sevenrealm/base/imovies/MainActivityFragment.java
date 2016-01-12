package com.sevenrealm.base.imovies;

import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.sevenrealm.base.imovies.provider.Contract.MovieEntry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    ArrayList<MovieDataModel> movies = new ArrayList<>();
    GridView gridView;
    public int id = 0;

    private LoaderManager loaderManager;

    private int MOVIES_LOADER = 0;
    private int FAV_LOADER = 1;
    private int HIGH_RATE_LOADER = 2;

    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_UID = 1;
    static final int COL_MOVIE_FAV = 2;
    static final int COL_MOVIE_TITLE = 3;
    static final int COL_MOVIE_IMAGE = 4;
    static final int COL_MOVIE_RELEASE = 5;
    static final int COL_MOVIE_OVERVIEW = 6;

    ImageAdapter imageAdapter;
    private static final String[] MOVIE_COLUMNS = {
            MovieEntry.TABLE_NAME + "." + MovieEntry._ID,
            MovieEntry.COLUMN_ID,
            MovieEntry.COLUMN_FAV,
            MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_IMAGE_PATH,
            MovieEntry.COLUMN_RELEASE_DATE,
            MovieEntry.COLUMN_OVERVIEW
    };

    public MainActivityFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        loaderManager = getLoaderManager();
        loaderManager.restartLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_fragment_menu, menu);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        imageAdapter = new ImageAdapter(getActivity(),null,0);
        movies.clear();
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        gridView = (GridView) view.findViewById(R.id.grid_view);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        new loadingData(0).execute();
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_vote) {
            loaderManager.restartLoader(HIGH_RATE_LOADER, null, this);
            new loadingData(1).execute();
            return true;
        }if (id == R.id.action_desc) {
            loaderManager.restartLoader(MOVIES_LOADER, null, this);
            new loadingData(0).execute();
            return true;
        }if (id == R.id.action_fav){
            loaderManager.restartLoader(FAV_LOADER, null, this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == MOVIES_LOADER) {
            return new CursorLoader(getActivity(), MovieEntry.CONTENT_URI, MOVIE_COLUMNS, null, null, MovieEntry._ID + " ASC");
        }else if(id == HIGH_RATE_LOADER){
            return new CursorLoader(getActivity(), MovieEntry.CONTENT_URI, MOVIE_COLUMNS, null, null, MovieEntry.COLUMN_VOTE_RATE + " DESC");
        }else if (id == FAV_LOADER){
            return new CursorLoader(getActivity(), MovieEntry.CONTENT_URI, MOVIE_COLUMNS, MovieEntry.COLUMN_FAV + " = ?",new String[]{String.valueOf(1)}, MovieEntry._ID + " ASC");
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        imageAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        imageAdapter.swapCursor(null);
    }

    private class loadingData extends AsyncTask {

        JSONArray results;
        private int orderFlag = 0;

        public loadingData(int orderFlag) {
            this.orderFlag = orderFlag;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            movies.clear();
        }

        @Override
        protected void onPostExecute(Object o) {
            gridView.setAdapter(imageAdapter);
            gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    Core core = new Core(getActivity());
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    int ID = cursor.getInt(COL_MOVIE_ID);
                    if (cursor.getInt(COL_MOVIE_FAV) == 1) {
                        core.updateFavoriteDB(0, String.valueOf(ID));
                        Toast.makeText(getActivity(), cursor.getString(COL_MOVIE_TITLE) + " removed from Favorite", Toast.LENGTH_SHORT).show();
                    }else {
                        core.updateFavoriteDB(1, String.valueOf(ID));
                        Toast.makeText(getActivity(), cursor.getString(COL_MOVIE_TITLE) + " Added To Favorite", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    DetailsFragment fragment2 = new DetailsFragment();
                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                    fragment2.setID(cursor.getInt(COL_MOVIE_UID));
                    FragmentManager fragmentManager = getFragmentManager();
                    if (getActivity().findViewById(R.id.fragment_pane) != null){
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment, fragment2);
                        fragmentTransaction.commit();
                    }else {
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.fragment, fragment2);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();

                    }

                }
            });
        }

//        @Override
//        protected void onProgressUpdate(Object[] values) {
//            if (values[0].equals("1")) {
//                Toast.makeText(getActivity(), "Failed To Load Data", Toast.LENGTH_SHORT).show();
//            }
//        }

        @Override
        protected Object doInBackground(Object[] params) {
            Core core = new Core(getActivity());
            try {
                JSONObject data = null;
                if (orderFlag == 0) {
                    data = core.getMovies();
                }else if (orderFlag == 1) {
                    data = core.getMoviesRate();
                }else  if (orderFlag == 2) {
                    data = core.getMoviesAsc();
                }
                if (data != null){
                    results = data.getJSONArray("results");
                    for (int i = 0; i < results.length() ;i++){
                        JSONObject movie = results.getJSONObject(i);
                        MovieDataModel m = new MovieDataModel();
                        m.setId(movie.getInt("id"));
                        m.setPoster_path(movie.getString("poster_path"));
                        m.setTitle(movie.getString("title"));
                        Log.d("Movie Title", movie.getString("title"));
                        movies.add(m);
                    }
                    Log.d("data", data.toString());
                }
            } catch (Exception e) {
                String error = "1";
                publishProgress(error);
            }
            return null;
        }
    }

}