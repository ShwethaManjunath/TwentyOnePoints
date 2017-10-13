package com.evolveback.health.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.evolveback.health.domain.Weight;

import com.evolveback.health.repository.UserRepository;
import com.evolveback.health.repository.WeightRepository;
import com.evolveback.health.repository.search.WeightSearchRepository;
import com.evolveback.health.security.AuthoritiesConstants;
import com.evolveback.health.security.SecurityUtils;
import com.evolveback.health.web.rest.util.HeaderUtil;
import com.evolveback.health.web.rest.util.PaginationUtil;
import com.evolveback.health.web.rest.vm.WeightByPeriod;
import io.swagger.annotations.ApiParam;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * REST controller for managing Weight.
 */
@RestController
@RequestMapping("/api")
public class WeightResource {

    private final Logger log = LoggerFactory.getLogger(WeightResource.class);

    private static final String ENTITY_NAME = "weight";

    private final WeightRepository weightRepository;

    private final WeightSearchRepository weightSearchRepository;

    private final UserRepository userRepository;

    public WeightResource(WeightRepository weightRepository, WeightSearchRepository weightSearchRepository,                     UserRepository userRepository) {
        this.weightRepository = weightRepository;
        this.weightSearchRepository = weightSearchRepository;
        this.userRepository = userRepository;
    }

    /**
     * POST  /weights : Create a new weight.
     *
     * @param weight the weight to create
     * @return the ResponseEntity with status 201 (Created) and with body the new weight, or with status 400 (Bad Request) if the weight has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/weights")
    @Timed
    public ResponseEntity<Weight> createWeight(@Valid @RequestBody Weight weight) throws URISyntaxException {
        log.debug("REST request to save Weight : {}", weight);
        if (weight.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new weight cannot already have an ID")).body(null);
        }
        if (!SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)) {
            log.debug("No user passed in, using current user: {}", SecurityUtils.getCurrentUserLogin());
            weight.setUser(userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get());
        }
        Weight result = weightRepository.save(weight);
        weightSearchRepository.save(result);
        return ResponseEntity.created(new URI("/api/weights/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /weights : Updates an existing weight.
     *
     * @param weight the weight to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated weight,
     * or with status 400 (Bad Request) if the weight is not valid,
     * or with status 500 (Internal Server Error) if the weight couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/weights")
    @Timed
    public ResponseEntity<Weight> updateWeight(@Valid @RequestBody Weight weight) throws URISyntaxException {
        log.debug("REST request to update Weight : {}", weight);
        if (weight.getId() == null) {
            return createWeight(weight);
        }
        Weight result = weightRepository.save(weight);
        weightSearchRepository.save(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, weight.getId().toString()))
            .body(result);
    }

    /**
     * GET  /weights : get all the weights.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of weights in body
     */
    @GetMapping("/weights")
    @Timed
    public ResponseEntity<List<Weight>> getAllWeights(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of Points");
        Page<Weight> page;
        if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)) {
            page = weightRepository.findAllByOrderByTimestampDesc(pageable);
        } else {
            page = weightRepository.findByUserIsCurrentUser(pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, " /api/points");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /weights/:id : get the "id" weight.
     *
     * @param id the id of the weight to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the weight, or with status 404 (Not Found)
     */
    @GetMapping("/weights/{id}")
    @Timed
    public ResponseEntity<Weight> getWeight(@PathVariable Long id) {
        log.debug("REST request to get Weight : {}", id);
        Weight weight = weightRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(weight));
    }

    /**
     * DELETE  /weights/:id : delete the "id" weight.
     *
     * @param id the id of the weight to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/weights/{id}")
    @Timed
    public ResponseEntity<Void> deleteWeight(@PathVariable Long id) {
        log.debug("REST request to delete Weight : {}", id);
        weightRepository.delete(id);
        weightSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/weights?query=:query : search for the weight corresponding
     * to the query.
     *
     * @param query    the query of the weight search
     * @param pageable the pagination information
     * @return the result of the search
     */
    @GetMapping("/_search/weights")
    @Timed
    public ResponseEntity<List<Weight>> searchWeights(@RequestParam String query, @ApiParam Pageable pageable) {
        log.debug("REST request to search for a page of Weights for query {}", query);
        Page<Weight> page = weightSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/weights");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /weight-by-days : get all the weigh-ins for last x days.
     */
    @GetMapping("/weight-by-days/{days}")
    @Timed
    public ResponseEntity<WeightByPeriod> getByDays(@PathVariable int days) {
        LocalDate rightNow = LocalDate.now();
        LocalDate daysAgo = rightNow.minusDays(days);
        List<Weight> weighIns = weightRepository.findAllByTimestampBetweenOrderByTimestampDesc(daysAgo, rightNow);
        WeightByPeriod response = new WeightByPeriod("Last " + days + " Days", filterByUser(weighIns));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private List<Weight> filterByUser(List<Weight> readings) {
        Stream<Weight> userReadings = readings.stream()
            .filter(bp -> bp.getUser().getLogin().equals(SecurityUtils.getCurrentUserLogin()));
        return userReadings.collect(Collectors.toList());
    }

}
