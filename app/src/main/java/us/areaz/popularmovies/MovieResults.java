package us.areaz.popularmovies;

import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.core.MovieResultsPage;

public class MovieResults {
    private MovieResultsPage resultsPage;

    public void setResultsPage(MovieResults results){
        setResultsPage(results.resultsPage);
    }

    public void setResultsPage(MovieResultsPage resultsPage){
        if(resultsPage == null){
            return;
        }
        if(this.resultsPage == null || resultsPage.getPage()==1) {
            this.resultsPage = resultsPage;
            return;
        }
        if(resultsPage.getResults() != null){
            this.resultsPage.getResults().addAll(resultsPage.getResults());
            this.resultsPage.setPage(resultsPage.getPage());
            this.resultsPage.setTotalPages(resultsPage.getTotalPages());
            this.resultsPage.setTotalResults(resultsPage.getTotalResults());
        }
    }

    public int getResultCount() {
        return resultsPage==null?0:resultsPage.getResults().size();
    }

    public int getPage(){
        return resultsPage == null?0:resultsPage.getPage();
    }

    public boolean hasMorePages(){
        return resultsPage == null?false:resultsPage.getPage()<resultsPage.getTotalPages();
    }

    public MovieDb getMovie(int position) {
        if(position < getResultCount()){
            return resultsPage.getResults().get(position);
        }
        return null;
    }
}
