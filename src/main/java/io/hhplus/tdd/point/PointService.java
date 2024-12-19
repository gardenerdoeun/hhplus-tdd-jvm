package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {

    private static final long MIN_CHARGE_AMOUNT = 100;
    private static final long MAX_CHARGE_AMOUNT = 100_000;
    private static final long MAX_BALANCE = 500_000;

    private static final long MIN_BALANCE_USE = 100;
    private static final long MAX_USE_AMOUNT = 100_000;

    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;

    public PointService(PointHistoryTable pointHistoryTable, UserPointTable userPointTable) {
        this.pointHistoryTable = pointHistoryTable;
        this.userPointTable = userPointTable;
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
    public UserPoint chargePoint(Long userId, Long pointId) throws IllegalAccessException {
        if(pointId < MIN_CHARGE_AMOUNT || pointId > MAX_CHARGE_AMOUNT) {
            throw new IllegalAccessException("충전 포인트는 최소 100원 이상, 최대 100,000원 이하 입니다");
        }
        // 사용자 포인트 조회
        UserPoint currentUserPoint = userPointTable.selectById(userId);

        // 포인트 충전
        long newBalance = currentUserPoint.point() + pointId;
        if(newBalance < MAX_BALANCE) {
            throw new IllegalAccessException("포인트 잔액은 최대 "+ MAX_BALANCE + "을 초과할 수 없습니다.");
        }
        UserPoint updateUserPoint = userPointTable.insertOrUpdate(userId, pointId);

        // 포인트 충전 내역 등록
        pointHistoryTable.insert(userId, pointId, TransactionType.CHARGE, System.currentTimeMillis());

        return updateUserPoint;
    }

    // Use Point
    public UserPoint usePoint(Long userId, Long pointId) throws IllegalAccessException {
        if(pointId > MAX_USE_AMOUNT) {
            throw new IllegalAccessException("사용 포인트는 최대 100,000원을 초과할 수 없습니다.");
        }
        // 사용자 포인트 조회
        UserPoint currentPoint = userPointTable.selectById(userId);

        // 포인트 차감
        if(currentPoint.point()-pointId < MIN_BALANCE_USE) {
            throw new IllegalAccessException("포인트 잔액은 최소 "+MIN_BALANCE_USE+"이상이어야 합니다.");
        }
        UserPoint updateUserPoint = userPointTable.insertOrUpdate(userId, -pointId);

        // 포인트 사용 내역 등록
        pointHistoryTable.insert(userId, pointId, TransactionType.USE, System.currentTimeMillis());

        return updateUserPoint;
    }

}
