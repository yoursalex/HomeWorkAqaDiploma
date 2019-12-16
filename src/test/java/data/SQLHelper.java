package data;
import java.sql.*;

import com.mysql.cj.xdevapi.Result;
import lombok.val;

public class SQLHelper {

    private static final String url = "jdbc:mysql://localhost:3306/db?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
   // private static final String url = "jdbc:postgresql://localhost:5432/db";
    private static final String user = "app";
    private static final String password = "pass";

    public static void cleanTables() throws SQLException {
        String deleteOrderEntity = "delete from order_entity;";
        String deletePaymentEntity = "delete from payment_entity;";
        String deleteCreditEntity = "delete from credit_request_entity;";

        try (Connection con = DriverManager.getConnection(url,user,password);
            PreparedStatement orderEntity = con.prepareStatement(deleteOrderEntity);
            PreparedStatement paymentEntity = con.prepareStatement(deletePaymentEntity);
            PreparedStatement creditEntity = con.prepareStatement(deleteCreditEntity);
        ) {
            orderEntity.executeUpdate();
            paymentEntity.executeUpdate();
            creditEntity.executeUpdate();
        }
    }

    public static String findPaymentStatus() throws SQLException{
        String stmt = "select * from payment_entity order by id desc limit 1;";
        String label= "status";
        return getStatus(stmt, label);
    }

    public static String findCreditStatus() throws SQLException{
        String stmt = "select distinct status from credit_request_entity order by id DESC limit 1;";
        String label = "status";
        return getStatus(stmt, label);
    }

    public static String findPaymentId() throws SQLException{
        String stmt = "select distinct payment_id from order_entity order by id desc limit 1;";
        String label = "payment_id";
        return getStatus(stmt, label);
    }

    public static String findCreditId() throws SQLException{
        String stmt = "select distinct credit_id from order_entity order by id desc limit 1;";
        String label = "credit_id";
        return getStatus(stmt, label);
    }

    public static boolean isNotEmpty() throws SQLException{
        String stmt = "select * from order_entity;";
        Connection connection = DriverManager.getConnection(url,user,password);
        PreparedStatement statement = connection.prepareStatement(stmt);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next();
    }


    public static String getStatus(String stmt, String label) throws SQLException {
        Connection connection = DriverManager.getConnection(url,user,password);
        PreparedStatement statement = connection.prepareStatement(stmt);
        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getNString(label);
    }

}
