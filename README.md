
동시성 제어 방식 분석 및 보고서
-
1. 현재 사용 중인 동시성 제어 방식
   -
   1. ReentrantLock
      - PointService 클래스에서 **ReentrantLock**을 사용하여 동시성 제어를 수행합니다.
      chargePoint와 usePoint 메서드에 lock.lock()과 lock.unlock()으로 동기화 블록을 명시적으로 구현했습니다.
      이는 Critical Section(공유 자원 접근 코드 영역)을 명확히 지정하여 동시에 여러 스레드가 데이터에 접근하지 못하도록 방지합니다.
   2. Thread Pool
      - 테스트 코드에서 **Executors.newFixedThreadPool**을 사용하여 고정된 수의 스레드로 작업을 처리합니다.
      고정된 스레드 수만큼 작업을 병렬로 실행하며, 초과 작업은 대기 큐에 저장되어 기존 작업이 완료되면 실행됩니다.
2. 장점
   1. ReentrantLock의 장점
      - 세밀한 제어: ReentrantLock을 사용하면 Lock을 명시적으로 설정할 수 있어 동기화 범위를 최소화하거나 조정 가능.
      - 공정성 옵션: 필요 시 Lock 생성 시 공정성을 설정하여 대기 중인 스레드가 순차적으로 실행되도록 할 수 있음.
   2. Thread Pool의 장점
      - 자원 효율성: 고정된 수의 스레드를 사용하여 스레드 생성/종료에 따른 오버헤드를 줄임.
      - 작업 관리: 대기 큐를 통해 초과 작업을 관리하며, 시스템 리소스 과부하를 방지.
3. 문제점 및 개선 사항
   1. ReentrantLock 사용 시 문제점
   Lock 범위 최적화:
      - 현재 메서드 전체에 Lock을 적용하고 있어 성능에 영향을 줄 수 있습니다.
    Lock을 필요한 코드 블록으로 제한하면 성능 향상이 가능합니다.
    예를 들어, 데이터베이스에 접근하거나 상태를 업데이트하는 부분만 Lock으로 보호.
    
      - 데드락 위험: 여러 Lock이 필요하거나 다른 스레드 간 순환 대기가 발생할 경우 데드락 위험이 존재.
    이를 방지하려면 Lock 순서를 정하거나, tryLock을 사용하여 타임아웃 기반의 Lock 대기 로직을 도입.
    2. Thread Pool 사용 시 문제점
    - 스레드 수 제한:
        - 스레드 풀 크기가 요청 수에 비해 너무 작을 경우 대기 시간이 길어질 수 있음.
        - 적절한 스레드 수는 작업의 특성과 시스템의 CPU 코어 수에 따라 결정해야 합니다.
    - 공유 자원 접근 충돌:
      - 여러 요청이 동시에 실행되면서 동일한 자원을 접근할 경우, Lock에 의존하여 성능 저하가 발생할 수 있음.
4. 개선 방안
   1. Lock 범위 최소화
        - Lock을 메서드 전체가 아니라 필요한 범위(예: 데이터베이스 접근과 갱신)로 축소하여 성능 최적화.
   2. 비동기 처리 도입
      - 비동기 작업 처리를 통해, 요청이 많을 때 대기 큐에 들어가는 작업을 더욱 효율적으로 관리.
   3. 테스트 코드 개선
      - 동시성 문제를 재현하기 위해 Thread.sleep 등을 도입하여 테스트 상황을 더욱 현실적으로 구성.
      - 예상치 못한 결과에 대해 추가적인 검증 로직과 로깅을 추가.
5. 결론

   현재 동시성 제어는 ReentrantLock과 Thread Pool을 사용해 구현되어 있으며, 기본적인 동시성 문제는 방지할 수 있습니다.
   그러나 Lock의 범위 최적화와 테스트 시 추가적인 로깅, 그리고 스레드 풀 크기 조정 등의 개선을 통해 성능과 신뢰성을 더욱 향상시킬 수 있습니다.