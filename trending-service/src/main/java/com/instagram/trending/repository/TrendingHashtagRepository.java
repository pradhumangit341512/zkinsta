package com.instagram.trending.repository;

import com.instagram.trending.entity.TrendingHashtag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrendingHashtagRepository extends JpaRepository<TrendingHashtag, Long> {

    Optional<TrendingHashtag> findByHashtag(String hashtag);

    @Query("SELECT t FROM TrendingHashtag t ORDER BY t.postCount DESC, t.viewCount DESC")
    List<TrendingHashtag> findTopTrending(Pageable pageable);

    List<TrendingHashtag> findByHashtagContainingIgnoreCase(String query);
}
