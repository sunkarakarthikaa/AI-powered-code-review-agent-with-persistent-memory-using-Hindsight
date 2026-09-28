package com.prguardian.repository;

import com.prguardian.model.PullRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB generates the implementation of this interface at
 * runtime — no hand-written query code needed for basic operations.
 * The two finder methods below use Spring Data's method-name query
 * derivation: it parses "findByRepoNameOrderBySubmittedAtDesc" and builds
 * the Mongo query from the method name itself.
 */
public interface PullRequestRepository extends MongoRepository<PullRequest, String> {

    List<PullRequest> findByRepoNameOrderBySubmittedAtDesc(String repoName);

    List<PullRequest> findByStatusOrderBySubmittedAtDesc(PullRequest.PrStatus status);
}