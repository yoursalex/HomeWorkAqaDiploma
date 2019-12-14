package data;
import java.sql.*;

import com.mysql.cj.xdevapi.Result;
import lombok.val;


public class SQLHelper {

    private static final String url = "jdbc:mysql://localhost:3306/db?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
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

    public static String putStatement() {
        return "select distinct status from payment_entity order by id desc limit 1;";
    }

    public static String findStatus() throws SQLException {
        String status;

        try (Connection connection = DriverManager.getConnection(url,user, password);
             PreparedStatement statement = connection.prepareStatement(putStatement());
             ResultSet resultSet = statement.executeQuery())
        {
            status = resultSet.getNString("status");
            statement.execute();
        }

        return status;
    }

}
