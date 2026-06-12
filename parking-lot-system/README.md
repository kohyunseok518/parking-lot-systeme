# 객체 지향 주차장 시스템 (OOD Parking Lot System)

## 프로젝트 목적
백엔드 아키텍처의 기본기를 다지기 위한 순수 자바 도메인 설계 프로젝트입니다. 단순히 동작하는 코드를 짜는 것을 넘어 객체 간의 협력과 적절한 책임 분배에 집중했습니다. 향후 스프링 부트(Spring Boot) 환경이나 대용량 트래픽 상황으로 확장하더라도 흔들리지 않는 견고한 도메인 모델을 구축하는 것이 주된 목표였습니다.

## 핵심 설계 및 리팩토링 로직

### 1. 상태를 묻지 말고 행동을 시켜라 (Tell, Don't Ask)
초기 설계에서는 주차장이 주차 공간(Spot)의 데이터를 직접 꺼내와서 주차 가능 여부를 통제했습니다. 이 구조는 특정 객체가 모든 정보를 쥐고 흔드는 갓 클래스(God Class)를 유발할 위험이 컸습니다.
이를 해결하기 위해 주차 공간 스스로가 자신의 상태(크기와 빈자리 여부)를 기준으로 입차 가능 여부를 판단하도록 책임을 넘겼습니다. 객체들이 서로의 데이터를 빼앗지 않고 메시지만 주고받으며 정당하게 협력하는 구조를 완성했습니다.

### 2. 명령과 조회의 분리 (CQS 원칙 준수)
초기에는 주차 가능 여부를 확인하는 동시에 내부 상태를 주차 완료로 변경해 버리는 치명적인 논리 오류가 있었습니다. 단순 조회를 목적으로 메서드를 호출해도 자리가 차버리는 부수 효과(Side Effect)가 발생할 수 있었습니다.
주차할 수 있는지 물어보는 질문(Query)과 실제 상태를 변경하는 명령(Command)을 완벽히 분리해 언제 어디서 시스템 상태를 조회하든 안전성을 보장하도록 개선했습니다.

### 3. 예외 처리(try-catch)와 비즈니스 흐름 제어의 분리
만차 상황을 제어할 때 처음에는 NullPointerException을 활용했습니다. 하지만 주차장에 자리가 없는 상황은 시스템 버그가 아니라 애플리케이션이 당연히 예측하고 방어해야 하는 정상적인 비즈니스 규칙입니다.
비용이 매우 무거운 예외 역추적 로직 대신 조건문(if)을 활용한 빠른 반환(Early Return) 구조로 제어 흐름을 변경했습니다. 이를 통해 불필요한 중첩을 줄이고 가독성과 성능을 모두 확보했습니다.

### 4. 공간 낭비를 막는 우선순위 탐색 (Fall-back Logic)
차량과 공간의 크기를 Enum으로 캡슐화하고 내부 순서값을 활용해 수학적인 대소 비교를 구현했습니다.
오토바이 같은 작은 차량이 텅 빈 대형 화물차 자리를 무작정 선점해 버리는 공간 낭비 문제를 발견했습니다. 이를 막기 위해 차량 크기와 정확히 일치하는 자리를 먼저 탐색하고 전부 만차일 경우에만 더 큰 자리를 내어주는 2단계 탐색 로직을 적용해 공간 효율을 극대화했습니다.

### 5. 전략 패턴(Strategy Pattern)을 통한 OCP 달성
요금 산정 로직은 비즈니스 요구사항의 변화에 가장 민감한 부분입니다.
구체적인 요금 계산 방식을 도메인 내부에 하드코딩하지 않고 PricingStrategy 인터페이스를 도입해 외부에서 정책을 주입받도록 분리했습니다. 향후 주말 요금제나 정기권 같은 새로운 정책이 추가되더라도 핵심 주차장 코드는 단 한 줄도 수정할 필요가 없습니다.

## UML 클래스 다이어그램
```mermaid
classDiagram
    %% Enum
    class Size {
        <<enumeration>>
        SMALL
        MIDDLE
        BIG
    }

    %% Vehicle & Subclasses
    class Vehicle {
        - size: Size
        ~ Vehicle(size: Size)
        + getSize(): Size
    }
    class Motorcycle {
        + Motorcycle()
    }
    class Car {
        + Car()
    }
    class Truck {
        + Truck()
    }

    Vehicle <|-- Motorcycle
    Vehicle <|-- Car
    Vehicle <|-- Truck
    Vehicle --> Size

    %% Domain Objects
    class Spot {
        - size: Size
        - isParked: boolean
        + Spot(size: Size)
        + canPark(vehicle: Vehicle): boolean
        + canParkBigger(vehicle: Vehicle): boolean
        + parked()
        + unParked()
    }
    Spot --> Size

    class ParkingFloor {
        - spots: List~Spot~
        + ParkingFloor(spots: List~Spot~)
        + findAvailableSpot(vehicle: Vehicle): Spot
        + findAvailableBiggerSpot(vehicle: Vehicle): Spot
    }
    ParkingFloor *-- "many" Spot : contains

    class ParkingLot {
        - floors: List~ParkingFloor~
        + parked(vehicle: Vehicle): ParkingTicket
        + out(parkingTicket: ParkingTicket, pricingStrategy: PricingStrategy)
    }
    ParkingLot *-- "many" ParkingFloor : contains

    %% Ticket
    class ParkingTicket {
        - vehicleSize: Size
        - spot: Spot
        - inTime: LocalDateTime
        + ParkingTicket(vehicleSize: Size, spot: Spot, inTime: LocalDateTime)
        + getSpot(): Spot
        + getInTime(): LocalDateTime
    }
    ParkingTicket --> Size
    ParkingTicket --> Spot : references

    %% Pricing Strategy
    class PricingStrategy {
        <<interface>>
        + calculatePrice(parkingTicket: ParkingTicket, outTime: LocalDateTime): long
    }
    class CommonPricingStrategy {
        + calculatePrice(parkingTicket: ParkingTicket, outTime: LocalDateTime): long
    }

    PricingStrategy <|.. CommonPricingStrategy : implements

    %% Dependencies
    ParkingLot ..> ParkingTicket : creates
    ParkingLot ..> PricingStrategy : uses