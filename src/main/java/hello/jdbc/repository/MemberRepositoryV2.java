package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC - ConnectionParam
 */
@Slf4j
@RequiredArgsConstructor
public class MemberRepositoryV2 {

    private final DataSource dataSource;

    public Member save(Connection conn, Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?, ?)";
        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            JdbcUtils.closeStatement(pstmt);
//            JdbcUtils.closeConnection(conn); 커넥션을 닫으면 다른 SQL 호출 시 같은 DB 세션을 사용할 수 없으므로 비즈니스 로직이 원자적으로 묶이지 않는다. 비즈니스 로직이 끝난 후 서비스 계층에서 닫아주어야한다.
        }
    }

    public Member findById(Connection conn, String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next()) {//데이터 있음
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }else { //데이터 없음
                throw new NoSuchElementException("member not found memberId=" + memberId);
            }
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
//            JdbcUtils.closeConnection(conn); 커넥션을 닫으면 다른 SQL 호출 시 같은 DB 세션을 사용할 수 없으므로 비즈니스 로직이 원자적으로 묶이지 않는다. 비즈니스 로직이 끝난 후 서비스 계층에서 닫아주어야한다.
        }
    }

    public void update(Connection conn, String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            JdbcUtils.closeStatement(pstmt);
//            JdbcUtils.closeConnection(conn); 커넥션을 닫으면 다른 SQL 호출 시 같은 DB 세션을 사용할 수 없으므로 비즈니스 로직이 원자적으로 묶이지 않는다. 비즈니스 로직이 끝난 후 서비스 계층에서 닫아주어야한다.
        }
    }

    public void delete(Connection conn, String memberId) throws SQLException {
        String sql = "delete from member where member_id = ?";

        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            JdbcUtils.closeStatement(pstmt);
//            JdbcUtils.closeConnection(conn); 커넥션을 닫으면 다른 SQL 호출 시 같은 DB 세션을 사용할 수 없으므로 비즈니스 로직이 원자적으로 묶이지 않는다. 비즈니스 로직이 끝난 후 서비스 계층에서 닫아주어야한다.
        }

    }

    private void close(Connection conn, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(conn);
    }

    private Connection getConnection() throws SQLException {
        Connection conn = dataSource.getConnection();
        log.info("get connection={}, class={}", conn, conn.getClass());
        return conn;
    }
}
