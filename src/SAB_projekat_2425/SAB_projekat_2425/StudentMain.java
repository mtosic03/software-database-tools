package SAB_projekat_2425.SAB_projekat_2425;

import rs.ac.bg.etf.sab.operations.*;
import rs.ac.bg.etf.sab.tests.TestHandler;
import rs.ac.bg.etf.sab.tests.TestRunner;
import student.*;

public class StudentMain {
    public static void main(String[] args) throws Exception {
// Uncomment and change fallowing lines
        GeneralOperations generalOperations = new tm220195_GeneralOperations();
        GenresOperations genresOperations = new tm220195_GenresOperations();
        MoviesOperations moviesOperations = new tm220195_MoviesOperations();
        RatingsOperations ratingsOperation = new tm220195_RatingsOperations();
        TagsOperations tagsOperations = new tm220195_TagsOperations();
        UsersOperations usersOperations = new tm220195_UsersOperations();
        WatchlistsOperations watchlistsOperations = new tm220195_WatchlistsOperations();

        TestHandler.createInstance(
                genresOperations,
                moviesOperations,
                ratingsOperation,
                tagsOperations,
                usersOperations,
                watchlistsOperations,
                generalOperations);
        TestRunner.runTests();
    }
}