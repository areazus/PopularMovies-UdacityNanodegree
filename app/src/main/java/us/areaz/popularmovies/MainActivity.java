package us.areaz.popularmovies;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbDiscover;
import info.movito.themoviedbapi.model.Discover;
import info.movito.themoviedbapi.model.core.MovieResultsPage;

public class MainActivity extends Activity {
    public static final String Fragment_Tag = MainActivity.class.getSimpleName()+".Activity_Fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            MainActivityFragment mainActivityFragment = new MainActivityFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.main_activity_container, mainActivityFragment, Fragment_Tag)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public static class MainActivityFragment extends Fragment {
        private RecyclerView mRecyclerView;
        LinearLayoutManager mLayoutManager;
        private MovieDataAdapter movieDataAdapter;
        private String sortBy;

        //testing
        private boolean loading = false;
        private int pastVisiblesItems, currentVisibleItems, totslItems;
        private int scrollTo;

        public void setScrollTo(int scrollTo){
            this.scrollTo = scrollTo;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getActivity().setContentView(R.layout.fragment_main);
            mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.pictures_recycler_view);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setHorizontalScrollBarEnabled(false);
            mRecyclerView.setVerticalScrollBarEnabled(false);
            setHasOptionsMenu(true);
            setRetainInstance(true);
            if(savedInstanceState == null) {
                movieDataAdapter = new MovieDataAdapter();
                fetchMovies(1);
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                sortBy = sharedPref.getString(getString(R.string.sort_by_key),
                        getString(R.string.sort_by_popularity_key));
            }

            {
                int currentOrientation = getResources().getConfiguration().orientation;
                if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mLayoutManager = new GridLayoutManager(getActivity(), 4);

                } else {
                    mLayoutManager = new GridLayoutManager(getActivity(), 2);
                }
                mRecyclerView.setLayoutManager(mLayoutManager);
                mRecyclerView.setAdapter(movieDataAdapter);
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (dy > 0) {
                            currentVisibleItems = mLayoutManager.getChildCount();
                            totslItems = mLayoutManager.getItemCount();
                            pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();
                            if (currentVisibleItems + pastVisiblesItems >= totslItems - 2 * currentVisibleItems && !loading) {
                                //We are almost at end, lets increase the results
                                MovieResults results = movieDataAdapter.getResults();
                                if (results.hasMorePages()) {
                                    fetchMovies(results.getPage() + 1);
                                    loading = true;
                                }
                            }
                        }
                    }
                });
            }
            return inflater.inflate(R.layout.fragment_main, container, false);
        }

        public void onChangeSortingPref(){
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.sort_by_key), sortBy);
            editor.commit();
            fetchMovies(1);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if(id == R.id.sort_by_popularity){
                sortBy = getString(R.string.sort_by_popularity_key);
                onChangeSortingPref();
            }else if(id == R.id.sort_by_rating){
                sortBy = getString(R.string.sort_by_rating_key);
                onChangeSortingPref();
            }
            return super.onOptionsItemSelected(item);
        }

        public void fetchMovies(int page){
            new FetchMovieDB().execute(getActivity().getString(R.string.tmdb_api_key), page + "", scrollTo + "");
        }

        public class FetchMovieDB  extends AsyncTask<String, Void, MovieResults> {
            private TmdbDiscover tmdbDiscover;
            MovieResultsPage movies;
            @Override
            protected MovieResults doInBackground(String... params) {
                tmdbDiscover = new TmdbApi(params[0]).getDiscover();
                int resultsNeeded = Integer.parseInt(params[2]);
                MovieResults results = new MovieResults();
                MovieResultsPage resultsPage = tmdbDiscover.getDiscover(new Discover().sortBy(sortBy).page(Integer.parseInt(params[1])));
                results.setResultsPage(resultsPage);
                while (results.getResultCount() < resultsNeeded && results.hasMorePages()) {
                    MovieResultsPage tempPage = tmdbDiscover.getDiscover(new Discover().sortBy(sortBy).page(results.getPage()+1));
                    results.setResultsPage(resultsPage);
                }
                return results;
            }

            @Override
            protected void onPostExecute(MovieResults param) {
                movieDataAdapter.setResults(param);
                movieDataAdapter.notifyDataSetChanged();
                if(!loading){
                    mRecyclerView.scrollToPosition(scrollTo);
                }
                loading = false;
            }

        }
    }

}
