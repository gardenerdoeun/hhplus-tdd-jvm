package io.hhplus.tdd.point;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {
    public final PointService pointService;

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        return pointService.selectPoint(id);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        return pointService.selectPointHistory(id);
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     * 정책
     * - 포인트 충전은 최소 100원 이상이어야 합니다.
     * - 포인트 충전은 최대 100,000원을 초과할 수 없습니다.
     * - 포인트 잔액 한도는 최대 500_000원 입니다.
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody long amount
    ) throws IllegalAccessException {
        return pointService.chargePoint(id, amount);
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     * 정책
     * - 사용 할 포인트가 보유 포인트보다 많을 경우 사용 불가능합니다.
     * - 포인트 잔액이 최소 100원이상 보유 중일 때 포인트 사용 가능합니다.
     * - 사용 가능한 포인트는 최대 100,000원입니다.
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) throws IllegalAccessException {
        return pointService.usePoint(id, amount);
    }
}
