package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 템플릿
 */
@Slf4j
public class MemberServiceV3_2 {

    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager); //밖에서 TransactionTemplate을 설정해서 주입받아서 사용해도 되지만 이런 형태로 사용하는 것은 관례로 굳어진 것도 있고 TransactionTemplate은 클래스이기 때문에 유연성이 없다.(주입 받는 타입이 유연하지 않음) 그렇기 때문에 트랜잭션 매니저를 주입받아서 내부에서 TransactionTemplate를 생성하는 방식으로 사용한다.
        this.memberRepository = memberRepository;
    }


    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        txTemplate.executeWithoutResult((status) -> {
            //코드 내에서 트랜잭션을 시작하고 종료하는 것을 관리한다.
            try {
                bizLogic(fromId, toId, money);
            } catch (SQLException e) { //체크 예외를 런타임 예외로 변환
                throw new IllegalStateException(e);
            }
            //정상적으로 종료시 commit, 아니면 rollback으로 자동으로 처리한다.
        });
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
