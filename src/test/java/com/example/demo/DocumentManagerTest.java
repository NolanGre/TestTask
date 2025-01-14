package com.example.demo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DocumentManagerTest {

    private DocumentManager documentManager;

    @BeforeEach
    void setUp() {
        documentManager = new DocumentManager();
    }

    @Test
    void testSaveNullDocument() {
        assertThrows(IllegalArgumentException.class, () -> documentManager.save(null));
    }

    @Test
    void testSaveNewDocument() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Sample Title")
                .content("Sample Content")
                .author(DocumentManager.Author.builder().id(UUID.randomUUID().toString()).name("Author Name").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        assertNotNull(savedDocument.getId());

        assertEquals("Sample Title", savedDocument.getTitle());
        assertEquals("Sample Content", savedDocument.getContent());
    }

    @Test
    void testSaveExistingDocument() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id(UUID.randomUUID().toString())
                .title("Initial Title")
                .content("Initial Content")
                .author(DocumentManager.Author.builder().id(UUID.randomUUID().toString()).name("Author Name").build())
                .created(Instant.now())
                .build();

        documentManager.save(document);

        document.setTitle("Updated Title");
        DocumentManager.Document updatedDocument = documentManager.save(document);

        assertEquals("Updated Title", updatedDocument.getTitle());
    }

    @Test
    void testFindByIdEmptyId() {
        assertThrows(IllegalArgumentException.class, () -> documentManager.findById(""));
    }

    @Test
    void testFindByIdExistingDocument() {
        String id = UUID.randomUUID().toString();
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id(id)
                .title("Sample Title")
                .content("Sample Content")
                .author(DocumentManager.Author.builder().id(UUID.randomUUID().toString()).name("Author Name").build())
                .created(Instant.now())
                .build();

        documentManager.save(document);

        Optional<DocumentManager.Document> foundDocument = documentManager.findById(id);
        assertTrue(foundDocument.isPresent());
        assertEquals("Sample Title", foundDocument.get().getTitle());
    }

    @Test
    void testFindByIdNonExistingDocument() {
        Optional<DocumentManager.Document> foundDocument = documentManager.findById(UUID.randomUUID().toString());
        assertTrue(foundDocument.isEmpty());
    }

    @Test
    void testSearchNullRequest() {
        assertThrows(IllegalArgumentException.class, () -> documentManager.search(null));
    }

    @Test
    void testSearchEmptyRequest() {
        DocumentManager.Document document1 = DocumentManager.Document.builder()
                .title("Title 1")
                .content("Content 1")
                .author(DocumentManager.Author.builder().id(UUID.randomUUID().toString()).name("Author One").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document document2 = DocumentManager.Document.builder()
                .title("Title 2")
                .content("Content 2")
                .author(DocumentManager.Author.builder().id(UUID.randomUUID().toString()).name("Author Two").build())
                .created(Instant.now())
                .build();

        documentManager.save(document1);
        documentManager.save(document2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder().build();

        List<DocumentManager.Document> results = documentManager.search(request);
        assertEquals(0, results.size());
    }

    @Test
    void testSearchByTitlePrefix() {
        DocumentManager.Document document1 = DocumentManager.Document.builder()
                .title("Introduction to Java")
                .content("Java content")
                .author(DocumentManager.Author.builder().id(UUID.randomUUID().toString()).name("Author One").build())
                .created(Instant.now())
                .build();

        DocumentManager.Document document2 = DocumentManager.Document.builder()
                .title("Advanced Java")
                .content("Advanced content")
                .author(DocumentManager.Author.builder().id(UUID.randomUUID().toString()).name("Author Two").build())
                .created(Instant.now())
                .build();

        documentManager.save(document1);
        documentManager.save(document2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Introduction"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);
        assertEquals(1, results.size());
        assertEquals("Introduction to Java", results.getFirst().getTitle());
    }

    @Test
    void testSearchByAuthorId() {
        String authorId = UUID.randomUUID().toString();
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Document Title")
                .content("Document content")
                .author(DocumentManager.Author.builder().id(authorId).name("Author Name").build())
                .created(Instant.now())
                .build();

        documentManager.save(document);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .authorIds(List.of(authorId))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);
        assertEquals(1, results.size());
        assertEquals("Document Title", results.getFirst().getTitle());
    }

    @Test
    void testSearchByDateRange() {
        Instant now = Instant.now();
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Date Test Document")
                .content("Date content")
                .author(DocumentManager.Author.builder().id(UUID.randomUUID().toString()).name("Author Name").build())
                .created(now)
                .build();

        documentManager.save(document);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .createdFrom(now.minusSeconds(15))
                .createdTo(now.plusSeconds(15))
                .build();


        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(1, results.size());
        assertEquals("Date Test Document", results.getFirst().getTitle());
    }

    @Test
    void testSearchInvalidDateRange() {
        Instant now = Instant.now();
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Date Range Test")
                .content("Content")
                .author(DocumentManager.Author.builder().id(UUID.randomUUID().toString()).name("Author Name").build())
                .created(now)
                .build();

        documentManager.save(document);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .createdFrom(now.plusSeconds(10))
                .createdTo(now.minusSeconds(10))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);
        assertTrue(results.isEmpty());
    }

    @Test
    void testSearchWithMultipleFilters() {
        Instant now = Instant.now();
        String authorId = UUID.randomUUID().toString();

        DocumentManager.Document document1 = DocumentManager.Document.builder()
                .title("Java Basics")
                .content("Content about Java")
                .author(DocumentManager.Author.builder().id(authorId).name("Author One").build())
                .created(now.minusSeconds(10))
                .build();

        DocumentManager.Document document2 = DocumentManager.Document.builder()
                .title("Advanced Java")
                .content("Content about advanced Java")
                .author(DocumentManager.Author.builder().id(authorId).name("Author One").build())
                .created(now.plusSeconds(10))
                .build();

        documentManager.save(document1);
        documentManager.save(document2);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Ja"))
                .containsContents(List.of("advanced"))
                .authorIds(List.of(authorId))
                .createdFrom(now.minusSeconds(5))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);
        assertEquals(1, results.size());
        assertEquals("Advanced Java", results.getFirst().getTitle());
    }

    @Test
    void testSearchNoMatches() {
        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Nonexistent"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);
        assertTrue(results.isEmpty());
    }
}
