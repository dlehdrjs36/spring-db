package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        //트랜잭션 시작. 트랜잭션이 종료되면 커넥션 반환을 알아서 해주기 때문에 커넥션을 별도로 반환해주는 작업이 필요가 없다.
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition()); //DefaultTransactionDefinition : 트랜잭션과 관련된 옵션을 지정할 수 있다.

        try {
            bizLogic(fromId, toId, money);
            transactionManager.commit(status); //성공 시 커밋
        } catch (Exception e) {
            transactionManager.rollback(status);//실패 시 롤백
            throw new IllegalStateException(e);
        }
    }

    //비즈니스 로직
    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId, toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
