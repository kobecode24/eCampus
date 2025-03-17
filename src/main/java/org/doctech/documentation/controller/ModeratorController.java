package org.doctech.documentation.controller;

import lombok.RequiredArgsConstructor;
import org.doctech.common.dto.ApiResponse;
import org.doctech.common.exception.DocumentationNotFoundException;
import org.doctech.documentation.dto.DocumentationDTO;
import org.doctech.documentation.dto.DocumentationSectionDTO;
import org.doctech.documentation.model.Documentation;
import org.doctech.documentation.model.DocumentationStatus;
import org.doctech.documentation.repository.DocumentationRepository;
import org.doctech.documentation.service.DocumentationService;
import org.doctech.security.model.SecurityUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/moderator/documentation")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
public class ModeratorController {

    private final DocumentationService documentationService;
    private final DocumentationRepository documentationRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getModeratorDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Get pending reviews
        Map<String, Object> pendingReviews = new HashMap<>();
        pendingReviews.put("count", documentationService.countByStatus(DocumentationStatus.REVIEW));
        pendingReviews.put("items", documentationService.getDocumentationByStatus(
            DocumentationStatus.REVIEW, 0, 5));
        dashboard.put("pendingReviews", pendingReviews);
        
        // Get recently published
        Map<String, Object> recentlyPublished = new HashMap<>();
        recentlyPublished.put("count", documentationService.countByStatus(DocumentationStatus.PUBLISHED));
        recentlyPublished.put("items", documentationService.getDocumentationByStatus(
            DocumentationStatus.PUBLISHED, 0, 5));
        dashboard.put("recentlyPublished", recentlyPublished);
        
        // Activity summary
        Map<String, Object> activity = new HashMap<>();
        activity.put("today", 5);
        activity.put("week", 23);
        activity.put("month", 87);
        dashboard.put("activity", activity);
        
        // Performance metrics
        Map<String, Object> performance = new HashMap<>();
        performance.put("avgReviewTime", "2.3 days");
        performance.put("docsApproved", 42);
        performance.put("docsRejected", 7);
        dashboard.put("performance", performance);
        
