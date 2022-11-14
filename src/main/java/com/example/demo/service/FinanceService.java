package com.example.demo.service;

import com.example.demo.model.Company;
import com.example.demo.model.Dividend;
import com.example.demo.model.ScrapedResult;
import com.example.demo.model.constants.CacheKey;
import com.example.demo.persist.CompanyRepository;
import com.example.demo.persist.DividendRepository;
import com.example.demo.persist.entity.CompanyEntity;
import com.example.demo.persist.entity.DividendEntity;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class FinanceService {

    private final CompanyRepository companyRepository;
    private final DividendRepository dividendRepository;

    /** 1. 요청이 자주 들어오는가?
     * 주식정보의 특성상 특정 데이터에 대한 요청이 몰리는 편이다.
     * APPL이나 GOOG같은 유명한 회사의 정보는 많이 찾고 상대적으로 규모가 작은
     * 회사의 경우에는 검색을 훨씬 덜 적게 한다. 그래서 동일한 정보에 대한 요청이 자주 들어온다.
     * 회사의 배당금 정보를 캐싱해 놓으면
     * 한번 요청이 왔던 데이터는 이후에 DB에 써칭을 하지 않아도 캐시에서 꺼내주면 되기때문에
     * 훨씬 더 빠른 응답이 가능하다.
     */

    /**
     * 2. 자주 변경되는 데이터인가?
     * 데이터의 변경이 잦은 데이터는 데이터가 업데이트 될때마다 캐시에 있는 데이터도 삭제해주거나
     * 업데이트해주어야 한다. 변경이 자주 일어나는 데이터라면 이 과정에 소요되는 시간도 고려해서
     * 캐싱하는 것을 고려해야 한다.
     *
     * 이 배당금 데이터는 과거에 배당받았던 금액의 정보가 바뀌는 경우가 사실상 없다고 봐야하고,
     * 회사명이 바뀌는 경우도 잘 발생하지 않는다. 기껏해야 배당정보가 추가됐을 때, 업데이트되는 경우
     * 가 있을 것이다. 이것도 회사마다 많아야 한 번, 혹은 분기에, 연에 한번 발생하는
     * 데이터의 변경이 굉장히 드물다.
     */

     /** 그래서 이런 이유를 고려해봤을 떄 배당금 정보는 캐싱을 하기에 적합하다.
     */

    @Cacheable(key = "#companyName", value = CacheKey.KEY_FINANCE)
    public ScrapedResult getDividendByCompanyName(String companyName) {
        log.info("search company -> " + companyName);

        // 1. 회사명을 기준으로 회사 정보를 조회
        CompanyEntity company =
                this.companyRepository.findByName(companyName)
                            .orElseThrow(() -> new RuntimeException("존재하지 않는 회사명입니다."));

        // 2. 조회된 회사 ID로 배당금 정보 조회
        List<DividendEntity> dividendEntities =
                this.dividendRepository.findAllByCompanyId(company.getId());


        // 3. 결과 조홥 후 반환
        List<Dividend> dividends = dividendEntities.stream()
                                    .map(e -> new Dividend(e.getDate(), e.getDividend()))
                                    .collect(Collectors.toList());

        return new ScrapedResult(new Company(company.getTicker(), company.getName()),
                                dividends);
    }
}
