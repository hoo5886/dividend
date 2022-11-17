package com.example.demo.scheduler;

import com.example.demo.model.Company;
import com.example.demo.model.ScrapedResult;
import com.example.demo.model.constants.CacheKey;
import com.example.demo.persist.CompanyRepository;
import com.example.demo.persist.DividendRepository;
import com.example.demo.persist.entity.CompanyEntity;
import com.example.demo.persist.entity.DividendEntity;
import com.example.demo.scraper.Scraper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@EnableCaching
@AllArgsConstructor //레파지토리가 초기화될 수 있도록
public class ScraperScheduler {

    private CompanyRepository companyRepository;
    private final Scraper yahooFinanceScraper;
    private final DividendRepository dividendRepository;

    // 일정 주기마다 수행
    @CacheEvict(value = CacheKey.KEY_FINANCE, allEntries = true)
    @Scheduled(cron = "${scheduler.scrap.yahoo}")
    public void yahooFinanceScheduling() {
        log.info("scraping scheduler is started");
        //1. 저장된 회사 목록을 조회
        List<CompanyEntity> companies = this.companyRepository.findAll();

        //2. 회사마다 배당금 정보를 새로 스크래핑
        for (CompanyEntity company : companies) {
            log.info("scraping scheduler is started -> " + company.getName());
            ScrapedResult scrapedResult =
                    this.yahooFinanceScraper.scrap(new Company(company.getTicker(), company.getName()));

            //3. 스크래핑한 배당금 정보 중에 DB에 없는 값은 저장한다.
            scrapedResult.getDividends().stream()
                    // Dividend 모델을 dividend Entity로 매핑
                    .map(e -> new DividendEntity(company.getId(), e))
                    // elements를 하나씩 Dividend Repository에 삽입
                    .forEach(e -> {
                        boolean exists = this.dividendRepository.existsByCompanyIdAndDate(e.getCompanyId(), e.getDate());
                        if (!exists) {
                            this.dividendRepository.save(e);
                            log.info("insert new dividend -> " + e.toString());
                        }
                    });
            // 연속적으로 scraping 대상 사이트 서버에 요청을 날리지 않도록 일시정지
            try {
                Thread.sleep(3000); // 3 seconds
            } catch (InterruptedException e) { // 하던 일을 멈추라는 신호를 보낼 때 사용
                Thread.currentThread().interrupt();
            }
        }





    }
}
