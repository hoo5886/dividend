package com.example.demo.persist;

import com.example.demo.persist.entity.CompanyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    boolean existsByTicker(String ticker);

    Optional<CompanyEntity> findByName(String name);
    /*
    * Optional을 쓰는 이유
    * 1. NPE를 방지해주는 효과
    * 2. 값이 없는 경우(null)에 대한 처리를 깔끔하게 정리해준다.
    * */

    //Optional<CompanyEntity> findByTicker(String ticker);

    /*LIKE로 자동완성 기능 만들기.
    1. 특정 문자열로 시작한다. => StringWith
    2. 대소문자 구별 없다. => IgnoreCase
    * */
    Page<CompanyEntity> findByNameStartingWithIgnoreCase(String s, Pageable limit);
}
