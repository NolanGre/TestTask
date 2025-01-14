package com.example.demo;

import lombok.Builder;
import lombok.Data;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */

public class DocumentManager {

    private final Map<String, Document> documentMap = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("Document should not be null");
        }

        if (ObjectUtils.isEmpty(document.getId())) {
            document.setId(UUID.randomUUID().toString());
        }

        documentMap.put(document.getId(), document);
        return documentMap.get(document.getId());
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request should not be null");
        }

        if (isRequestEmpty(request)) {
            return Collections.emptyList();
        }

        return documentMap.values().stream()
                .filter(document -> filterByDate(request.getCreatedFrom(), request.getCreatedTo(), document.getCreated()))
                .filter(document -> containsAuthorId(request.getAuthorIds(), document.getAuthor().getId()))
                .filter(document -> containsContent(request.getContainsContents(), document.getContent()))
                .filter(document -> containsTitlePrefixes(request.getTitlePrefixes(), document.getTitle()))
                .collect(Collectors.toList());
    }

    private boolean isRequestEmpty(SearchRequest request) {
        return Stream.of(
                request.getTitlePrefixes(),
                request.getContainsContents(),
                request.getAuthorIds(),
                request.getCreatedFrom(),
                request.getCreatedTo()
        ).allMatch(Objects::isNull);
    }

    private boolean filterByDate(Instant createdFrom, Instant createdTo, Instant documentCreated) {
        return (createdFrom == null || documentCreated.isAfter(createdFrom)) &&
                (createdTo == null || documentCreated.isBefore(createdTo));
    }

    private boolean containsAuthorId(List<String> authorsIds, String authorId) {
        return (authorsIds == null) || (authorId != null && authorsIds.contains(authorId));
    }

    private boolean containsContent(List<String> containContents, String documentContent) {
        if (containContents == null || documentContent == null) {
            return true;
        }

        List<String> words = Arrays.asList(documentContent.split("\\s+"));

        return words.stream()
                .anyMatch(containContents::contains);
    }

    private boolean containsTitlePrefixes(List<String> titlePrefixes, String documentTitle) {
        if (titlePrefixes == null || documentTitle == null) {
            return true;
        }

        List<String> words = Arrays.asList(documentTitle.split(" "));

        return words.stream()
                .anyMatch(word -> titlePrefixes.stream()
                        .anyMatch(word::startsWith));
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        if (ObjectUtils.isEmpty(id)) {
            throw new IllegalArgumentException("Id should not be empty");
        }

        return Optional.ofNullable(documentMap.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}