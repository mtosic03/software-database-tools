package student;

import rs.ac.bg.etf.sab.operations.TagsOperations;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class tm220195_TagsOperations implements TagsOperations {
    private final Connection connection = DB.getInstance().getConnection();

    @Override
    public Integer addTag(Integer filmId, String tagName) {
        String checkFilm = "SELECT 1 FROM Film WHERE Id = ?";
        String checkExisting = "SELECT 1 FROM FilmTag f JOIN Tag t ON f.TagId = t.Id WHERE f.FilmId=? AND t.Naziv=?";
        String checkTag = "SELECT Id FROM Tag WHERE Naziv=?";
        String insertTag = "INSERT INTO Tag (Naziv) VALUES (?)";
        String insertFilmTag = "INSERT INTO FilmTag (FilmId, TagId) VALUES (?, ?)";

        try {
            // 1. Proveri da li film postoji
            try (PreparedStatement ps = connection.prepareStatement(checkFilm)) {
                ps.setInt(1, filmId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                }
            }

            // 2. Proveri da li veza (filmId, tag) već postoji
            try (PreparedStatement ps = connection.prepareStatement(checkExisting)) {
                ps.setInt(1, filmId);
                ps.setString(2, tagName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return null; // već postoji
                }
            }

            Integer tagId = null;
            // 3. Proveri da li tag postoji
            try (PreparedStatement ps = connection.prepareStatement(checkTag)) {
                ps.setString(1, tagName);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tagId = rs.getInt("Id");
                    }
                }
            }

            // 4. Ako tag ne postoji, ubaci ga
            if (tagId == null) {
                try (PreparedStatement ps = connection.prepareStatement(insertTag, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, tagName);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) tagId = rs.getInt(1);
                    }
                }
            }

            // 5. Ubaci u FilmTag
            try (PreparedStatement ps = connection.prepareStatement(insertFilmTag)) {
                ps.setInt(1, filmId);
                ps.setInt(2, tagId);
                ps.executeUpdate();
            }

            return filmId;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public Integer removeTag(Integer integer, String s) {
        String sqlDeleteFilmTag =
                "DELETE FROM FilmTag " +
                        "WHERE FilmId = ? AND TagId = (SELECT Id FROM Tag WHERE Naziv = ?)";

        try (PreparedStatement ps = connection.prepareStatement(sqlDeleteFilmTag)) {
            ps.setInt(1, integer);
            ps.setString(2, s);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                return integer;
            } else {
                return null;
            }

        } catch (SQLException e) {
            //            Logger.getLogger(UsersOperations.class.getName()).log(Level.SEVERE, null, e);
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int removeAllTagsForMovie(Integer integer) {
        String sqlDeleteAll = "DELETE FROM FilmTag WHERE FilmId = ?";

        try (PreparedStatement ps = connection.prepareStatement(sqlDeleteAll)) {
            ps.setInt(1, integer);
            int affected= ps.executeUpdate();
            return affected;
        } catch (SQLException e) {
           System.out.println("Greska removeallTags");
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean hasTag(Integer integer, String s) {
        String sql = "SELECT 1 FROM FilmTag f JOIN Tag t ON f.TagId = t.Id WHERE f.FilmId = ? AND t.Naziv = ?";
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setInt(1,integer);
            ps.setString(2,s);
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        }catch(SQLException e){
            System.out.println("Greska hasTag");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> getTagsForMovie(Integer integer) {
        String sql = "SELECT t.Naziv FROM FilmTag f JOIN Tag t ON f.TagId = t.Id WHERE f.FilmId = ?";
        List<String> tags = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, integer);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tags.add(rs.getString("Naziv"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Greska getTagsForMovie");
            e.printStackTrace();
        }

        return tags;
    }

    @Override
    public List<Integer> getMovieIdsByTag(String s) {
        String sql = "SELECT f.FilmId FROM FilmTag f JOIN Tag t ON f.TagId = t.Id WHERE t.Naziv = ?";
        List<Integer> movieIds = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    movieIds.add(rs.getInt("FilmId"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Greska getMovieIdsByTag");
            e.printStackTrace();
        }

        return movieIds;
    }

    @Override
    public List<String> getAllTags() {
        String sql = "SELECT Naziv FROM Tag";
        List<String> tags = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tags.add(rs.getString("Naziv"));
            }

        } catch (SQLException e) {
            System.out.println("Greska getAllTags");
            e.printStackTrace();
        }

        return tags;
    }
}