        return ResponseEntity.ok(new ApiResponse(true, "Moderator dashboard data retrieved", dashboard));
    }

    @GetMapping("/queue")
    public ResponseEntity<ApiResponse> getModeratorQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        List<DocumentationDTO> queue = documentationService.getDocumentationByStatus(
            DocumentationStatus.REVIEW, page, size);
        
        return ResponseEntity.ok(new ApiResponse(true, "Moderator queue retrieved", queue));
    }
    
    @PostMapping("/{docId}/review")
    public ResponseEntity<ApiResponse> submitReview(
            @PathVariable UUID docId,
            @RequestBody Map<String, Object> reviewData,
            Authentication authentication) {
        
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        boolean approved = (boolean) reviewData.get("approved");
        String feedback = (String) reviewData.get("feedback");
        
        DocumentationStatus newStatus = approved ? 
            DocumentationStatus.PUBLISHED : DocumentationStatus.DRAFT;
        
        // Store the review feedback in a fake manner since we're not implementing the full model yet
        documentationService.updateDocumentationStatus(docId, newStatus);
        
        // Create mock review data for response
        Map<String, Object> review = new HashMap<>();
        review.put("documentationId", docId);
        review.put("reviewerId", user.getId());
        review.put("reviewerUsername", user.getUsername());
        review.put("approved", approved);
        review.put("feedback", feedback);
        review.put("reviewDate", LocalDateTime.now());
        review.put("newStatus", newStatus);
        
        return ResponseEntity.ok(new ApiResponse(true, "Review submitted successfully", review));
    }
    
    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse> getModeratorAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        // Status distribution
        Map<String, Long> statusDistribution = new HashMap<>();
        for (DocumentationStatus status : DocumentationStatus.values()) {
            statusDistribution.put(status.name(), documentationService.countByStatus(status));
        }
        analytics.put("statusDistribution", statusDistribution);
        
        // Document type distribution
        analytics.put("technologyDistribution", Map.of(
            "FRONTEND", 35,
            "BACKEND", 28,
            "API", 22,
            "LIBRARY", 15
        ));
        
        // Activity timeline (simulated)
        List<Map<String, Object>> timeline = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Map<String, Object> dayStats = new HashMap<>();
            dayStats.put("date", LocalDateTime.now().minusDays(i).toLocalDate().toString());
            dayStats.put("published", new Random().nextInt(5));
            dayStats.put("reviewed", new Random().nextInt(8));
            dayStats.put("created", new Random().nextInt(10));
            timeline.add(dayStats);
        }
        analytics.put("activityTimeline", timeline);
        
        // Top tags
        analytics.put("topTags", List.of(
            Map.of("tag", "React", "count", 25),
            Map.of("tag", "Spring Boot", "count", 22),
            Map.of("tag", "API", "count", 20),
            Map.of("tag", "Database", "count", 15),
            Map.of("tag", "Security", "count", 12)
        ));
        
        return ResponseEntity.ok(new ApiResponse(true, "Moderator analytics retrieved", analytics));
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getDocumentationPortal() {
        Map<String, Object> portalData = new HashMap<>();
        
        // Get counts of documentation by status
        portalData.put("counts", Map.of(
            "draft", documentationService.countByStatus(DocumentationStatus.DRAFT),
            "review", documentationService.countByStatus(DocumentationStatus.REVIEW),
            "published", documentationService.countByStatus(DocumentationStatus.PUBLISHED),
            "archived", documentationService.countByStatus(DocumentationStatus.ARCHIVED),
            "total", documentationRepository.count()
        ));
        
        // Get recent documentation
        portalData.put("recentDocs", documentationService.getDocumentationByStatus(
            DocumentationStatus.PUBLISHED, 0, 5));
        
        // Get pending reviews
        portalData.put("pendingReviews", documentationService.getDocumentationByStatus(
            DocumentationStatus.REVIEW, 0, 5));
        
        return ResponseEntity.ok(new ApiResponse(true, "Documentation portal data retrieved", portalData));
    }

    @GetMapping("/structure/{docId}")
    public ResponseEntity<ApiResponse> getDocumentStructure(@PathVariable UUID docId) {
        // This endpoint already exists in DocumentationController
        // call that implementation or duplicates it here
        Documentation doc = documentationRepository.findById(docId)
            .orElseThrow(() -> new DocumentationNotFoundException("Documentation not found with id: " + docId));
        
        List<DocumentationSectionDTO> sections = documentationService.getDocumentationSections(docId);
        
        Map<String, Object> structure = new HashMap<>();
        structure.put("id", doc.getId());
        structure.put("title", doc.getTitle());
        structure.put("status", doc.getStatus());
        structure.put("sections", sections);
        
        return ResponseEntity.ok(new ApiResponse(
            true,
            "Document structure retrieved successfully",
            structure
        ));
    }

    @PostMapping("/status/{docId}")
    public ResponseEntity<ApiResponse> updateDocumentationStatus(
            @PathVariable UUID docId,
            @RequestBody Map<String, String> statusData,
            Authentication authentication) {
        
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        DocumentationStatus status = DocumentationStatus.valueOf(statusData.get("status"));
        String comment = statusData.getOrDefault("comment", "");
        
        // Update status
        documentationService.updateDocumentationStatus(docId, status);
        
        // Create action record for response
        Map<String, Object> action = new HashMap<>();
        action.put("documentationId", docId);
        action.put("userId", user.getId());
        action.put("username", user.getUsername());
        action.put("status", status);
        action.put("comment", comment);
        action.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(new ApiResponse(true, "Documentation status updated", action));
    }

    @GetMapping("/editor/{docId}")
    public ResponseEntity<ApiResponse> getDocumentEditor(@PathVariable UUID docId) {
        DocumentationDTO doc = documentationService.getDocumentationById(docId);
        List<DocumentationSectionDTO> sections = documentationService.getDocumentationSections(docId);
        
        Map<String, Object> editorData = new HashMap<>();
        editorData.put("document", doc);
        editorData.put("sections", sections);
        
        // Add dummy editor metadata
        editorData.put("metadata", Map.of(
            "lastSaved", LocalDateTime.now().minusMinutes(5),
            "wordCount", countWords(doc.getContent()),
            "readTime", estimateReadTime(doc.getContent())
        ));
        
        return ResponseEntity.ok(new ApiResponse(true, "Document editor data retrieved", editorData));
    }

    @GetMapping("/workflow/{docId}")
    public ResponseEntity<ApiResponse> getDocumentationWorkflow(@PathVariable UUID docId) {
        Documentation doc = documentationRepository.findById(docId)
            .orElseThrow(() -> new DocumentationNotFoundException("Documentation not found"));
        
        // Create a workflow representation
        Map<String, Object> workflow = new HashMap<>();
        workflow.put("id", doc.getId());
        workflow.put("title", doc.getTitle());
        workflow.put("currentStatus", doc.getStatus());
        
        // Create history simulation
        List<Map<String, Object>> history = new ArrayList<>();
        
        // Always add creation status
        history.add(Map.of(
            "status", DocumentationStatus.DRAFT,
            "timestamp", doc.getCreatedAt(),
            "user", doc.getAuthor().getUsername(),
            "comment", "Document created"
        ));
        
        // Add a couple simulated transitions based on current status
        if (doc.getStatus() == DocumentationStatus.REVIEW || 
            doc.getStatus() == DocumentationStatus.PUBLISHED || 
            doc.getStatus() == DocumentationStatus.ARCHIVED) {
            
            history.add(Map.of(
                "status", DocumentationStatus.REVIEW,
                "timestamp", doc.getLastUpdatedAt().minusDays(2),
                "user", doc.getAuthor().getUsername(),
                "comment", "Submitted for review"
            ));
        }
        
        if (doc.getStatus() == DocumentationStatus.PUBLISHED || 
            doc.getStatus() == DocumentationStatus.ARCHIVED) {
            
            history.add(Map.of(
                "status", DocumentationStatus.PUBLISHED,
                "timestamp", doc.getLastUpdatedAt(),
                "user", "moderator1",
                "comment", "Approved and published"
            ));
        }
        
        workflow.put("history", history);
        
        // Available next states
        List<String> availableTransitions = new ArrayList<>();
        switch (doc.getStatus()) {
            case DRAFT:
                availableTransitions.add(DocumentationStatus.REVIEW.name());
                break;
            case REVIEW:
                availableTransitions.add(DocumentationStatus.PUBLISHED.name());
                availableTransitions.add(DocumentationStatus.DRAFT.name());
                break;
            case PUBLISHED:
                availableTransitions.add(DocumentationStatus.ARCHIVED.name());
                availableTransitions.add(DocumentationStatus.DRAFT.name());
                break;
            case ARCHIVED:
                availableTransitions.add(DocumentationStatus.DRAFT.name());
                break;
        }
        workflow.put("availableTransitions", availableTransitions);
        
        return ResponseEntity.ok(new ApiResponse(true, "Workflow information retrieved", workflow));
    }

    @GetMapping("/recent-activity")
    public ResponseEntity<ApiResponse> getRecentDocumentationActivity() {
        Map<String, Object> activity = new HashMap<>();
        
        // Recent status changes (simulated for now)
        List<Map<String, Object>> statusChanges = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> change = new HashMap<>();
            change.put("documentId", UUID.randomUUID());
            change.put("documentTitle", "Sample Document " + (i + 1));
            change.put("fromStatus", DocumentationStatus.DRAFT.name());
            change.put("toStatus", DocumentationStatus.REVIEW.name());
            change.put("changedBy", "user" + (i % 3 + 1));
            change.put("timestamp", LocalDateTime.now().minusDays(i));
            statusChanges.add(change);
        }
        activity.put("statusChanges", statusChanges);
        
        // Recent edits (simulated)
        List<Map<String, Object>> recentEdits = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map<String, Object> edit = new HashMap<>();
            edit.put("documentId", UUID.randomUUID());
            edit.put("documentTitle", "Edited Document " + (i + 1));
            edit.put("editedBy", "editor" + (i % 3 + 1));
            edit.put("timestamp", LocalDateTime.now().minusHours(i * 2));
            edit.put("changeDescription", "Updated content in section " + (i + 1));
            recentEdits.add(edit);
        }
        activity.put("recentEdits", recentEdits);
        
        // Recent views
        List<Map<String, Object>> recentViews = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Map<String, Object> view = new HashMap<>();
            view.put("documentId", UUID.randomUUID());
            view.put("documentTitle", "Viewed Document " + (i + 1));
            view.put("viewedBy", "viewer" + (i % 5 + 1));
            view.put("timestamp", LocalDateTime.now().minusMinutes(i * 30));
            recentViews.add(view);
        }
        activity.put("recentViews", recentViews);
        
        return ResponseEntity.ok(new ApiResponse(true, "Recent activity retrieved", activity));
    }

    @GetMapping("/statistics/summary")
    public ResponseEntity<ApiResponse> getDocumentationStatisticsSummary() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Document status distribution
        Map<String, Long> statusCounts = documentationService.getDocumentStatusDistribution();
        statistics.put("statusDistribution", statusCounts);
        
        // Technology distribution
        Map<String, Long> techDistribution = documentationService.getDocumentTechnologyDistribution();
        statistics.put("technologyDistribution", techDistribution);
        
        // Top viewed documents
        Page<DocumentationDTO> topViewed = documentationService.getMostViewedDocumentation(PageRequest.of(0, 5));
        statistics.put("topViewed", topViewed.getContent());
        
        // Activity timeline (last 7 days)
        List<Map<String, Object>> timeline = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 7; i++) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", LocalDateTime.now().minusDays(i).toLocalDate().toString());
            day.put("created", random.nextInt(10));
            day.put("published", random.nextInt(6));
            day.put("viewed", 10 + random.nextInt(20));
            timeline.add(day);
        }
        statistics.put("activityTimeline", timeline);
        
        return ResponseEntity.ok(new ApiResponse(true, "Documentation statistics retrieved", statistics));
    }

    @GetMapping("/document-details/{docId}")
    public ResponseEntity<ApiResponse> getDocumentDetails(@PathVariable UUID docId) {
        DocumentationDTO doc = documentationService.getDocumentationById(docId);
        List<DocumentationSectionDTO> sections = documentationService.getDocumentationSections(docId);
        
        // Get document status workflow data
        Map<String, Object> workflowData = getWorkflowData(doc.getStatus());
        
        // Build response with all document details
        Map<String, Object> details = new HashMap<>();
        details.put("document", doc);
        details.put("sections", sections);
        details.put("workflow", workflowData);
        
        // Add some analytics
        details.put("analytics", Map.of(
            "wordCount", countWords(doc.getContent()),
            "readTime", estimateReadTime(doc.getContent()),
            "lastViewed", LocalDateTime.now().minusHours(3),
            "viewCount", doc.getViews() != null ? doc.getViews() : 0
        ));
        
        return ResponseEntity.ok(new ApiResponse(true, "Document details retrieved", details));
    }

    private Map<String, Object> getWorkflowData(DocumentationStatus status) {
        Map<String, Object> workflow = new HashMap<>();
        workflow.put("currentStatus", status);
        
        // Define available transitions based on current status
        List<String> availableTransitions = new ArrayList<>();
        switch (status) {
            case DRAFT:
                availableTransitions.add(DocumentationStatus.REVIEW.name());
                break;
            case REVIEW:
                availableTransitions.add(DocumentationStatus.PUBLISHED.name());
                availableTransitions.add(DocumentationStatus.DRAFT.name());
                break;
            case PUBLISHED:
                availableTransitions.add(DocumentationStatus.ARCHIVED.name());
                availableTransitions.add(DocumentationStatus.DRAFT.name());
                break;
            case ARCHIVED:
                availableTransitions.add(DocumentationStatus.DRAFT.name());
                break;
            default:
                break;
        }
        workflow.put("availableTransitions", availableTransitions);
        
        // Add simple metrics
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("timeInCurrentStatus", "2 days");
        metrics.put("reviewers", List.of("admin", "moderator1"));
        metrics.put("lastStatusChange", LocalDateTime.now().minusDays(2));
        
        workflow.put("metrics", metrics);
        
        return workflow;
    }

    private int countWords(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        // Remove HTML tags
        String plainText = content.replaceAll("<[^>]*>", "");
        // Count words
        return plainText.split("\\s+").length;
    }

    private String estimateReadTime(String content) {
        int wordCount = countWords(content);
        // Average reading speed: 200-250 words per minute
        int minutes = wordCount / 225;
        return minutes == 0 ? "Less than a minute" : minutes + " min read";
    }
}
