package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 - 커넥션 파라미터 전달
 */
@Slf4j
class MemberServiceV2Test {

    public static final String MEMBER_A = "memberA";
    public static final String MEMBER_B = "memberB";
    public static final String MEMBER_EX = "ex";

    private MemberRepositoryV2 memberRepository;
    private MemberServiceV2 memberServiceV1;
    private Connection conn;

    @BeforeEach
    void before() throws SQLException {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        conn = dataSource.getConnection();
        memberRepository = new MemberRepositoryV2(dataSource);
        memberServiceV1 = new MemberServiceV2(dataSource, memberRepository);
    }

    @AfterEach
    void after() throws SQLException {
        memberRepository.delete(conn, MEMBER_A);
        memberRepository.delete(conn, MEMBER_B);
        memberRepository.delete(conn, MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        memberRepository.save(conn, memberA);
        memberRepository.save(conn, memberB);

        //when
        log.info("START TX"); //내부에서 커넥션을 생성하고 이 커넥션을 내부에서 파라미터로 전달해가며 사용한다. 내부에서는 모두 동일한 커넥션을 사용하고 있다.
        memberServiceV1.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        log.info("END TX");

        //then
        Member findMemberA = memberRepository.findById(conn, memberA.getMemberId());
        Member findMemberB = memberRepository.findById(conn, memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체 중 예외 발생")
    void accountTransferEx() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);
        memberRepository.save(conn, memberA);
        memberRepository.save(conn, memberEx);

        //when
        assertThatThrownBy(() -> memberServiceV1.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = memberRepository.findById(conn, memberA.getMemberId());
        Member findMemberB = memberRepository.findById(conn, memberEx.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(10000);// 트랜잭션 미사용(v1)버전에서는 비즈니스로직이 원자적으로 rollback이 안되어 8000 상태
        assertThat(findMemberB.getMoney()).isEqualTo(10000);
    }
}