package com.example.demo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * [굳이 엔티티 클래스를 사용하지 않고 model클래스를 따로 정의해주는 이유]
 * 엔티티는 DB와 직접적으로 매핑되기 위한 클래스이다.
 * 엔티티 인스턴스를 서비스 코드 내부에서 데이터 주고받기 위한 용도로 쓰거나
 * 이 과정에서 데이터 내용을 변경하는 로직이 들어가면
 * 이 클래스의 원래 역할의 범위를 벗어난다.
 *
 * [코드의 재사용과 차이는 무엇일까]
 * 코드의 재사용은 여러 로직에 동작들을 쪼개서 쪼갠 동작간에 유사성을 가지는 부분
 * 비슷한 역할을 하는 동작을 찾고 그 역할을 일반화해서 코드를 재사용할 수 있도록
 * 코드의 중복을 없애주는 것
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    private String ticker;
    private String name;
}


