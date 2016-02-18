package us.areaz.popularmovies;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import info.movito.themoviedbapi.model.MovieDb;

/**
 * Created by ahmed on 2/15/2016.
 */
public class MovieSorter {
    static final Comparator<MovieDb> PopularityOrder =
            new Comparator<MovieDb>() {
                @Override
                public int compare(MovieDb a, MovieDb b) {
                    if(a.getPopularity()<b.getPopularity()){
                        return 1;
                    }else if(a.getPopularity()==b.getPopularity()){
                        return 0;
                    }
                    return -1;
                }
            };

    static final Comparator<MovieDb> RatingOrder =
            new Comparator<MovieDb>() {
                @Override
                public int compare(MovieDb a, MovieDb b) {
                    if(a.getVoteAverage()<b.getVoteAverage()){
                        return 1;
                    }else if(a.getVoteAverage()==b.getVoteAverage()){
                        return 0;
                    }
                    return -1;
                }
            };

    public static void sortByPopularity(List<MovieDb> list){
        Collections.sort(list, PopularityOrder);
    }

    public static void sortByRating(List<MovieDb> list){
        Collections.sort(list, RatingOrder);
    }



}
