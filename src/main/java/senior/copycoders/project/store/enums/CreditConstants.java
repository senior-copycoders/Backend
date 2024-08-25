package senior.copycoders.project.store.enums;

import lombok.Getter;

@Getter
public enum CreditConstants {
    MAX_CREDIT_AMOUNT(30_000_000),
    MIN_CREDIT_AMOUNT(200_000),
    MAX_INTEREST_RATE(18),
    MIN_INTEREST_RATE(0),
    MAX_CREDIT_PERIOD(360),
    MIN_CREDIT_PERIOD(12);

    private final int value;

    CreditConstants(int value) {
        this.value = value;
    }

}
