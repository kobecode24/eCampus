package org.doctech.documentation.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.DocumentationNotFoundException;
import org.doctech.common.exception.UserNotFoundException;
import org.doctech.common.utils.ValidationUtils;
import org.doctech.documentation.dto.DocumentationDTO;
import org.doctech.documentation.dto.DocumentationSectionDTO;
import org.doctech.documentation.mapper.DocumentationMapper;
import org.doctech.documentation.model.Documentation;
import org.doctech.documentation.model.DocumentationSection;
import org.doctech.documentation.model.DocumentationStatus;
import org.doctech.documentation.model.TechnologyType;
import org.doctech.documentation.repository.DocumentationRepository;
import org.doctech.documentation.repository.DocumentationSectionRepository;
import org.doctech.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("documentationService")
@RequiredArgsConstructor
@Transactional
public class DocumentationServiceImpl implements DocumentationService {

    private final DocumentationRepository documentationRepository;
    private final UserRepository userRepository;
    private final DocumentationMapper documentationMapper;
    private final DocumentationSectionRepository sectionRepository;

    @Override
    public DocumentationDTO createDocumentation(DocumentationDTO documentationDTO) {
        ValidationUtils.validate(documentationDTO);

        if (!userRepository.existsById(documentationDTO.getAuthorId())) {
            throw new UserNotFoundException("Author not found with id: " + documentationDTO.getAuthorId());
        }

        Documentation documentation = documentationMapper.toEntity(documentationDTO);
        documentation.setAuthor(userRepository.getReferenceById(documentationDTO.getAuthorId()));

        Documentation savedDocumentation = documentationRepository.save(documentation);
        return documentationMapper.toDTO(savedDocumentation);
    }

