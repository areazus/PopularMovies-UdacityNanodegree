package us.areaz.popularmovies;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.squareup.picasso.Picasso;

import java.util.List;

import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.TmdbReviews;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.Reviews;
import info.movito.themoviedbapi.model.Video;

import static us.areaz.popularmovies.MovieDataAdapter.Movie_Backdrop_Path;
import static us.areaz.popularmovies.MovieDataAdapter.Movie_ID;
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
        private RelativeLayout relativeLayout;

        private FavoriteMoviesSqlHelper sqlHelper;
        private String MovieId;
        private TmdbReviews.ReviewResultsPage reviewResultsPage;
        private List<Video> videos;
        private FetchMovie fetchMovie;
        private LayoutInflater inflater;
        private Intent intent;

        public MovieDetailActivityFragment() {

        }

        public void setIntent(Intent intent){
            this.intent = intent;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            this.inflater = inflater;
            rootView = (ScrollView) inflater.inflate(R.layout.fragment_detail, container, false);
            relativeLayout = (RelativeLayout) rootView.findViewById(R.id.detail_relative_layout);
            Intent intent = this.intent==null?getActivity().getIntent():this.intent;
            if(intent == null || intent.getStringExtra(Movie_ID) == null){
                return null;
            }

            if(sqlHelper == null){
                sqlHelper = new FavoriteMoviesSqlHelper(getActivity());
            }
            MovieId = intent.getStringExtra(Movie_ID);
            if(fetchMovie == null) {
                fetchMovie = new FetchMovie();
                fetchMovie.execute(getString(R.string.tmdb_api_key), MovieId);
            }
            boolean isFavorite = sqlHelper.isFavorite(MovieId);

            String imageURL = getString(R.string.image_fetch_url_backdrop)+intent.getStringExtra(Movie_Backdrop_Path);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.detailed_movie_backdrop_view);
            Picasso.with(inflater.getContext()).load(imageURL).placeholder(R.drawable.placeholder_backdrop)
                    .error(R.drawable.placeholder_backdrop).fit().centerCrop().into(imageView);

            imageURL = getString(R.string.image_fetch_url_poster)+intent.getStringExtra(Movie_Poster_Path);
            Picasso.with(inflater.getContext()).load(imageURL).placeholder(R.drawable.placeholder_poster)
                    .error(R.drawable.placeholder_poster).into((ImageView) rootView.findViewById(R.id.detailed_movie_poster_view));


            String message = intent.getStringExtra(Movie_Title);
            TextView textView = (TextView)rootView.findViewById(R.id.detailed_movie_title);
            textView.setText(message);

            message = intent.getStringExtra(Movie_Vote_Average);
            textView = (TextView)rootView.findViewById(R.id.detailed_movie_rating);
            textView.setText(message+" / 10");

            message = intent.getStringExtra(Movie_Release_Date);
            textView = (TextView)rootView.findViewById(R.id.detailed_movie_release_date);
            textView.setText(message);

            MaterialFavoriteButton favButton = (MaterialFavoriteButton) rootView.
                    findViewById(R.id.detailed_favorite_button);
            favButton.setFavorite(isFavorite);
            favButton.setOnFavoriteChangeListener(
                    new MaterialFavoriteButton.OnFavoriteChangeListener() {
                        @Override
                        public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                            if(favorite){
                                buttonView.setAnimateFavorite(true);
                                sqlHelper.add(MovieId);
                            }else if(!favorite){
                                buttonView.setAnimateUnfavorite(true);
                                sqlHelper.delete(MovieId);
                            }
                        }
                    });

            message = intent.getStringExtra(Movie_Overview);
            textView = (TextView)rootView.findViewById(R.id.detailed_movie_overview);
            textView.setText(message);


            return rootView;
        }

        public void initView() {
            if(videos != null && videos.size() > 0){
                addTrailersToView();
            }
            if(reviewResultsPage != null && reviewResultsPage.getResults().size() > 0){
                addReviewsToView();
            }

        }

        private void addTrailersToView() {
            TextView view = (TextView)relativeLayout.findViewById(R.id.detailed_movie_trailer_title);
            view.setText("TRAILERS");
            LinearLayout listView = (LinearLayout)relativeLayout.findViewById(R.id.detailed_movie_trailer_list);
            for(int i=0; i<videos.size(); i++){
                final Video v = videos.get(i);
                RelativeLayout relativeLayout = (RelativeLayout)inflater.inflate(R.layout.movie_trailer_view, null);
                TextView textView = (TextView) relativeLayout.findViewById(R.id.movie_trailer_description);
                textView.setText(v.getName());
                listView.addView(relativeLayout);
                relativeLayout.setOnClickListener(new RelativeLayout.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        try{
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + v.getKey()));
                            startActivity(intent);
                        }catch (ActivityNotFoundException ex) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://www.youtube.com/watch?v=" + v.getKey()));
                            startActivity(intent);
                        }
                    }
                });
            }
        }

        private void addReviewsToView() {
            TextView view = (TextView)relativeLayout.findViewById(R.id.detailed_movie_reviews_title);
            view.setText("Reviews");
            LinearLayout listView = (LinearLayout)relativeLayout.findViewById(R.id.detailed_movie_reviews_list);
            for(final Reviews r : reviewResultsPage.getResults()){
                RelativeLayout relativeLayout = (RelativeLayout)inflater.inflate(R.layout.movie_review_view, null);
                TextView textView = (TextView) relativeLayout.findViewById(R.id.movie_review_Content);
                textView.setText("\""+r.getContent()+"\"");
                textView.setOnClickListener(new TextView.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(r.getUrl()));
                        startActivity(i);
                    }
                });

                textView = (TextView) relativeLayout.findViewById(R.id.movie_review_Author);
                textView.setText(Character.toUpperCase(r.getAuthor().charAt(0)) + r.getAuthor().substring(1));
                listView.addView(relativeLayout);
            }



        }

        public class FetchMovie  extends AsyncTask<String, Void, Boolean> {
            //ToBeUsedLater: for trailed and any other network activity
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    TmdbApi api = new TmdbApi(params[0]);
                    TmdbMovies movies = api.getMovies();
                    MovieDb movie = movies.getMovie(Integer.parseInt(params[1]), "en");
                    reviewResultsPage = api.getReviews().getReviews(Integer.parseInt(params[1]), "en", 1);
                    videos = movies.getVideos(Integer.parseInt(params[1]), "en");
                    return true;
                }catch (Exception e){
                    Log.e(this.getClass().getName(), e.getMessage(), e);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean changed){
                if(changed)
                    initView();
            }

        }
    }
}