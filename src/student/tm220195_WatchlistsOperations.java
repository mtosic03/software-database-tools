package student;

import rs.ac.bg.etf.sab.operations.WatchlistsOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class tm220195_WatchlistsOperations implements WatchlistsOperations {
    private final Connection connection = DB.getInstance().getConnection();
    @Override
    public boolean addMovieToWatchlist(Integer integer, Integer integer1) {
        String checkSql = "SELECT 1 FROM Watchlist WHERE UserId = ? AND FilmId = ?";
        String insertSql = "INSERT INTO Watchlist (UserId, FilmId) VALUES (?, ?)";
        try {
            try (PreparedStatement psCheck = connection.prepareStatement(checkSql)) {
                psCheck.setInt(1, integer);
                psCheck.setInt(2, integer1);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        return false;
                    }
                }
            }
            try (PreparedStatement psInsert = connection.prepareStatement(insertSql)) {
                psInsert.setInt(1, integer);
                psInsert.setInt(2, integer1);
                int affected = psInsert.executeUpdate();
                return affected > 0;
            }
        } catch (SQLException e) {
            System.out.println("Greska addMovieToWatchlist");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeMovieFromWatchlist(Integer integer, Integer integer1) {
        String sql = "DELETE FROM Watchlist WHERE UserId = ? AND FilmId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, integer);
            ps.setInt(2, integer1);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.out.println("Greska removeMovieFromWatchlist");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isMovieInWatchlist(Integer integer, Integer integer1) {
        String sql = "SELECT 1 FROM Watchlist WHERE UserId = ? AND FilmId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, integer);
            ps.setInt(2, integer1);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Greska isMovieInWatchlist");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Integer> getMoviesInWatchlist(Integer integer) {
        String sql = "SELECT FilmId FROM Watchlist WHERE UserId = ?";
        List<Integer> movieIds = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, integer);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movieIds.add(rs.getInt("FilmId"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Greska getMoviesInWatchlist");
            e.printStackTrace();
        }

        return movieIds;
    }

    @Override
    public List<Integer> getUsersWithMovieInWatchlist(Integer integer) {
        String sql = "SELECT UserId FROM Watchlist WHERE FilmId = ?";
        List<Integer> userIds = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, integer);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getInt("UserId"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Greska getUsersWithMovieInWatchlist");
            e.printStackTrace();
        }

        return userIds;
    }
}
