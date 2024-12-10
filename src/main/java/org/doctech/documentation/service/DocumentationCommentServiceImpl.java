package org.doctech.documentation.service;

import lombok.RequiredArgsConstructor;
import org.doctech.common.exception.CommentNotFoundException;
import org.doctech.common.exception.DocumentationNotFoundException;
import org.doctech.common.exception.UserNotFoundException;
import org.doctech.common.utils.ValidationUtils;
import org.doctech.documentation.dto.DocumentationCommentDTO;
import org.doctech.documentation.mapper.DocumentationCommentMapper;
import org.doctech.documentation.model.Documentation;
import org.doctech.documentation.model.DocumentationComment;
import org.doctech.documentation.repository.DocumentationCommentRepository;
import org.doctech.documentation.repository.DocumentationRepository;
import org.doctech.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentationCommentServiceImpl implements DocumentationCommentService {

    private final DocumentationCommentRepository commentRepository;
    private final DocumentationRepository documentationRepository;
    private final UserRepository userRepository;
    private final DocumentationCommentMapper commentMapper;

    @Override
    public DocumentationCommentDTO createComment(UUID documentationId, DocumentationCommentDTO commentDTO) {
        ValidationUtils.validate(commentDTO);

        Documentation documentation = documentationRepository.findById(documentationId)
                .orElseThrow(() -> new DocumentationNotFoundException("Documentation not found with id: " + documentationId));

        if (!userRepository.existsById(commentDTO.getAuthorId())) {
            throw new UserNotFoundException("Author not found with id: " + commentDTO.getAuthorId());
        }

        DocumentationComment comment = commentMapper.toEntity(commentDTO);
        comment.setDocumentation(documentation);
        comment.setAuthor(userRepository.getReferenceById(commentDTO.getAuthorId()));

        DocumentationComment savedComment = commentRepository.save(comment);
        return commentMapper.toDTO(savedComment);
    }

    @Override
    public DocumentationCommentDTO updateComment(UUID id, DocumentationCommentDTO commentDTO) {
        ValidationUtils.validate(commentDTO);

        DocumentationComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + id));

        // Only update the content
        comment.setContent(commentDTO.getContent());

        DocumentationComment updatedComment = commentRepository.save(comment);
        return commentMapper.toDTO(updatedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentationCommentDTO getCommentById(UUID id) {
        DocumentationComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + id));
        return commentMapper.toDTO(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentationCommentDTO> getCommentsByDocumentationId(UUID documentationId) {
        if (!documentationRepository.existsById(documentationId)) {
            throw new DocumentationNotFoundException("Documentation not found with id: " + documentationId);
        }

        return commentRepository.findByDocumentationId(documentationId).stream()
                .map(commentMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DocumentationCommentDTO> getCommentsByAuthor(UUID authorId, Pageable pageable) {
        if (!userRepository.existsById(authorId)) {
            throw new UserNotFoundException("Author not found with id: " + authorId);
        }

        return commentRepository.findByAuthorId(authorId, pageable)
                .map(commentMapper::toDTO);
    }

    @Override
    public void deleteComment(UUID id) {
        if (!commentRepository.existsById(id)) {
            throw new CommentNotFoundException("Comment not found with id: " + id);
        }
        commentRepository.deleteById(id);
    }
}