package student;

import rs.ac.bg.etf.sab.operations.RatingsOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class tm220195_RatingsOperations implements RatingsOperations {
    private final Connection connection = DB.getInstance().getConnection();

    @Override
    public boolean addRating(Integer userId, Integer movieId, Integer score) {
        String checkUser = "SELECT 1 FROM Korisnik WHERE Id = ?";
        String checkMovie = "SELECT 1 FROM Film WHERE Id = ?";
        String checkExisting = "SELECT 1 FROM Rating WHERE UserId = ? AND FilmId = ?";
        String insertRating = "INSERT INTO Rating (UserId, FilmId, Rating) VALUES (?, ?, ?)";
        String verifyInsert = "SELECT 1 FROM Rating WHERE UserId = ? AND FilmId = ?";

        try {
            // Proveri da li korisnik postoji
            try (PreparedStatement ps = connection.prepareStatement(checkUser)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;
                }
            }

            // Proveri da li film postoji
            try (PreparedStatement ps = connection.prepareStatement(checkMovie)) {
                ps.setInt(1, movieId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return false;
                }
            }

            // Proveri da li ocena već postoji
            try (PreparedStatement ps = connection.prepareStatement(checkExisting)) {
                ps.setInt(1, userId);
                ps.setInt(2, movieId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return false;
                }
            }

            // Pokušaj da ubacis ocenu (trigger može da blokira)
            try (PreparedStatement ps = connection.prepareStatement(insertRating)) {
                ps.setInt(1, userId);
                ps.setInt(2, movieId);
                ps.setInt(3, score);
                ps.executeUpdate();
            }

            // Proveri da li je trigger stvarno ubacio red
            try (PreparedStatement ps = connection.prepareStatement(verifyInsert)) {
                ps.setInt(1, userId);
                ps.setInt(2, movieId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next(); // true ako je ubačeno, false ako je trigger blokirao
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateRating(Integer userId, Integer movieId, Integer newScore) {
        String checkExisting = "SELECT Rating FROM Rating WHERE UserId = ? AND FilmId = ?";
        String updateSql = "UPDATE Rating SET Rating = ? WHERE UserId = ? AND FilmId = ?";

        try {
            Integer oldRating = null;

            // Proveri da li ocena postoji i uzmi staru vrednost
            try (PreparedStatement psCheck = connection.prepareStatement(checkExisting)) {
                psCheck.setInt(1, userId);
                psCheck.setInt(2, movieId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (!rs.next()) return false;
                    oldRating = rs.getInt("Rating");
                }
            }

            // Pokušaj da ažuriraš (trigger može da blokira)
            try (PreparedStatement psUpdate = connection.prepareStatement(updateSql)) {
                psUpdate.setInt(1, newScore);
                psUpdate.setInt(2, userId);
                psUpdate.setInt(3, movieId);
                psUpdate.executeUpdate();
            }

            // Proveri da li je trigger stvarno promenio vrednost
            try (PreparedStatement psCheck = connection.prepareStatement(checkExisting)) {
                psCheck.setInt(1, userId);
                psCheck.setInt(2, movieId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        Integer currentRating = rs.getInt("Rating");
                        // Ako se vrednost promenila, update je uspeo
                        return !currentRating.equals(oldRating);
                    }
                }
            }

            return false;

        } catch (SQLException e) {
            System.out.println("Greska updateRating");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean removeRating(Integer userId, Integer movieId) {
        String sql = "DELETE FROM Rating WHERE UserId = ? AND FilmId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);
            int affected = ps.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            System.out.println("Greska removeRating");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Integer getRating(Integer userId, Integer movieId) {
        String sql = "SELECT Rating FROM Rating WHERE UserId = ? AND FilmId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, movieId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Rating");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Greska getRating");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Integer> getRatedMoviesByUser(Integer userId) {
        String sql = "SELECT FilmId FROM Rating WHERE UserId = ?";
        List<Integer> movieIds = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movieIds.add(rs.getInt("FilmId"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Greska getRatedMoviesByUser");
            e.printStackTrace();
            return new ArrayList<>();
        }

        return movieIds;
    }

    @Override
    public List<Integer> getUsersWhoRatedMovie(Integer movieId) {
        String sql = "SELECT UserId FROM Rating WHERE FilmId = ?";
        List<Integer> userIds = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, movieId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    userIds.add(rs.getInt("UserId"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Greska getUsersWhoRatedMovie");
            e.printStackTrace();
            return new ArrayList<>();
        }

        return userIds;
    }
}