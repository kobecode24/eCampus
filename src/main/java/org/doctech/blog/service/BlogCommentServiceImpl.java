package org.doctech.blog.service;

import lombok.RequiredArgsConstructor;
import org.doctech.blog.dto.BlogCommentDTO;
import org.doctech.blog.mapper.BlogCommentMapper;
import org.doctech.blog.model.Blog;
import org.doctech.blog.model.BlogComment;
import org.doctech.blog.repository.BlogCommentRepository;
import org.doctech.blog.repository.BlogRepository;
import org.doctech.common.exception.BlogCommentNotFoundException;
import org.doctech.common.exception.BlogNotFoundException;
import org.doctech.common.exception.UnauthorizedCommentActionException;
import org.doctech.common.exception.UserNotFoundException;
import org.doctech.common.utils.ValidationUtils;
import org.doctech.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogCommentServiceImpl implements BlogCommentService {

    private final BlogCommentRepository commentRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final BlogCommentMapper commentMapper;

    @Override
    public BlogCommentDTO createComment(UUID blogId, BlogCommentDTO commentDTO) {
        ValidationUtils.validate(commentDTO);

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + blogId));

        if (!blog.isPublished()) {
            throw new IllegalStateException("Cannot comment on an unpublished blog");
        }

        if (!userRepository.existsById(commentDTO.getAuthorId())) {
            throw new UserNotFoundException("User not found with id: " + commentDTO.getAuthorId());
        }

        BlogComment comment = commentMapper.toEntity(commentDTO);
        comment.setBlog(blog);
        comment.setAuthor(userRepository.getReferenceById(commentDTO.getAuthorId()));

        BlogComment savedComment = commentRepository.save(comment);
        return commentMapper.toDTO(savedComment);
    }

    @Override
    public BlogCommentDTO updateComment(UUID id, BlogCommentDTO commentDTO) {
        BlogComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BlogCommentNotFoundException("Comment not found with id: " + id));

        validateCommentAuthor(comment, commentDTO.getAuthorId());

        comment.setContent(commentDTO.getContent());
        BlogComment updatedComment = commentRepository.save(comment);
        return commentMapper.toDTO(updatedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogCommentDTO getCommentById(UUID id) {
        BlogComment comment = commentRepository.findById(id)
                .orElseThrow(() -> new BlogCommentNotFoundException("Comment not found with id: " + id));
        return commentMapper.toDTO(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogCommentDTO> getBlogComments(UUID blogId, Pageable pageable) {
        if (!blogRepository.existsById(blogId)) {
            throw new BlogNotFoundException("Blog not found with id: " + blogId);
        }

        return commentRepository.findRecentComments(blogId, pageable)
                .map(commentMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogCommentDTO> getUserComments(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        return commentRepository.findByAuthorId(userId, pageable)
                .map(commentMapper::toDTO);
    }

    @Override
    public void deleteComment(UUID id) {
        if (!commentRepository.existsById(id)) {
            throw new BlogCommentNotFoundException("Comment not found with id: " + id);
        }
        commentRepository.deleteById(id);
    }

    private void validateCommentAuthor(BlogComment comment, UUID authorId) {
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new UnauthorizedCommentActionException("User is not authorized to modify this comment");
        }
    }
}