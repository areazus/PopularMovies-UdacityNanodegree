package us.areaz.popularmovies;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import info.movito.themoviedbapi.model.MovieDb;
import us.areaz.popularmovies.MainActivity.MainActivityFragment;

public class MovieDataAdapter extends RecyclerView.Adapter<MovieDataAdapter.ImageHolder> {
    private MovieResults results = new MovieResults();
    private ViewGroup parent;
    private String url;

    public static final String Movie_ID = "mID";
    public static final String Movie_Backdrop_Path = "mbackdropPath";
    public static final String Movie_Overview = "mOverview";
    public static final String Movie_Poster_Path = "mPosterPath";
    public static final String Movie_Release_Date = "mReleaseDate";
    public static final String Movie_Title = "mTitle";
    public static final String Movie_Vote_Average = "mVoteAverage";
    public static final String Movie_Vote_Count = "mVoteCount";

    public void clearResults(){
        if(this.results != null){
            this.results.clearAll();
        }
    }

    public void setResults(MovieResults results){
        if(results == null){
            return;
        }
        if(this.results.getResultCount() == 0){
            this.results = results;
        }else{
            this.results.setResultsPage(results);
        }
    }

    @Override
    public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.parent = parent;
        url=parent.getContext().getString(R.string.image_fetch_url_poster);
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pic_item_view, parent, false);
        ImageHolder vh = new ImageHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(ImageHolder holder, int position) {
        //Recycling of view
        MovieDb movie = results.getMovie(position);
        if(movie != null){
            holder.setMovie(movie);
            String imageURL = url+movie.getPosterPath();
            Picasso.with(parent.getContext()).load(imageURL).placeholder(R.drawable.placeholder_poster)
                    .error(R.drawable.placeholder_poster).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return this.results.getResultCount();
    }

    public MovieResults getResults(){
        return  this.results;
    }

    public static class ImageHolder extends RecyclerView.ViewHolder  implements View.OnClickListener {
        public ImageView imageView; //To Hold the image
        public MovieDb movie;   //To Hold Movie

        public ImageHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            imageView = (ImageView) v.findViewById(R.id.movie_image_view);
        }

        public void setMovie(MovieDb movie){
            this.movie = movie;
        }

        @Override
        public void onClick(View v) {
            MainActivityFragment mainActivityFragment = (MainActivityFragment)((MainActivity)v.getContext())
                    .getFragmentManager()
                    .findFragmentById(R.id.fragment_movies);
            mainActivityFragment.showDetails(movie);
        }
    }

}
