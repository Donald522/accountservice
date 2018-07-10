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
public class Account {
    private String id;
    private String name;
    private String currency;
    private double amount;
}
