package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Incubating;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
//    private final UserPointTable userPointTable = mock(UserPointTable.class);
//    private final PointHistoryTable pointHistoryTable = mock(PointHistoryTable.class);

    @Mock
    private UserPointTable userPointTable;
    @Mock
    private PointHistoryTable pointHistoryTable;
    @Mock
    private PointValidator pointValidator;
    @InjectMocks
    private PointService pointService;

    @Test
    @DisplayName("포인트 조회 성공")
    void testSelectPoint() {
        //given
        long userId = 1L;
        UserPoint userPoint = new UserPoint(userId, 1000, System.currentTimeMillis());

        when(userPointTable.selectById(userId)).thenReturn(userPoint);

        //when
        UserPoint result = pointService.selectPoint(userId);

        //then
        assertEquals(1000, result.point());

        //mock 호출 검증
        verify(userPointTable, times(1)).selectById(userId);
    }

    @Test
    @DisplayName("포인트 내역 조회 성공")
    void testSelectPointHistory() {
        // Given
        long userId = 1L;
        List<PointHistory> history = List.of(
                new PointHistory(1, userId, 500, TransactionType.CHARGE, System.currentTimeMillis()),
                new PointHistory(2, userId, -200, TransactionType.USE, System.currentTimeMillis())
        );

        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(history);

        // When
        List<PointHistory> result = pointService.selectPointHistory(userId);

        // Then
        assertEquals(2, result.size());
        assertEquals(500, result.get(0).amount());
        assertEquals(TransactionType.CHARGE, result.get(0).type());
        verify(pointHistoryTable, times(1)).selectAllByUserId(userId);
    }

    @Test
    @DisplayName("포인트 충전 성공")
    void testChargePoint() throws IllegalAccessException {
        // Given
        long userId = 1L;
        long amount = 500;
        UserPoint currentPoint = new UserPoint(userId, 1000, System.currentTimeMillis());
        UserPoint updatedPoint = new UserPoint(userId, 1500, System.currentTimeMillis());

        doNothing().when(pointValidator).ChargeValidate(amount);
        when(userPointTable.selectById(userId)).thenReturn(currentPoint);
        when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(updatedPoint);

        // When
        UserPoint result = pointService.chargePoint(userId, amount);

        // Then
        assertEquals(1500, result.point());
        verify(pointValidator, times(1)).ChargeValidate(amount);
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, times(1)).insertOrUpdate(userId, amount);
        verify(pointHistoryTable, times(1))
                .insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    @DisplayName("포인트 충전 실패 - 최소 금액 미달")
    void chargePoint_InvalidAmount() throws IllegalAccessException {
        // Given
        long userId = 1L;
        long invalidAmount = 50; // 최소 금액 미달

        // Validator가 금액 검증에서 예외를 던지도록 설정
        doThrow(new IllegalArgumentException("금액은 최소 100원에서 최대 100,000원이어야 합니다."))
                .when(pointValidator).ChargeValidate(invalidAmount);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> pointService.chargePoint(userId, invalidAmount));

        // Mock 호출 검증
        verify(pointValidator, times(1)).ChargeValidate(invalidAmount);
        verify(userPointTable, never()).selectById(anyLong());
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }


    @Test
    @DisplayName("포인트 충전 실패 - 충전 한도 초과")
    void chargePoint_ExceedsMaxBalance() throws IllegalAccessException {
        // Given
        long userId = 1L;
        long amount = 10_000;
        UserPoint currentPoint = new UserPoint(userId, 495_000, System.currentTimeMillis());

        // Validator는 금액 검증에 성공하도록 설정
        doNothing().when(pointValidator).ChargeValidate(amount);
        when(userPointTable.selectById(userId)).thenReturn(currentPoint);

        // When & Then
        assertThrows(IllegalStateException.class, () -> pointService.chargePoint(userId, amount));

        // Mock 호출 검증
        verify(pointValidator, times(1)).ChargeValidate(amount);
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    @DisplayName("포인트 사용 실패 - 잔액 부족")
    void usePoint_InsufficientBalance() throws IllegalAccessException {
        // Given
        long userId = 1L;
        long amount = 950; // 사용 금액이 너무 커서 잔액 부족
        UserPoint currentPoint = new UserPoint(userId, 900, System.currentTimeMillis());

        // Validator는 금액 검증에 성공하도록 설정
        doNothing().when(pointValidator).UseValidate(amount);
        when(userPointTable.selectById(userId)).thenReturn(currentPoint);

        // When & Then
        assertThrows(IllegalStateException.class, () -> pointService.usePoint(userId, amount));

        // Mock 호출 검증
        verify(pointValidator, times(1)).UseValidate(amount);
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    @DisplayName("포인트 사용 실패 - 최소 사용 가능한 보유 잔액 부족")
    void usePoint_Failure_CurrentBalance() throws IllegalAccessException {
        // Given
        long userId = 1L;
        long amount = 950;
        UserPoint currentPoint = new UserPoint(userId, 50, System.currentTimeMillis());

        // Validator는 금액 검증에 성공하도록 설정
        doNothing().when(pointValidator).UseValidate(amount);
        when(userPointTable.selectById(userId)).thenReturn(currentPoint);

        // When & Then
        assertThrows(IllegalStateException.class, () -> pointService.usePoint(userId, amount));

        // Mock 호출 검증
        verify(pointValidator, times(1)).UseValidate(amount);
        verify(userPointTable, times(1)).selectById(userId);
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }

    @Test
    @DisplayName("포인트 사용 실패 - 사용 금액 초과")
    void testUsePoint() throws IllegalAccessException {
        // Given
        long userId = 1L;
        long amount = 150_000;

        doThrow(new IllegalArgumentException("사용 포인트는 최대 100,000원입니다."))
                .when(pointValidator).UseValidate(amount);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> pointService.usePoint(userId, amount));

        verify(pointValidator, times(1)).UseValidate(amount);
        verify(userPointTable, never()).selectById(anyLong());
        verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
        verify(pointHistoryTable, never()).insert(anyLong(), anyLong(), any(TransactionType.class), anyLong());
    }
}