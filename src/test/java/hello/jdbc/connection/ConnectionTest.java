package hello.jdbc.connection;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class ConnectionTest {

    @Test
    void driverManager() throws SQLException {
        //커넥션 생성 시 마다 설정 파라미터 정보 필요. 설정과 사용이 분리안됨
        Connection conn1 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        Connection conn2 = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        log.info("connection={}, class={}", conn1, conn1.getClass());
        log.info("connection={}, class={}", conn2, conn2.getClass());
    }

    @Test
    void dataSourceDriverManager() throws SQLException {
        //DriverManagerDataSource - 항상 새로운 커넥션을 획득
        //초기에는 커넥션을 생성하지 않고 설정 정보를 설정하기 위해 파라미터 필요. 이후에 설정된 정보대로 커넥션 생성. 설정과 사용이 분리되어 있음.
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        useDataSource(dataSource);

    }

    @Test
    void dataSourceConnectionPool() throws SQLException, InterruptedException {
        //커넥션 풀링
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);
        dataSource.setMaximumPoolSize(10);
        dataSource.setPoolName("JdbcConnectionPool");

        useDataSource(dataSource);
        Thread.sleep(1000);
    }

    private void useDataSource(DataSource dataSource) throws SQLException {
        Connection conn1 = dataSource.getConnection();
        Connection conn2 = dataSource.getConnection();
        log.info("connection={}, class={}", conn1, conn1.getClass());
        log.info("connection={}, class={}", conn2, conn2.getClass());
    }
}
