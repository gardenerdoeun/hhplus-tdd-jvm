package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PointServiceConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private UserPointTable userPointTable;

    @Autowired
    private PointHistoryTable pointHistoryTable;

    private final long userId = 1L;

    @BeforeEach
    public void setUp() {
        userPointTable.insertOrUpdate(userId, 10000L); // 초기 포인트 설정
    }

    @Test
    @DisplayName("동시성 테스트 - 충전 및 사용")
    public void testConcurrentChargeAndUse() throws InterruptedException {
        var executor = Executors.newFixedThreadPool(10);

        // 5개의 충전 작업과 5개의 사용 작업 추가
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                try {
                    pointService.chargePoint(userId, 1000L);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });

            executor.submit(() -> {
                try {
                    pointService.usePoint(userId, 500L);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // 최종 포인트 검증
        var finalPoints = userPointTable.selectById(userId).point();
        assertEquals(12500L, finalPoints); // 10,000 + (5 * 1000) - (5 * 500) = 12,500

        // 트랜잭션 기록 검증
        var histories = pointHistoryTable.selectAllByUserId(userId);
        assertEquals(10, histories.size()); // 5 충전 + 5 사용

        long chargeCount = histories.stream().filter(h -> h.type().equals(TransactionType.CHARGE)).count();
        long useCount = histories.stream().filter(h -> h.type().equals(TransactionType.USE)).count();

        assertEquals(5, chargeCount);
        assertEquals(5, useCount);
    }
}
