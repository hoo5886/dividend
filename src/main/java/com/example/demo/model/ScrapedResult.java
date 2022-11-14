package com.example.demo.model;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 스크래핑한 회사가 어떤 회사인지 알 수 있는 컴퍼니 정보를 담은 컴퍼니 인스턴드 인스턴스,
 * 배당금 인스턴스리스트를 맴버변수로 가진다.
 * 한 회사는 여러 개의 배당금 정보를 갖는다.
 */

@Data
@AllArgsConstructor
public class ScrapedResult {

    private Company company;

    private List<Dividend> dividends;

    public ScrapedResult() {
        this.dividends = new ArrayList<>();
    }
}
