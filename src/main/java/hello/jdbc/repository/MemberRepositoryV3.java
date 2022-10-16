package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 * DataSourceUtils.getConnection()
 * DataSourceUtils.releaseConnection()
 */
@Slf4j
@RequiredArgsConstructor
public class MemberRepositoryV3 {

    private final DataSource dataSource;

    public Member save(Member member) throws SQLException {
        String sql = "insert into member(member_id, money) values (?, ?)";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(conn, pstmt, null);
        }
    }

    public Member findById(String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
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
            close(conn, pstmt, rs);
        }
    }

    public void update(String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(conn, pstmt, null);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(conn, pstmt, null);
        }

    }

    private void close(Connection conn, Statement stmt, ResultSet rs) {

        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        //트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다. 커넥션을 종료하지 않고 트랜잭션 동기화 매니저에 보관하도록 한다.
        //커넥션은 트랜잭션 종료(커밋, 롤백)할 때 까지 살아있어야 한다.
        //DataSourceUtils.releaseConnection는 트랜잭션을 사용하기 위해 동기화된 커넥션은 닫지 않고 그대로 유지한다.
        //만약 서비스 계층에서 트랜잭션으로 묶지 않으면 트랜잭션 동기화 매니저가 관리하는 커넥션이 없을 것이다. 이러한 경우에는 해당 커넥션을 닫는다.
        DataSourceUtils.releaseConnection(conn, dataSource);
//        JdbcUtils.closeConnection(conn); 커넥션을 종료
    }

    private Connection getConnection() throws SQLException {
        //트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        //DataSourceUtils.getConnection는 트랜잭션 동기화 매니저가 관리하는 커넥션이 있으면 해당 커넥션을 반환한다.
        //만약 서비스 계층에서 트랜잭션으로 묶지 않으면 트랜잭션 동기화 매니저에서 관리하는 커넥션이 없을 것이다. 이러한 경우 새로운 커넥션을 생성해서 반환한다.
        Connection conn = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class={}", conn, conn.getClass());
        return conn;
    }
}
