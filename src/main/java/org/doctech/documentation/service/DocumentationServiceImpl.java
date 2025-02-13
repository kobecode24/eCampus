package org.doctech.documentation.service;

import lombok.RequiredArgsConstructor;
import org.doctech.blog.model.Blog;
import org.doctech.common.exception.DocumentationNotFoundException;
import org.doctech.common.exception.UserNotFoundException;
import org.doctech.common.utils.ValidationUtils;
import org.doctech.documentation.dto.DocumentationDTO;
import org.doctech.documentation.mapper.DocumentationMapper;
import org.doctech.documentation.model.Documentation;
import org.doctech.documentation.model.TechnologyType;
import org.doctech.documentation.repository.DocumentationRepository;
import org.doctech.security.model.SecurityUser;
import org.doctech.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("documentationService")
@RequiredArgsConstructor
@Transactional
public class DocumentationServiceImpl implements DocumentationService {

    private final DocumentationRepository documentationRepository;
    private final UserRepository userRepository;
    private final DocumentationMapper documentationMapper;

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
        return documentationMapper.toDTO(documentation);
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
    public DocumentationDTO incrementViews(UUID id) {
        Documentation documentation = findDocumentationById(id);
        documentation.incrementViews();
        Documentation savedDoc = documentationRepository.save(documentation);
        return documentationMapper.toDTO(savedDoc);
    }

    @Override
    public void deleteDocumentation(UUID id) {
        if (!documentationRepository.existsById(id)) {
            throw new DocumentationNotFoundException("Documentation not found with id: " + id);
        }
        documentationRepository.deleteById(id);
    }

    // Helper Methods
    private Documentation findDocumentationById(UUID id) {
        return documentationRepository.findById(id)
                .orElseThrow(() -> new DocumentationNotFoundException("Documentation not found with id: " + id));
    }
}