package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointService {

    private static final long MAX_BALANCE = 500_000; // 최대 보유 잔액
    private static final long MIN_BALANCE_USE = 100; // 최소 사용 가능 금액

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;
    private final PointValidator pointValidator;
    private final Lock lock = new ReentrantLock(); // 동시성 제어를 위한 ReentrantLock

    public PointService(PointHistoryTable pointHistoryTable, UserPointTable userPointTable, PointValidator pointValidator) {
        this.pointHistoryTable = pointHistoryTable;
        this.userPointTable = userPointTable;
        this.pointValidator = pointValidator;
    }

    // Select Point
    public UserPoint selectPoint(Long userId) {
        UserPoint userPoint = userPointTable.selectById(userId);
        if(userPoint == null) {
            throw new RuntimeException("포인트 정보가 없습니다");
        }
        return userPoint;
    }

    // Select Histories
    public List<PointHistory> selectPointHistory(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    // Charge Point
    public UserPoint chargePoint(Long userId, Long amount) throws IllegalAccessException {
        lock.lock(); // 동시성 제어 시작
        try {
            pointValidator.ChargeValidate(amount);
            UserPoint currentPoint = userPointTable.selectById(userId);

            if (currentPoint.point() + amount > 500_000) {
                throw new IllegalStateException("잔액이 최대 보유 한도를 초과합니다.");
            }

            UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, currentPoint.point() + amount);
            pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis());

            return updatedPoint;
        } finally {
            lock.unlock(); // 동시성 제어 해제
        }
    }

    // Use Point
    public UserPoint usePoint(Long userId, Long amount) throws IllegalAccessException {
        lock.lock(); // 동시성 제어 시작
        try {
            pointValidator.UseValidate(amount);
            UserPoint currentPoint = userPointTable.selectById(userId);

            if (currentPoint.point() < 100) {
                throw new IllegalStateException("100원 이상이어야 합니다.");
            }

            if (currentPoint.point() - amount < 0) {
                throw new IllegalStateException("잔액이 부족합니다.");
            }

            UserPoint updatedPoint = userPointTable.insertOrUpdate(userId, currentPoint.point()-amount);
            pointHistoryTable.insert(userId, amount, TransactionType.USE, System.currentTimeMillis());

            return updatedPoint;
        } finally {
            lock.unlock(); // 동시성 제어 해제
        }
    }

}
