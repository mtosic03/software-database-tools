package student;

import rs.ac.bg.etf.sab.operations.MoviesOperations;
import rs.ac.bg.etf.sab.operations.UsersOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class tm220195_MoviesOperations implements MoviesOperations {
    private final Connection connection = DB.getInstance().getConnection();

    @Override
    public Integer addMovie(String title, Integer genreId, String director) {
        String insertFilm = "INSERT INTO Film (Naslov, Reziser) VALUES (?, ?)";
        String insertFilmGenre = "INSERT INTO FilmGenre (FilmId, GenreId) VALUES (?, ?)";
        String checkGenre = "SELECT 1 FROM Genre WHERE Id = ?";
        String checkFilmGenre = "SELECT 1 FROM FilmGenre WHERE FilmId=? AND GenreId=?";

        try {
            connection.setAutoCommit(false);

            // Provera da li genreId postoji
            try (PreparedStatement psCheck = connection.prepareStatement(checkGenre)) {
                psCheck.setInt(1, genreId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) {
                        connection.rollback();
                        return null;
                    }
                }
            }

            int filmId;
            // Ubacivanje filma
            try (PreparedStatement psFilm = connection.prepareStatement(insertFilm, Statement.RETURN_GENERATED_KEYS)) {
                psFilm.setString(1, title);
                psFilm.setString(2, director);
                psFilm.executeUpdate();

                try (ResultSet rs = psFilm.getGeneratedKeys()) {
                    if (rs.next()) {
                        filmId = rs.getInt(1);
                    } else {
                        connection.rollback();
                        return null;
                    }
                }
            }

            // Provera da li je veza Film-Genre već postoji
            try (PreparedStatement psCheckFG = connection.prepareStatement(checkFilmGenre)) {
                psCheckFG.setInt(1, filmId);
                psCheckFG.setInt(2, genreId);
                try (ResultSet rs = psCheckFG.executeQuery()) {
                    if (!rs.next()) { // Ako ne postoji, dodaj
                        try (PreparedStatement psFG = connection.prepareStatement(insertFilmGenre)) {
                            psFG.setInt(1, filmId);
                            psFG.setInt(2, genreId);
                            psFG.executeUpdate();
                        }
                    }
                }
            }

            connection.commit();
            return filmId;

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ignored) {}
            return null;
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }




    @Override
    public Integer updateMovieTitle(Integer integer, String s) {
        String sql = "UPDATE [dbo].[Film] SET Naslov=? WHERE Id=?";
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setString(1, s);
            ps.setInt(2, integer);
            int affected=ps.executeUpdate();
            return affected>0?integer:null;

        } catch (SQLException e) {
            System.out.println("Greska updateMovieTitle");
            return null;
        }
    }

    @Override
    public Integer addGenreToMovie(Integer integer, Integer integer1) {
        String checkMovie = "SELECT 1 FROM Film WHERE Id=?";
        String checkGenre = "SELECT 1 FROM Genre WHERE Id=?";
        String checkLink = "SELECT 1 FROM FilmGenre WHERE FilmId=? AND GenreId=?";
        String insertLink = "INSERT INTO FilmGenre (FilmId, GenreId) VALUES (?, ?)";

        try {
            // Provera postojanja filma
            try (PreparedStatement ps = connection.prepareStatement(checkMovie)) {
                ps.setInt(1, integer);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                }
            }

            // Provera postojanja žanra
            try (PreparedStatement ps = connection.prepareStatement(checkGenre)) {
                ps.setInt(1, integer1);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                }
            }

            // Provera da li je veza već postoji
            try (PreparedStatement ps = connection.prepareStatement(checkLink)) {
                ps.setInt(1, integer);
                ps.setInt(2, integer1);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return null; // već postoji
                }
            }

            // Ubacivanje veze
            try (PreparedStatement ps = connection.prepareStatement(insertLink)) {
                ps.setInt(1, integer);
                ps.setInt(2, integer1);
                int affected = ps.executeUpdate();
                return affected > 0 ? integer : null;
            }

        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public Integer removeGenreFromMovie(Integer integer, Integer integer1) {
        String sql="Delete from [dbo].[FilmGenre] where FilmId=? and GenreId=?";
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setInt(1,integer);
            ps.setInt(2,integer1);
            int affected = ps.executeUpdate();
            return affected>0?integer:null;
        } catch (SQLException e) {

            System.out.println("Greska removeGenreFromMovie");
            return null;
        }
    }

    @Override
    public Integer updateMovieDirector(Integer integer, String s) {
        String sql="Update [dbo].[Film] set Reziser=? where Id=?";
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setString(1, s);
            ps.setInt(2, integer);
            int affected = ps.executeUpdate();
            return affected>0?integer:null;

        }catch(SQLException e){
//            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Greska updateMovieDirector");
            return null;
        }
    }

    @Override
    public Integer removeMovie(Integer integer) {
        String sql="Delete from [dbo].[Film] where Id=?";
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setInt(1, integer);
            int affected = ps.executeUpdate();
            return affected>0?integer:null;
        }catch(SQLException e){
//            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Greska removeMovie");
            return null;
        }
    }

    @Override
    public List<Integer> getMovieIds(String s, String s1) {
        String sql = "SELECT Id FROM [dbo].[Film] where Naslov=? and Reziser=?";
        List<Integer> movIds = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);) {
            ps.setString(1, s);
            ps.setString(2, s1);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                movIds.add(rs.getInt("Id"));
            }
        } catch (SQLException e) {
//            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Greska getMovieIds");
            return new ArrayList<>();
        }
        return movIds;
    }

    @Override
    public List<Integer> getAllMovieIds() {
        String sql = "SELECT Id FROM [dbo].[Film]";
        List<Integer> movIds = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                movIds.add(rs.getInt("Id"));
            }
        } catch (SQLException e) {
//            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Greska getAllMovieIds");
            return new ArrayList<>();
        }
        return movIds;
    }

    @Override
    public List<Integer> getMovieIdsByGenre(Integer integer) {
        String sql = "SELECT FilmId FROM [dbo].[FilmGenre] where GenreId=?";
        List<Integer> movIds = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);) {
            ps.setInt(1, integer);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                movIds.add(rs.getInt("FilmId"));
            }
        } catch (SQLException e) {
//            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Greska getMovieIdsByGenre");
            return new ArrayList<>();
        }
        return movIds;
    }

    @Override
    public List<Integer> getGenreIdsForMovie(Integer integer) {
        String sql = "SELECT GenreId FROM [dbo].[FilmGenre] where FilmId=?";
        List<Integer> movIds = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);) {
            ps.setInt(1, integer);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                movIds.add(rs.getInt("GenreId"));
            }
        } catch (SQLException e) {
//            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Greska getGenreIdsForMovie");
            return new ArrayList<>();
        }
        return movIds;
    }

    @Override
    public List<Integer> getMovieIdsByDirector(String s) {
        String sql = "SELECT Id FROM [dbo].[Film] where Reziser=?";
        List<Integer> movIds = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);) {
            ps.setString(1, s);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                movIds.add(rs.getInt("Id"));
            }
        } catch (SQLException e) {
//            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, e);
            System.out.println("Greska getMovieIdsByDirector");
            return new ArrayList<>();
        }
        return movIds;
    }
}
