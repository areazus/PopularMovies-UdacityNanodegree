package us.areaz.popularmovies;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbDiscover;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.Discover;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;

import static us.areaz.popularmovies.MovieDataAdapter.Movie_Backdrop_Path;
import static us.areaz.popularmovies.MovieDataAdapter.Movie_ID;
import static us.areaz.popularmovies.MovieDataAdapter.Movie_Overview;
import static us.areaz.popularmovies.MovieDataAdapter.Movie_Poster_Path;
import static us.areaz.popularmovies.MovieDataAdapter.Movie_Release_Date;
import static us.areaz.popularmovies.MovieDataAdapter.Movie_Title;
import static us.areaz.popularmovies.MovieDataAdapter.Movie_Vote_Average;
import static us.areaz.popularmovies.MovieDataAdapter.Movie_Vote_Count;

public class MainActivity extends Activity {
    public static final String Fragment_Tag = MainActivity.class.getSimpleName()+".Activity_Fragment";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.detail_activity_container) != null) {
            this.mTwoPane = true;
            if (savedInstanceState == null) {
                getFragmentManager().beginTransaction()
                        .add(R.id.detail_activity_container, new MovieDetailActivity.MovieDetailActivityFragment(), MovieDetailActivity.Fragment_Tag)
                        .commit();
            }
        }else{
            this.mTwoPane = false;
        }
    }

    public boolean isDualPane(){
        return this.mTwoPane;
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
        private SwipeRefreshLayout swipeRefreshLayout;
        LinearLayoutManager mLayoutManager;
        private MovieDataAdapter movieDataAdapter;
        private FavoriteMoviesSqlHelper sqlHelper;
        private String sortBy;
        private boolean showOnlyFav;

        public static final String Get_Favorites = "Get_Favorites";
        private List<Integer> favoriteList;
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
            FrameLayout rootView = (FrameLayout)inflater.inflate(R.layout.fragment_main, container, false);
            boolean dualMode = ((MainActivity)getActivity()).isDualPane();

            swipeRefreshLayout= (SwipeRefreshLayout)rootView.findViewById(R.id.swipeRefreshLayout);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    fetchMovies(1);
                }
            });
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.pictures_recycler_view);
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
                sqlHelper = new FavoriteMoviesSqlHelper(getActivity());
            }

            {
                int currentOrientation = getResources().getConfiguration().orientation;
                if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mLayoutManager = new GridLayoutManager(getActivity(), dualMode?3:4);
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
            return rootView;
        }

        public void onChangeSortingPref(){
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.sort_by_key), sortBy);
            editor.commit();
            fetchMovies(1);
        }

        public void onChangeFavoritePref(){
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Get_Favorites, showOnlyFav);
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
            } else if(id == R.id.showAll){
                item.setChecked(true);
                showOnlyFav = false;
                onChangeFavoritePref();
            }else if(id == R.id.onlyFav) {
                item.setChecked(true);
                showOnlyFav = true;
                onChangeFavoritePref();
            }
            return super.onOptionsItemSelected(item);
        }

        public void fetchMovies(int page){
            swipeRefreshLayout.setRefreshing(true);
            if(showOnlyFav){
                favoriteList = sqlHelper.getAll();
            }
            new FetchMovieDB().execute(getActivity().getString(R.string.tmdb_api_key), page + "", scrollTo + "", showOnlyFav?Get_Favorites:"");
        }

        public void showDetails(MovieDb movie) {
            Intent movieDetailIntent = new Intent(getActivity(), MovieDetailActivity.class);
            movieDetailIntent.putExtra(Movie_ID, movie.getId() + "");
            movieDetailIntent.putExtra(Movie_Backdrop_Path, movie.getBackdropPath());
            movieDetailIntent.putExtra(Movie_Overview, movie.getOverview());
            movieDetailIntent.putExtra(Movie_Poster_Path, movie.getPosterPath());
            movieDetailIntent.putExtra(Movie_Release_Date, movie.getReleaseDate());
            movieDetailIntent.putExtra(Movie_Title, movie.getTitle());
            movieDetailIntent.putExtra(Movie_Vote_Average, movie.getVoteAverage()+"");
            movieDetailIntent.putExtra(Movie_Vote_Count, movie.getVoteCount());

            if(((MainActivity) getActivity()).isDualPane()){
                MovieDetailActivity.MovieDetailActivityFragment detailActivityFragment = new MovieDetailActivity.MovieDetailActivityFragment();
                detailActivityFragment.setIntent(movieDetailIntent);
                getFragmentManager().beginTransaction()
                        .replace(R.id.detail_activity_container, detailActivityFragment, MovieDetailActivity.Fragment_Tag)
                        .commit();
            }else{

                startActivity(movieDetailIntent);
            }




        }

        public class FetchMovieDB  extends AsyncTask<String, Void, MovieResults> {
            private TmdbDiscover tmdbDiscover;
            private static final String emptymoviecreator = "{\"total_pages\":0,\"page\":0,\"total_results\":0,\"results\":[]}";
            MovieResultsPage movies;

            @Override
            protected MovieResults doInBackground(String... params) {
                try{
                if(params.length >= 4){
                    if(params[3].equals(Get_Favorites)){
                        //A special case when user asks to only display their favorite movies
                        ObjectMapper mapper = new ObjectMapper();
                        TmdbMovies movies = new TmdbApi(params[0]).getMovies();
                        MovieResults results = new MovieResults();
                        try {
                            //First we need to create the ResultPage object. Since its third party class and can't
                            //be modified directly, we create it using a fake json string
                            MovieResultsPage resultsPage = mapper.readValue(emptymoviecreator, MovieResultsPage.class);
                            List<MovieDb> moviesList = resultsPage.getResults();
                            if(favoriteList != null && favoriteList.size()>0) {
                                for (int id : favoriteList) {
                                    MovieDb movie = movies.getMovie(id, "en");
                                    if(movie != null){
                                        moviesList.add(movie);
                                    }
                                }
                                resultsPage.setTotalResults(moviesList.size());
                            }
                            if(sortBy.equals(getString(R.string.sort_by_popularity_key))){
                                MovieSorter.sortByPopularity(moviesList);
                            }else{
                                MovieSorter.sortByRating(moviesList);
                            }
                            results.setResultsPage(resultsPage);
                            return results;
                        } catch (IOException e) {
                            Log.e("FetchMovieDB", e.getMessage(), e);
                        }
                    }
                }
                {
                    //Part 1 code to fetch data using discover api
                    tmdbDiscover = new TmdbApi(params[0]).getDiscover();
                    int resultsNeeded = Integer.parseInt(params[2]);
                    MovieResults results = new MovieResults();
                    MovieResultsPage resultsPage = tmdbDiscover.getDiscover(new Discover().sortBy(sortBy).page(Integer.parseInt(params[1])));
                    results.setResultsPage(resultsPage);
                    while (results.getResultCount() < resultsNeeded && results.hasMorePages()) {
                        MovieResultsPage tempPage = tmdbDiscover.getDiscover(new Discover().sortBy(sortBy).page(results.getPage() + 1));
                        results.setResultsPage(resultsPage);
                    }
                    return results;
                }
                }catch (Exception e){
                    Log.e(this.getClass().getName(), e.getMessage(), e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(MovieResults param) {
                if(param == null){
                    return;
                }
                if(param.getPage() <2){
                    movieDataAdapter.clearResults();
                }
                movieDataAdapter.setResults(param);
                movieDataAdapter.notifyDataSetChanged();
                if(!loading){
                    mRecyclerView.scrollToPosition(scrollTo);
                }
                swipeRefreshLayout.setRefreshing(false);
                loading = false;
            }
        }
    }

}
