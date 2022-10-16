package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection conn = dataSource.getConnection();
        try {
            conn.setAutoCommit(false);//트랜잭션 시작

            bizLogic(conn, fromId, toId, money);

            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw new IllegalStateException(e);
        } finally {
            release(conn);

        }
    }

    //비즈니스 로직
    private void bizLogic(Connection conn, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(conn, fromId);
        Member toMember = memberRepository.findById(conn, toId);

        memberRepository.update(conn, fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(conn, toId, toMember.getMoney() + money);
    }

    private void release(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true); //트랜잭션 모드 기본설정 값으로 다시 재설정
                conn.close();
                //커넥션 풀을사용하고 있다면 커넥션 종료 시 커넥션 풀로 돌아간다.
                //문제는 setAutocommit 설정을 false로 하고 커넥션을 종료하는 경우, 해당 커넥션에 연결된 DB 세션 설정 값이 수동 트랜잭션 모드가 적용된 상태로 유지된다.
                //때문에 다음에 커넥션 풀에서 해당 커넥션을 얻어서 사용하는 유저가 기본 값인 자동 트랜잭션 모드가아닌 수동 트랜잭션 모드로 사용하는 경우가 발생한다.
                //문제가 없도록 트랜잭션 기본 설정 값(true)인 자동 트랜잭션 모드로 변경하고 종료시켜준다.
                //커넥션 풀을 사용하지않고 매번 커넥션을 생성하는 경우에는 세션을 바로 닫아주어도 무관하다.
            } catch (Exception e) {
                log.info("error", e);
            }
        }
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
