package com.evolveback.health.repository.search;

import com.evolveback.health.domain.Points;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the Points entity.
 */
public interface PointsSearchRepository extends ElasticsearchRepository<Points, Long> {
}
