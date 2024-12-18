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
        return userPointTable.insertOrUpdate(userId, pointId);
    }

    // Use Point
    public UserPoint usePoint(Long userId, Long pointId) {
        return userPointTable.insertOrUpdate(userId, pointId);
    }

}
