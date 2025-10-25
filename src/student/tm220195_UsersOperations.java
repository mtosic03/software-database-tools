package student;

import rs.ac.bg.etf.sab.operations.UsersOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class tm220195_UsersOperations implements UsersOperations {
    private final Connection connection = DB.getInstance().getConnection();

    @Override
    public Integer addUser(String s) {
        String sql = "INSERT INTO [dbo].[Korisnik] (Username) VALUES (?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s);
            int affected = ps.executeUpdate();
            if (affected == 0) return null;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public Integer updateUser(Integer integer, String s) {
        String sql = "UPDATE [dbo].[Korisnik] SET Username = ? WHERE Id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s);
            ps.setInt(2, integer);
            int affected = ps.executeUpdate();
            return affected > 0 ? integer : null;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public Integer removeUser(Integer integer) {
        String sql = "DELETE FROM [dbo].[Korisnik] WHERE Id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, integer);
            int affected = ps.executeUpdate();
            return affected > 0 ? integer : null;
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public boolean doesUserExist(String s) {
        String sql = "SELECT 1 FROM [dbo].[Korisnik] WHERE Username = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            return false;
        }
    }

    @Override
    public Integer getUserId(String s) {
        String sql = "SELECT Id FROM [dbo].[Korisnik] WHERE Username = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("Id") : null;
            }
        } catch (SQLException ex) {
            return null;
        }
    }

    @Override
    public List<Integer> getAllUserIds() {
        String sql = "SELECT Id FROM [dbo].[Korisnik]";
        List<Integer> userIds = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                userIds.add(rs.getInt("Id"));
            }
        } catch (SQLException ex) {
            return null;
        }
        return userIds;
    }

    @Override
    public List<Integer> getRecommendedMoviesFromFavoriteGenres(Integer userId) {
        String sql =
                "WITH FavoriteGenres AS ( " +
                        "    SELECT fg.GenreId " +
                        "    FROM Rating r " +
                        "    JOIN FilmGenre fg ON fg.FilmId = r.FilmId " +
                        "    WHERE r.UserId = ? " +
                        "    GROUP BY fg.GenreId " +
                        "    HAVING AVG(CAST(r.Rating AS DECIMAL(10,3))) >= 8 " +
                        "), " +
                        "MovieStats AS ( " +
                        "    SELECT f.Id, COUNT(r.Rating) AS RatingCount, " +
                        "           AVG(CAST(r.Rating AS DECIMAL(10,3))) AS AvgRating " +
                        "    FROM Film f " +
                        "    LEFT JOIN Rating r ON r.FilmId = f.Id " +
                        "    GROUP BY f.Id " +
                        ") " +
                        "SELECT ms.Id, ms.AvgRating " +
                        "FROM MovieStats ms " +
                        "WHERE EXISTS ( " +
                        "    SELECT 1 FROM FilmGenre fg " +
                        "    JOIN FavoriteGenres fav ON fav.GenreId = fg.GenreId " +
                        "    WHERE fg.FilmId = ms.Id " +
                        ") " +
                        "  AND ms.Id NOT IN (SELECT FilmId FROM Rating WHERE UserId = ?) " +
                        "  AND ms.Id NOT IN (SELECT FilmId FROM Watchlist WHERE UserId = ?) " +
                        "  AND ( " +
                        "      (ms.RatingCount >= 4 AND ms.AvgRating >= 7.5) " +
                        "      OR (ms.RatingCount < 4 AND ms.RatingCount > 0 AND ms.AvgRating >= 9) " +
                        "  ) " +
                        "ORDER BY ms.AvgRating DESC, ms.Id ASC";

        List<Integer> movieIds = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movieIds.add(rs.getInt("Id"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return movieIds;
    }

    @Override
    public Integer getRewards(Integer userId) {
        String sql = "SELECT BrojNagrada FROM Korisnik WHERE Id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("BrojNagrada");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    @Override
    public List<String> getThematicSpecializations(Integer userId) {
        String sql =
                "SELECT t.Naziv, COUNT(*) AS TagCount " +
                        "FROM Rating r " +
                        "JOIN FilmTag ft ON ft.FilmId = r.FilmId " +
                        "JOIN Tag t ON t.Id = ft.TagId " +
                        "WHERE r.UserId = ? AND r.Rating >= 8 " +
                        "GROUP BY t.Naziv " +
                        "HAVING COUNT(*) >= 2 " +
                        "ORDER BY t.Naziv";

        List<String> specializations = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    specializations.add(rs.getString("Naziv"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return specializations;
    }

    @Override
    public String getUserDescription(Integer userId) {
        String countSql = "SELECT COUNT(*) AS RatedCount FROM Rating WHERE UserId = ?";
        String tagSql =
                "SELECT COUNT(DISTINCT t.Id) AS UniqueTagCount " +
                        "FROM Rating r " +
                        "JOIN FilmTag ft ON ft.FilmId = r.FilmId " +
                        "JOIN Tag t ON t.Id = ft.TagId " +
                        "WHERE r.UserId = ?";

        try {
            int ratedCount = 0;
            try (PreparedStatement ps = connection.prepareStatement(countSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ratedCount = rs.getInt("RatedCount");
                    }
                }
            }

            if (ratedCount < 10) {
                return "undefined";
            }

            int uniqueTagCount = 0;
            try (PreparedStatement ps = connection.prepareStatement(tagSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        uniqueTagCount = rs.getInt("UniqueTagCount");
                    }
                }
            }

            if (uniqueTagCount >= 10) {
                return "curious";
            } else {
                return "focused";
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            return "";
        }
    }
}