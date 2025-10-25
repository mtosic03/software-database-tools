package student;

import rs.ac.bg.etf.sab.operations.GenresOperations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;


public class tm220195_GenresOperations implements GenresOperations {
    private final Connection connection = DB.getInstance().getConnection();

    @Override
    public Integer addGenre(String s) {
        String selectSql = "SELECT Id FROM Genre WHERE Naziv = ?";
        String insertSql = "INSERT INTO Genre (Naziv) VALUES (?)";

        try {
            try (PreparedStatement ps1 = connection.prepareStatement(selectSql)) {
                ps1.setString(1, s);
                try (ResultSet rs = ps1.executeQuery()) {
                    if (rs.next()) {
                        return null;
                    }
                }
            }
            try (PreparedStatement ps2 = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps2.setString(1, s);
                ps2.executeUpdate();

                try (ResultSet rs = ps2.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1); // novi Id
                    } else {
                        return null;
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Greska addGenre");
            return null;
        }
    }

    @Override
    public Integer updateGenre(Integer integer, String s) {
        String sql="Update Genre set Naziv=? where Id=?";
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setString(1,s);
            ps.setInt(2,integer);
            int affected=ps.executeUpdate();
            return affected>0?integer:null;
        } catch (SQLException e) {
            System.out.println("Greska updateGenre");
            return null;
        }
    }

    @Override
    public Integer removeGenre(Integer integer) {
        String sql="Delete from Genre where Id=?";
        try(PreparedStatement ps=connection.prepareStatement(sql)){
            ps.setInt(1,integer);
            int affected=ps.executeUpdate();
            return affected>0?integer:null;
        } catch (SQLException e) {
            System.out.println("Greska updateGenre");
            return null;
        }
    }

    @Override
    public boolean doesGenreExist(String s) {
        String sql = "SELECT 1 FROM Genre WHERE Naziv = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // ako postoji bar jedan red → true
            }
        } catch (SQLException e) {
            System.out.println("Greska doesGenreExist");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Integer getGenreId(String s) {
        String sql = "SELECT Id FROM Genre WHERE Naziv = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, s);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Id");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            System.out.println("Greska getGenreId");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Integer> getAllGenreIds() {
        String sql = "SELECT Id FROM Genre";
        List<Integer> genreIds = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                genreIds.add(rs.getInt("Id"));
            }

        } catch (SQLException e) {
            System.out.println("Greska getAllGenreIds");
            e.printStackTrace();
        }

        return genreIds;
    }
}
