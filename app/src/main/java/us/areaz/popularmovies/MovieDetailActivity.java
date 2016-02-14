package us.areaz.popularmovies;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;

import static us.areaz.popularmovies.MovieDataAdapter.Movie_Backdrop_Path;
import static us.areaz.popularmovies.MovieDataAdapter.Movie_Overview;
import static us.areaz.popularmovies.MovieDataAdapter.Movie_Poster_Path;
import static us.areaz.popularmovies.MovieDataAdapter.Movie_Release_Date;
import static us.areaz.popularmovies.MovieDataAdapter.Movie_Title;
import static us.areaz.popularmovies.MovieDataAdapter.Movie_Vote_Average;

/**
 * Created by ahmed on 2/12/2016.
 */
public class MovieDetailActivity extends Activity {
    public static final String Fragment_Tag = MovieDetailActivity.class.getSimpleName()+".Activity_Fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.detail_activity_container, new MovieDetailActivityFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    public static class MovieDetailActivityFragment extends Fragment {
        private ScrollView rootView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = (ScrollView) inflater.inflate(R.layout.fragment_detail, container, false);
            Intent intent = getActivity().getIntent();

            String imageURL = getString(R.string.image_fetch_url_backdrop)+intent.getStringExtra(Movie_Backdrop_Path);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.detailed_movie_backdrop_view);
            Picasso.with(inflater.getContext()).load(imageURL).fit().centerCrop().into(imageView);

            imageURL = getString(R.string.image_fetch_url_poster)+intent.getStringExtra(Movie_Poster_Path);
            Picasso.with(inflater.getContext()).load(imageURL).into((ImageView) rootView.findViewById(R.id.detailed_movie_poster_view));


            String message = intent.getStringExtra(Movie_Title);
            TextView textView = (TextView)rootView.findViewById(R.id.detailed_movie_title);
            textView.setText(message);

            message = intent.getStringExtra(Movie_Vote_Average);
            textView = (TextView)rootView.findViewById(R.id.detailed_movie_rating);
            textView.setText(message+" / 10");

            message = intent.getStringExtra(Movie_Release_Date);
            textView = (TextView)rootView.findViewById(R.id.detailed_movie_release_date);
            textView.setText(message);

            message = intent.getStringExtra(Movie_Overview);
            textView = (TextView)rootView.findViewById(R.id.detailed_movie_overview);
            textView.setText(message);


            return rootView;
        }

        public void initView(MovieDb movieDb) {
            if (movieDb != null) {
                //For Part 2:
            }
        }


        public class FetchMovie  extends AsyncTask<String, Void, MovieDb> {
            //ToBeUsedLater: for trailed and any other network activity
            @Override
            protected MovieDb doInBackground(String... params) {
                TmdbMovies movies = new TmdbApi(params[0]).getMovies();
                MovieDb movie = movies.getMovie(Integer.parseInt(params[1]), "en");
                return movie;
            }

            @Override
            protected void onPostExecute(MovieDb movieDb){
                initView(movieDb);
            }

        }
    }
}