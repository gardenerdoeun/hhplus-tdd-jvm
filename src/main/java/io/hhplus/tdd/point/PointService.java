package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointService {
    private final PointHistoryTable pointHistoryTable;
    private final UserPointTable userPointTable;

    public PointService(PointHistoryTable pointHistoryTable, UserPointTable userPointTable) {
        this.pointHistoryTable = pointHistoryTable;
        this.userPointTable = userPointTable;
    }

    // Select Point
    public UserPoint selectPoint(Long userId) {
        return userPointTable.selectById(userId);
    }

    // Select Histories
    public List<PointHistory> selectPointHistory(long userId) {
        return pointHistoryTable.selectAllByUserId(userId);
    }

    // Charge Point
    public UserPoint chargePoint(Long userId, Long pointId) {
        // 사용자 포인트 조회
        UserPoint userPoint = userPointTable.selectById(userId);

        // 포인트 충전
        pointId = userPoint.point() + pointId;
        userPoint= userPointTable.insertOrUpdate(userId, pointId);

        // 포인트 충전 내역 등록
        pointHistoryTable.insert(userId, pointId, TransactionType.CHARGE, System.currentTimeMillis());

        return userPoint;
    }

    // Use Point
    public UserPoint usePoint(Long userId, Long pointId) {
        // 사용자 포인트 조회
        UserPoint userPoint = userPointTable.selectById(userId);

        // 포인트 차감
        pointId = userPoint.point() - pointId;
        userPoint = userPointTable.insertOrUpdate(userId, pointId);

        // 포인트 사용 내역 등록
        pointHistoryTable.insert(userId, pointId, TransactionType.USE, System.currentTimeMillis());

        return userPoint;
    }

}
