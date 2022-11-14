package com.example.demo.scraper;

import com.example.demo.model.Company;
import com.example.demo.model.Dividend;
import com.example.demo.model.ScrapedResult;
import com.example.demo.model.constants.Month;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {

    private static final String STATISTICS_URL =
            "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";
    private static final long START_TIME = 86400; // 60 * 60 * 24 = 1일


    @Override
    public ScrapedResult scrap(Company company) {
        //Java10에서 처음 도입된 var는 변수를 선언할 때 타입을 생략할 수 있으며 컴파일러가 타입을 추론합니다.
        var scrapedResult = new ScrapedResult();
        scrapedResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000;

            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            Elements parsingDivs =
                    document.getElementsByAttributeValue
                            (
                    "data-test",
                    "historical-prices");
            Element tableEle = parsingDivs.get(0); // table 전체

            Element tbody = tableEle.children().get(1); // tbody

            List<Dividend> dividends = new ArrayList<>();
            for (Element e: tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",", ""));
                int year = Integer.valueOf(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }

                dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));

            }
            scrapedResult.setDividends(dividends);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return scrapedResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {

        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Document document = Jsoup.connect(url).get();
            Element titleEle = document.getElementsByTag("h1").get(0);
            String title = titleEle.text().split(" - ")[0].trim();

            return new Company(ticker, title);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
