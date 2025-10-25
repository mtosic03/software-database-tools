package student;

import rs.ac.bg.etf.sab.operations.GeneralOperations;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;

public class tm220195_GeneralOperations implements GeneralOperations {
    private final Connection connection = DB.getInstance().getConnection();

    @Override
    public void eraseAll() {
        String eraseAllQuery = "{ call eraseAll() }; ";

        try(CallableStatement cs = connection.prepareCall(eraseAllQuery)) {

            cs.execute();

        } catch (SQLException ex) {
            Logger.getLogger(tm220195_GeneralOperations.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
