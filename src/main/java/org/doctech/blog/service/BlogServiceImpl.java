package org.doctech.blog.service;

import lombok.RequiredArgsConstructor;
import org.doctech.blog.dto.BlogDTO;
import org.doctech.blog.mapper.BlogMapper;
import org.doctech.blog.model.Blog;
import org.doctech.blog.repository.BlogRepository;
import org.doctech.blog.repository.BlogLikesRepository;
import org.doctech.common.exception.BlogNotFoundException;
import org.doctech.common.exception.UserNotFoundException;
import org.doctech.common.utils.ValidationUtils;
import org.doctech.security.model.SecurityUser;
import org.doctech.user.model.User;
import org.doctech.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final BlogMapper blogMapper;
    private final BlogLikesRepository blogLikesRepository;

    // Blog Creation and Management
    @Override
    public BlogDTO createBlog(BlogDTO blogDTO) {
        ValidationUtils.validate(blogDTO);

        User author = userRepository.findById(blogDTO.getAuthorId())
                .orElseThrow(() -> new UserNotFoundException("Author not found with id: " + blogDTO.getAuthorId()));

        Blog blog = blogMapper.toEntity(blogDTO);
        blog.setAuthor(author);


        Blog savedBlog = blogRepository.save(blog);
        return blogMapper.toDTO(savedBlog);
    }

    @Override
    public BlogDTO updateBlog(UUID id, BlogDTO blogDTO) {
        Blog blog = findBlogById(id);
        validateBlogUpdateEligibility(blog);
        updateBlogFields(blog, blogDTO);
        return blogMapper.toDTO(blogRepository.save(blog));
    }

    @Override
    public void deleteBlog(UUID id) {
        if (!blogRepository.existsById(id)) {
            throw new BlogNotFoundException("Blog not found with id: " + id);
        }
        blogRepository.deleteById(id);
    }

    // Blog Publishing
    @Override
    public BlogDTO publishBlog(UUID id) {
        Blog blog = findBlogById(id);
        validateAndPublishBlog(blog);
        return blogMapper.toDTO(blogRepository.save(blog));
    }

    // Blog Retrieval
    @Override
    @Transactional(readOnly = true)
    public BlogDTO getBlogById(UUID id, UUID currentUserId) {
        Blog blog = findBlogByIdWithComments(id);
        return enrichDTOWithLikeStatus(blog, currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogDTO> getAllBlogs(Pageable pageable, UUID currentUserId) {
        // Check if we're sorting by comments.size
        if (pageable.getSort().stream()
                .anyMatch(order -> order.getProperty().equals("comments.size"))) {
            
            // Create a new pageable without the sort
            Pageable pageableWithoutSort = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize()
            );
            
            // Use the custom query method for sorting by comment count
            return blogRepository.findAllOrderByCommentCountDesc(pageableWithoutSort)
                    .map(blog -> enrichDTOWithLikeStatus(blog, currentUserId));
        }
        
        // Use the default method for other sorts
        return blogRepository.findAll(pageable)
                .map(blog -> enrichDTOWithLikeStatus(blog, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogDTO> getPublishedBlogs(Pageable pageable, UUID currentUserId) {
        return blogRepository.findByPublishedTrue(pageable)
                .map(blog -> enrichDTOWithLikeStatus(blog, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogDTO> getBlogsByAuthor(UUID authorId, Pageable pageable) {
        validateAuthorExists(authorId);
        return blogRepository.findByAuthorId(authorId, pageable)
                .map(blogMapper::toDTO);
    }

    @Transactional
    @Override
    public BlogDTO toggleLike(UUID blogId, UUID userId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        boolean newLikeState = blog.toggleLike(user);
        Blog updatedBlog = blogRepository.save(blog);
        
        BlogDTO dto = blogMapper.toDTO(updatedBlog);
        dto.setHasLiked(newLikeState);
        
        return dto;
    }

    // Blog Search and Filtering
    @Override
    @Transactional(readOnly = true)
    public Page<BlogDTO> getBlogsByTag(String tag, Pageable pageable, UUID currentUserId) {
        return blogRepository.findByTag(tag, pageable)
                .map(blog -> enrichDTOWithLikeStatus(blog, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogDTO> searchBlogs(String query, Pageable pageable, UUID currentUserId) {
        return blogRepository.search(query, pageable)
                .map(blog -> enrichDTOWithLikeStatus(blog, currentUserId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BlogDTO> getMostPopularBlogs(Pageable pageable, UUID currentUserId) {
        return blogRepository.findAllByOrderByLikesDesc(pageable)
                .map(blog -> enrichDTOWithLikeStatus(blog, currentUserId));
    }

    // Authorization
    @Override
    public boolean isAuthorOrAdmin(UUID blogId, Object principal) {
        Blog blog = findBlogById(blogId);
        SecurityUser securityUser = (SecurityUser) principal;

        return blog.getAuthor().getId().equals(securityUser.getId()) ||
                securityUser.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    // Helper Methods
    private Blog findBlogById(UUID id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + id));
    }

    private Blog findBlogByIdWithComments(UUID id) {
        return blogRepository.findByIdWithComments(id)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with id: " + id));
    }

    private User findAndValidateAuthor(UUID authorId) {
        return userRepository.findById(authorId)
                .orElseThrow(() -> new UserNotFoundException("Author not found with id: " + authorId));
    }

    private User findAndValidateUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
    }

    private void validateAuthorExists(UUID authorId) {
        if (!userRepository.existsById(authorId)) {
            throw new UserNotFoundException("Author not found with id: " + authorId);
        }
    }

    private Blog createBlogEntity(BlogDTO blogDTO, User author) {
        Blog blog = blogMapper.toEntity(blogDTO);
        blog.setAuthor(author);
        blog.setPublished(false);
        return blog;
    }

    private void validateBlogUpdateEligibility(Blog blog) {
        if (blog.isPublished()) {
            throw new IllegalStateException("Published blogs cannot be updated");
        }
    }

    private void updateBlogFields(Blog blog, BlogDTO blogDTO) {
        blog.setTitle(blogDTO.getTitle());
        blog.setContent(blogDTO.getContent());
        blog.setTags(blogDTO.getTags());
        blog.setPointsCost(blogDTO.getPointsCost());
    }

    private void validateAndPublishBlog(Blog blog) {
        if (blog.isPublished()) {
            throw new IllegalStateException("Blog is already published");
        }
        blog.publish();
    }

    private boolean isAuthorOrAdminCheck(Blog blog, User user) {
        return blog.getAuthor().getId().equals(user.getId()) ||
                user.getRoles().stream()
                        .anyMatch(role -> role.getName().equals("ADMIN"));
    }

    // Helper method to set hasLiked field
    private BlogDTO enrichDTOWithLikeStatus(Blog blog, UUID userId) {
        BlogDTO dto = blogMapper.toDTO(blog);
        
        if (userId != null) {
            // Check if user has liked this blog using repository
            boolean hasLiked = blogLikesRepository.existsByBlogIdAndUserId(blog.getId(), userId);
            dto.setHasLiked(hasLiked);
        } else {
            dto.setHasLiked(false);
        }
        
        return dto;
    }
}
