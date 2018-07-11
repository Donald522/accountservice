package service

import dao.AccountStorage
import dao.AccountStorageImpl
import model.Account
import model.Transaction
import spock.lang.Specification

import java.util.function.BiFunction

/**
 * Created by Anton Tolkachev.
 * Since 11.07.2018
 */
class AccountServiceImplSpec extends Specification {

    def "Invokes data storage api with correct arguments for account creating"() {
        given: "Name of account holder"
        def name = "Anton"

        and: "service for account creation"
        def storage = Mock(AccountStorage)
        def service = new AccountServiceImpl(storage)

        when:
        service.createAccount(name)

        then:
        interaction {
            1 * storage.execute(*_) >> { arguments ->
                def account = (Account) arguments[1].get(0)
                assert name == account.name
            }
            1 * storage.commit()
        }
    }

    def "Invokes data storage api with correct arguments for account ids loading"() {
        given: "service for account loading"
        def storage = Mock(AccountStorage)
        def service = new AccountServiceImpl(storage)

        when:
        service.getAllAccountIds()

        then:
        interaction {
            1 * storage.query(!null as BiFunction, null)
        }
    }

    def "Invokes data storage api with correct arguments for account loading by id"() {
        given: "Account id"
        def id = "account-id-12345"

        and: "service for account creation"
        def storage = Mock(AccountStorage)
        def service = new AccountServiceImpl(storage)

        when:
        service.getAccountById(id)

        then:
        interaction {
            1 * storage.query(!null as BiFunction, id)
        }
    }

    def "Returns all created accounts"() {
        given: "service for work with accounts"
        def storage = new AccountStorageImpl()
        def service = new AccountServiceImpl(storage)

        and: "Account holders"
        def name1 = "Luka Modric"
        def name2 = "James Bond"

        when:
        def id1 = service.createAccount(name1)
        def id2 = service.createAccount(name2)

        def accountIds = service.getAllAccountIds()

        then:
        accountIds.size() == 2
        service.getAccountById(id1).name == name1
        service.getAccountById(id2).name == name2
    }

    def "Do transaction if enough money on the 'From' account"() {
        given: "service for work with accounts"
        def storage = new AccountStorageImpl()
        def service = new AccountServiceImpl(storage)

        and: "Account holders"
        def name1 = "Luka Modric"
        def name2 = "James Bond"
        def transactionAmount = 55

        when:
        def id1 = service.createAccount(name1)
        def id2 = service.createAccount(name2)

        def initialAmount1 = service.getAccountById(id1).amount
        def initialAmount2 = service.getAccountById(id2).amount

        assert initialAmount1 > transactionAmount

        def transaction = Transaction.builder().fromAccountId(id1).toAccountId(id2).amount(transactionAmount).build()

        service.doTransaction(transaction)

        then:
        def amount1 = service.getAccountById(id1).amount
        def amount2 = service.getAccountById(id2).amount

        amount1 == initialAmount1 - transactionAmount
        amount2 == initialAmount2 + transactionAmount
    }

    def "Reject transaction if not enough money on the 'From' account"() {
        given: "service for work with accounts"
        def storage = new AccountStorageImpl()
        def service = new AccountServiceImpl(storage)

        and: "Account holders"
        def name1 = "Luka Modric"
        def name2 = "James Bond"
        def transactionAmount = 150

        when:
        def id1 = service.createAccount(name1)
        def id2 = service.createAccount(name2)

        def initialAmount1 = service.getAccountById(id1).amount
        def initialAmount2 = service.getAccountById(id2).amount

        assert initialAmount1 < transactionAmount

        def transaction = Transaction.builder().fromAccountId(id1).toAccountId(id2).amount(transactionAmount).build()

        service.doTransaction(transaction)

        then:
        def amount1 = service.getAccountById(id1).amount
        def amount2 = service.getAccountById(id2).amount

        amount1 == initialAmount1
        amount2 == initialAmount2
    }

}
