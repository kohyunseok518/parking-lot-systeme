```markdown
# 🚗 객체 지향 주차장 시스템 (OOD Parking Lot System)

## 1. 문제 사이트
* [여기에 문제 출처 혹은 사이트 링크를 작성해주세요]

## 2. 프로젝트 목적
백엔드 아키텍처의 기본기를 다지기 위한 순수 자바 도메인 설계 프로젝트입니다. 단순히 동작하는 코드를 짜는 것을 넘어 객체 간의 협력과 적절한 책임 분배에 집중했습니다. 향후 스프링 부트(Spring Boot) 환경이나 대용량 트래픽 상황으로 확장하더라도 흔들리지 않는 견고한 도메인 모델을 구축하는 것이 주된 목표였습니다.

## 3. 폴더 구조
```text
parking-lot-system/
├── src/
│   └── main/
│       └── java/
│           └── com/parkinglot/
│               ├── domain/
│               │   ├── vehicle/
│               │   │   ├── Size.java
│               │   │   ├── Vehicle.java
│               │   │   ├── Car.java
│               │   │   ├── Motorcycle.java
│               │   │   └── Truck.java
│               │   ├── space/
│               │   │   ├── Spot.java
│               │   │   ├── ParkingFloor.java
│               │   │   └── ParkingLot.java
│               │   └── ticket/
│               │       └── ParkingTicket.java
│               ├── policy/
│               │   ├── PricingStrategy.java
│               │   └── CommonPricingStrategy.java
│               └── Application.java

```

## 4. 고민했던 로직 및 해결 방법

### 4-1. 예외 처리를 비즈니스 흐름 제어에 사용하던 문제

**문제 상황:** 초기에는 만차 상황을 처리할 때 `NullPointerException`을 활용해 예외를 던지고 처리했습니다. 예외 생성과 스택 추적은 메모리와 CPU 비용이 매우 무거운 작업입니다.
**해결 방법:** 만차는 시스템 에러가 아니라 예측 가능한 비즈니스 상황입니다. 무거운 예외 처리 대신 조건문과 빠른 반환(Early Return)을 적용해 성능을 최적화하고 가독성을 높였습니다. 불필요한 `else` 블록을 제거해 코드 흐름을 일직선으로 만들었습니다.

```java
// 개선된 ParkingLot.java 로직
public ParkingTicket parked(Vehicle vehicle) {
    for (ParkingFloor floor : floors) {
        // 1. 딱 맞는 자리 탐색
        Spot spot = floor.findAvailableSpot(vehicle);
        if(spot != null) {
            return new ParkingTicket(vehicle.getSize() spot LocalDateTime.now());
        }

        // 2. 더 큰 자리 탐색
        spot = floor.findAvailableBiggerSpot(vehicle);
        if(spot != null) {
            return new ParkingTicket(vehicle.getSize() spot LocalDateTime.now());
        }
    }
    System.out.println("만차입니다.");
    return null;
}

```

### 4-2. CQS(명령과 조회의 분리) 원칙 위배

**문제 상황:** `Spot` 객체에서 주차 가능 여부를 묻는 `canPark()` 메서드 내부에 `isParked = true` 상태 변경 로직이 섞여 있었습니다. 단순 조회를 목적으로 호출해도 자리가 차버리는 부수 효과(Side Effect)가 존재했습니다.
**해결 방법:** 상태를 묻는 질문(Query)과 상태를 변경하는 명령(Command)을 완벽하게 분리하여 언제 호출해도 안전한 메서드로 리팩토링했습니다.

```java
// 개선된 Spot.java 로직
public boolean canPark(Vehicle vehicle) {
    // 단순 조회(Query) 역할만 수행
    if(vehicle.getSize().ordinal() == size.ordinal() && isParked == false) {
        return true;
    } else {
        return false;
    }
}

// 상태 변경(Command) 명시적 분리
public void parked() {
    isParked = true;
}

```

### 4-3. 탐욕적 탐색으로 인한 공간 낭비

**문제 상황:** Enum의 `ordinal()`을 활용해 대소 비교를 구현한 후 리스트를 순회할 때 오토바이가 텅 빈 대형 화물차 자리를 무작정 선점해버리는 공간 비효율 문제가 발생했습니다.
**해결 방법:** 차량 크기와 정확히 일치하는 자리를 먼저 찾아보고 자리가 없을 경우에만 더 큰 자리를 찾는 2단계 우선순위 탐색(Fall-back) 로직을 층(Floor) 객체 내부에 분리해 구현했습니다.

```java
// 개선된 ParkingFloor.java 로직
public Spot findAvailableSpot(Vehicle vehicle) {
    for (Spot spot : spots) {
        if(spot.canPark(vehicle)) {
            spot.parked();
            return spot;
        }
    }
    return null;
}

public Spot findAvailableBiggerSpot(Vehicle vehicle) {
    for (Spot spot : spots) {
        if (spot.canParkBigger(vehicle)) {
            spot.parked();
            return spot;
        }
    }
    return null;
}

```

### 4-4. 요금 정책의 확장성 문제

**문제 상황:** 요구사항에 구체적인 요금 산정 공식이 없었습니다. 이를 도메인 내부에 하드코딩하면 새로운 요금제 추가 시 핵심 코드를 직접 수정해야 하는 OCP(개방-폐쇄 원칙) 위배 문제가 우려되었습니다.
**해결 방법:** `PricingStrategy` 인터페이스를 도입해 전략 패턴(Strategy Pattern)을 적용했습니다. 외부에서 주차권과 현재 시간을 넘겨받아 정책을 수행하도록 설계해 무한한 확장이 가능하도록 구축했습니다.

```java
// 전략 패턴이 적용된 CommonPricingStrategy.java
public class CommonPricingStrategy implements PricingStrategy{
    @Override
    public long calculatePrice(ParkingTicket parkingTicket LocalDateTime outTime) {
        long minutes = ChronoUnit.MINUTES.between(parkingTicket.getInTime() outTime);
        return minutes * 100;
    }
}

```

## 5. 회고

[이곳에 프로젝트를 진행하며 느낀 점이나 어려웠던 점 그리고 앞으로의 학습 방향 등을 자유롭게 작성해주세요]

```

```
