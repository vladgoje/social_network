package socialnetwork.repository.database;

import socialnetwork.config.ApplicationContext;
import socialnetwork.domain.Entity;
import socialnetwork.domain.Utilizator;
import socialnetwork.repository.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DbRepository<ID, E extends Entity<ID>> implements Repository<ID,E> {
    protected final String host = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.host");
    protected final String user = ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.user");
    protected final String password= ApplicationContext.getPROPERTIES().getProperty("database.socialnetwork.password");

    protected static Connection dbconn;
    protected ResultSet results;

    public boolean selectAction(String sql) {
        try {
            PreparedStatement statement = dbconn.prepareStatement(sql);
            this.results = statement.executeQuery();
            return true;
        } catch(SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public int updateAction(String sql){
        System.out.printf(sql);
        try {
            PreparedStatement statement = dbconn.prepareStatement(sql);
            return statement.executeUpdate();
        } catch(SQLException e){
            e.printStackTrace();
            return -1;
        }
    }

    public ResultSet getResults(){
        return this.results;
    }

    public abstract int count();

}