    @Override
    public DocumentationDTO updateDocumentation(UUID id, DocumentationDTO documentationDTO) {
        ValidationUtils.validate(documentationDTO);

        Documentation documentation = documentationRepository.findById(id)
                .orElseThrow(() -> new DocumentationNotFoundException("Documentation not found with id: " + id));

        documentation.setTitle(documentationDTO.getTitle());
        documentation.setContent(documentationDTO.getContent());
        documentation.setTags(documentationDTO.getTags());
        documentation.setTechnology(documentationDTO.getTechnology());

        Documentation updatedDocumentation = documentationRepository.save(documentation);
        return documentationMapper.toDTO(updatedDocumentation);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentationDTO getDocumentationById(UUID id) {
        Documentation documentation = documentationRepository.findByIdWithComments(id)
                .orElseThrow(() -> new DocumentationNotFoundException("Documentation not found with id: " + id));
        
        DocumentationDTO dto = documentationMapper.toDTO(documentation);
        
        // Add sections to the DTO
        List<DocumentationSectionDTO> sections = getDocumentationSections(id);
        dto.setSections(sections);
        
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentationDTO> getAllDocumentation(Pageable pageable) {
        return documentationRepository.findAll(pageable)
                .map(documentationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentationDTO> getDocumentationByTechnology(TechnologyType technology, Pageable pageable) {
        return documentationRepository.findByTechnology(technology, pageable)
                .map(documentationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentationDTO> getDocumentationByTag(String tag, Pageable pageable) {
        return documentationRepository.findByTag(tag, pageable)
                .map(documentationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentationDTO> getDocumentationByAuthor(UUID authorId, Pageable pageable) {
        if (!userRepository.existsById(authorId)) {
            throw new UserNotFoundException("Author not found with id: " + authorId);
        }
        return documentationRepository.findByAuthorId(authorId, pageable)
                .map(documentationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentationDTO> getMostViewedDocumentation(Pageable pageable) {
        return documentationRepository.findMostViewed(pageable)
                .map(documentationMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentationDTO> searchDocumentation(String query, Pageable pageable) {
        return documentationRepository.search(query, pageable)
                .map(documentationMapper::toDTO);
    }

    @Override
    @Transactional
    public void incrementViews(UUID id) {
        Documentation documentation = findDocumentationById(id);
        documentation.incrementViews();
        Documentation savedDoc = documentationRepository.save(documentation);
        documentationMapper.toDTO(savedDoc);
    }

    @Override
    public void deleteDocumentation(UUID id) {
        if (!documentationRepository.existsById(id)) {
            throw new DocumentationNotFoundException("Documentation not found with id: " + id);
        }
        documentationRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentationSectionDTO> getDocumentationSections(UUID docId) {
        if (!documentationRepository.existsById(docId)) {
            throw new DocumentationNotFoundException("Documentation not found with id: " + docId);
        }
        
        return sectionRepository.findByDocumentationIdOrderByOrderIndex(docId)
                .stream()
                .map(this::mapSectionToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DocumentationSectionDTO createSection(UUID docId, DocumentationSectionDTO sectionDTO) {
        Documentation documentation = findDocumentationById(docId);
        
        DocumentationSection section = new DocumentationSection();
        section.setDocumentation(documentation);
        section.setTitle(sectionDTO.getTitle());
        section.setContent(sectionDTO.getContent());
        section.setSectionId(sectionDTO.getSectionId() != null ? 
                sectionDTO.getSectionId() : 
                generateSectionId(sectionDTO.getTitle()));
        section.setOrderIndex(sectionDTO.getOrderIndex() != null ? 
                sectionDTO.getOrderIndex() : 
                getNextOrderIndex(docId));
        
        DocumentationSection savedSection = sectionRepository.save(section);
        return mapSectionToDTO(savedSection);
    }

    @Override
    @Transactional
    public DocumentationSectionDTO updateSection(UUID sectionId, DocumentationSectionDTO sectionDTO) {
        DocumentationSection section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new DocumentationNotFoundException("Section not found with id: " + sectionId));
        
        section.setTitle(sectionDTO.getTitle());
        section.setContent(sectionDTO.getContent());
        if (sectionDTO.getSectionId() != null) {
            section.setSectionId(sectionDTO.getSectionId());
        }
        if (sectionDTO.getOrderIndex() != null) {
            section.setOrderIndex(sectionDTO.getOrderIndex());
        }
        
        DocumentationSection updatedSection = sectionRepository.save(section);
        return mapSectionToDTO(updatedSection);
    }

    @Override
    @Transactional
    public void updateDocumentationStatus(UUID docId, DocumentationStatus status) {
        Documentation documentation = findDocumentationById(docId);
        documentation.setStatus(status);
        documentationRepository.save(documentation);
    }

    @Override
    @Transactional
    public Documentation createDocumentationFromTemplate(String title, String description, TechnologyType tech, UUID authorId) {
        // Create the main documentation
        Documentation doc = Documentation.builder()
            .title(title)
            .content(description)
            .technology(tech)
            .status(DocumentationStatus.DRAFT)
            .build();
        
        doc.setAuthor(userRepository.getReferenceById(authorId));
        
        Documentation savedDoc = documentationRepository.save(doc);
        
        // Create template sections based on the technology type
        if (tech == TechnologyType.FRONTEND) {
            createSampleSection(savedDoc, "Introduction",
                "<p>Welcome to the documentation for " + title + ". This guide provides a comprehensive overview of this frontend technology.</p>",
                "introduction", 0);
            
            createSampleSection(savedDoc, "Getting Started",
                "<p>To start using " + title + ", follow these steps:</p>" +
                "<ol><li>Install dependencies</li><li>Configure your project</li><li>Import the necessary modules</li></ol>",
                "getting-started", 1);
            
            createSampleSection(savedDoc, "Component Architecture",
                "<p>Understanding the component architecture is essential for effective development:</p>" +
                "<ul><li>Component lifecycle</li><li>State management</li><li>Props and data flow</li></ul>",
                "component-architecture", 2);
            
            createSampleSection(savedDoc, "Advanced Usage",
                "<p>For advanced scenarios, consider these patterns:</p>" +
                "<ul><li>Code splitting</li><li>Server-side rendering</li><li>Performance optimization</li></ul>",
                "advanced-usage", 3);
            
        } else if (tech == TechnologyType.BACKEND) {
            createSampleSection(savedDoc, "Introduction",
                "<p>This documentation covers " + title + ", a powerful backend solution for modern applications.</p>",
                "introduction", 0);
            
            createSampleSection(savedDoc, "Setup and Configuration",
                "<p>Configure your environment with these steps:</p>" +
                "<ol><li>Install the framework</li><li>Set up your database connection</li><li>Configure application properties</li></ol>",
                "setup-configuration", 1);
            
            createSampleSection(savedDoc, "Core Architecture",
                "<p>The architecture includes several key components:</p>" +
                "<ul><li>Controllers</li><li>Services</li><li>Repositories</li><li>Models</li></ul>",
                "core-architecture", 2);
            
            createSampleSection(savedDoc, "Data Access Layer",
                "<p>Learn how to interact with your database effectively:</p>" +
                "<ul><li>ORM setup</li><li>Query optimization</li><li>Transaction management</li></ul>",
                "data-access", 3);
            
        } else if (tech == TechnologyType.API) {
            createSampleSection(savedDoc, "API Overview",
                "<p>" + title + " provides robust API capabilities for your application integration needs.</p>",
                "api-overview", 0);
            
            createSampleSection(savedDoc, "Authentication",
                "<p>Secure your API with these authentication methods:</p>" +
                "<ul><li>API Keys</li><li>OAuth 2.0</li><li>JWT tokens</li></ul>",
                "authentication", 1);
            
            createSampleSection(savedDoc, "Endpoints",
                "<p>Key endpoints available in this API:</p>" +
                "<ul><li>User resources</li><li>Content resources</li><li>Analytics endpoints</li></ul>",
                "endpoints", 2);
            
            createSampleSection(savedDoc, "Rate Limiting and Quotas",
                "<p>Understanding usage limits:</p>" +
                "<ul><li>Request quotas</li><li>Rate limiting policies</li><li>Handling rate limit responses</li></ul>",
                "rate-limiting", 3);
            
        } else {
            // Generic template for other technology types
            createSampleSection(savedDoc, "Introduction",
                "<p>Welcome to the documentation for " + title + ". " + description + "</p>",
                "introduction", 0);
            
            createSampleSection(savedDoc, "Getting Started",
                "<p>To begin using this technology:</p>" +
                "<ol><li>Installation guide</li><li>Basic configuration</li><li>Hello world example</li></ol>",
                "getting-started", 1);
            
            createSampleSection(savedDoc, "Core Features",
                "<p>Explore the main capabilities:</p>" +
                "<ul><li>Feature 1</li><li>Feature 2</li><li>Feature 3</li></ul>",
                "core-features", 2);
            
            createSampleSection(savedDoc, "Advanced Topics",
                "<p>For advanced users:</p>" +
                "<ul><li>Performance optimization</li><li>Security considerations</li><li>Integration options</li></ul>",
                "advanced-topics", 3);
        }
        
        return savedDoc;
    }

    // Helper Methods
    private Documentation findDocumentationById(UUID id) {
        return documentationRepository.findById(id)
                .orElseThrow(() -> new DocumentationNotFoundException("Documentation not found with id: " + id));
    }

    private DocumentationSectionDTO mapSectionToDTO(DocumentationSection section) {
        return DocumentationSectionDTO.builder()
                .id(section.getId())
                .documentationId(section.getDocumentation().getId())
                .title(section.getTitle())
                .content(section.getContent())
                .orderIndex(section.getOrderIndex())
                .sectionId(section.getSectionId())
                .build();
    }

    private String generateSectionId(String title) {
        // Generate a slug for the section ID based on the title
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
    }

    private Integer getNextOrderIndex(UUID docId) {
        List<DocumentationSection> sections = sectionRepository.findByDocumentationIdOrderByOrderIndex(docId);
        return sections.isEmpty() ? 0 : sections.get(sections.size() - 1).getOrderIndex() + 1;
    }

    private void createSampleSection(Documentation doc, String title, String content, String sectionId, int order) {
        DocumentationSection section = DocumentationSection.builder()
                .documentation(doc)
                .title(title)
                .content(content)
                .sectionId(sectionId)
                .orderIndex(order)
                .build();

        sectionRepository.save(section);
    }

    // Add these implementations to your DocumentationServiceImpl class

    @Override
    public long countByStatus(DocumentationStatus status) {
        return documentationRepository.countByStatus(status);
    }

    @Override
    public List<DocumentationDTO> getDocumentationByStatus(DocumentationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Documentation> docs = documentationRepository.findByStatusOrderByLastUpdatedAtDesc(status, pageable);
        return docs.stream()
                .map(documentationMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getDocumentStatusDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        for (DocumentationStatus status : DocumentationStatus.values()) {
            distribution.put(status.name(), documentationRepository.countByStatus(status));
        }
        return distribution;
    }

    @Override
    public Map<String, Long> getDocumentTechnologyDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        for (TechnologyType type : TechnologyType.values()) {
            distribution.put(type.name(), documentationRepository.countByTechnology(type));
        }
        return distribution;
    }
}