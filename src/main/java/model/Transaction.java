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
@Builder
@EqualsAndHashCode
@ToString
public class Transaction {
    private long id;
    private String fromAccountId;
    private String toAccountId;
    private double amount;
}
