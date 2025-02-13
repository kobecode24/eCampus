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
import org.doctech.security.model.SecurityUser;
import org.doctech.user.model.User;
import org.doctech.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service("blogCommentService")
@RequiredArgsConstructor
@Transactional
public class BlogCommentServiceImpl implements BlogCommentService {

    private final BlogCommentRepository commentRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final BlogCommentMapper commentMapper;
    private final BlogCommentRepository blogCommentRepository;
    private final BlogCommentMapper blogCommentMapper;

    @Override
    public BlogCommentDTO createComment(UUID blogId, BlogCommentDTO commentDTO) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + blogId));
        
        User author = userRepository.findById(commentDTO.getAuthorId())
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + commentDTO.getAuthorId()));

        BlogComment comment = blogCommentMapper.toEntity(commentDTO);
        comment.setBlog(blog);
        comment.setAuthor(author);
        
        BlogComment savedComment = blogCommentRepository.save(comment);
        return blogCommentMapper.toDTO(savedComment);
    }

    @Override
    public BlogCommentDTO updateComment(UUID id, BlogCommentDTO commentDTO) {
        BlogComment comment = findCommentById(id);
        validateCommentOwnership(comment, commentDTO.getAuthorId());

        comment.setContent(commentDTO.getContent());
        BlogComment updatedComment = commentRepository.save(comment);
        return commentMapper.toDTO(updatedComment);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogCommentDTO getCommentById(UUID id) {
        return commentMapper.toDTO(findCommentById(id));
    }

    @Override
    public void deleteComment(UUID id) {
        BlogComment comment = findCommentById(id);
        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogCommentDTO> getCommentsByBlog(UUID blogId, Pageable pageable) {
        if (!blogRepository.existsById(blogId)) {
            throw new BlogNotFoundException("Blog not found with id: " + blogId);
        }
        return commentRepository.findByBlogId(blogId, pageable)
                .map(commentMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogCommentDTO> getCommentsByUser(UUID userId, Pageable pageable) {
        validateUserExists(userId);
        return commentRepository.findByAuthorId(userId, pageable)
                .map(commentMapper::toDTO);
    }

    @Override
    public boolean isCommentAuthor(UUID commentId, Object principal) {
        SecurityUser securityUser = (SecurityUser) principal;
        BlogComment comment = findCommentById(commentId);
        return comment.getAuthor().getId().equals(securityUser.getId());
    }

    @Override
    public boolean isCommentAuthorOrAdmin(UUID commentId, Object principal) {
        SecurityUser securityUser = (SecurityUser) principal;
        BlogComment comment = findCommentById(commentId);

        boolean isAuthor = comment.getAuthor().getId().equals(securityUser.getId());
        boolean isAdmin = securityUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        return isAuthor || isAdmin;
    }

    // Helper Methods
    private Blog findAndValidateBlog(UUID blogId) {
        return blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + blogId));
    }

    private void validateBlogIsPublished(Blog blog) {
        if (!blog.isPublished()) {
            throw new IllegalStateException("Cannot comment on an unpublished blog");
        }
    }

    private void validateCommentAuthor(UUID authorId) {
        if (!userRepository.existsById(authorId)) {
            throw new UserNotFoundException("User not found with id: " + authorId);
        }
    }

    private BlogComment findCommentById(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new BlogCommentNotFoundException("Comment not found with id: " + id));
    }

    private void validateCommentOwnership(BlogComment comment, UUID authorId) {
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new UnauthorizedCommentActionException("User is not authorized to modify this comment");
        }
    }

    private void validateUserExists(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }
    }

    private BlogComment createCommentEntity(BlogCommentDTO commentDTO, Blog blog) {
        BlogComment comment = commentMapper.toEntity(commentDTO);
        comment.setBlog(blog);
        comment.setAuthor(userRepository.getReferenceById(commentDTO.getAuthorId()));
        return comment;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogCommentDTO> getCommentsByBlogId(UUID blogId, Pageable pageable) {
        if (!blogRepository.existsById(blogId)) {
            throw new BlogNotFoundException("Blog not found with id: " + blogId);
        }

        // Using a dedicated query method in repository for better performance
        return commentRepository.findByBlogIdOrderByCreatedAtDesc(blogId, pageable)
                .map(commentMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogCommentDTO> getCommentsByAuthor(UUID authorId, Pageable pageable) {
        if (!userRepository.existsById(authorId)) {
            throw new UserNotFoundException("User not found with id: " + authorId);
        }

        // Using a dedicated query method in repository with proper ordering
        return commentRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable)
                .map(commentMapper::toDTO);
    }
}