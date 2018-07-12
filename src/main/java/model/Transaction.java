package model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by Anton Tolkachev.
 * Since 09.07.2018
 */
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
@ToString
public class Transaction {
    private long id;
    private String fromAccountId;
    private String toAccountId;
    private double amount;

    public Transaction() {
    }

    public Transaction(long id, String fromAccountId, String toAccountId, double amount) {
        this.id = id;
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }
}
