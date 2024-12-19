package io.hhplus.tdd.point;

import org.springframework.stereotype.Component;

@Component
public class PointValidator {
    private static final long MIN_AMOUNT = 100;
    private static final long MAX_AMOUNT = 100_000;

    public void ChargeValidate(long amount) throws IllegalAccessException {
        if(amount < MIN_AMOUNT) {
            throw new IllegalAccessException("최소 100원 이상이어야 합니다.");
        }
        if(amount > MAX_AMOUNT) {
            throw new IllegalAccessException("최대 100,000원을 초과 할 수 없습니다.");
        }
    }
    public void UseValidate(long amount) throws IllegalAccessException {
        if(amount > MAX_AMOUNT) {
            throw new IllegalAccessException("최대 100,000원을 초과 할 수 없습니다.");
        }
    }

}
