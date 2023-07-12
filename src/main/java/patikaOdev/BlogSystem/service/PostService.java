package patikaOdev.BlogSystem.service;

import org.springframework.stereotype.Service;

import org.springframework.util.CollectionUtils;
import patikaOdev.BlogSystem.dataAccess.CommentRepository;
import patikaOdev.BlogSystem.dataAccess.PostRepository;
import patikaOdev.BlogSystem.dataAccess.UserRepository;
import patikaOdev.BlogSystem.dto.GetAllPostDto;

import patikaOdev.BlogSystem.dto.requests.AddPostRequest;
import patikaOdev.BlogSystem.dto.requests.UpdatePostRequest;
import patikaOdev.BlogSystem.dto.responses.GetAllPostResponse;

import patikaOdev.BlogSystem.entities.Comment;
import patikaOdev.BlogSystem.entities.Post;

import patikaOdev.BlogSystem.exception.BusinessException;
import patikaOdev.BlogSystem.mapper.ModelMapperService;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;

    private final ModelMapperService modelMapperService;

    private final CommentRepository commentRepository;



    public PostService(PostRepository postRepository, ModelMapperService modelMapperService,
                       UserRepository userRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.modelMapperService = modelMapperService;
        this.commentRepository = commentRepository;
    }

    public GetAllPostResponse getAllPosts() {
        GetAllPostResponse response = new GetAllPostResponse();
        List<GetAllPostDto> dtos = postRepository.findAll()
                .stream()
                .filter(Objects::nonNull)
                .map(this::convertPostToGetAllPostsDto)
                .collect(Collectors.toList());

        response.setGetAllPostDto(dtos);
        response.setResultCode("1");
        response.setResultDescription("Success");

        return response;
    }

    public Post saveOnePost(AddPostRequest newPost) {
        Post post = this.modelMapperService.forRequest().map(newPost, Post.class);
        post.setCreationDate(new Date());
        return postRepository.save(post);
    }

    public void deleteOnePostById(Long postId) {
        Post post = postRepository.findById(postId).orElse(null);
        if (!CollectionUtils.isEmpty(post.getComments())) {
            throw new BusinessException("Post cannot be deleted while the post has comments.");
        }
        this.postRepository.deleteById(postId);
    }

    public Post updateOnePost(Long postId, UpdatePostRequest updatePostRequest) {
        Post post = postRepository.findById(postId).orElse(null);
        if (Objects.nonNull(post)) {
            post.setContent(updatePostRequest.getContent());
            post.setTitle(updatePostRequest.getTitle());
            post.setViewCount(updatePostRequest.getViewCount());
            post.setIsPublished(updatePostRequest.getIsPublished());
            postRepository.save(post);
            return post;
        }

        throw new BusinessException("Post could not found");

    }

    private GetAllPostDto convertPostToGetAllPostsDto(Post post) {
        GetAllPostDto getAllPostsDto = new GetAllPostDto();
        getAllPostsDto.setCategoryId(post.getCategory().getCategoryId());
        getAllPostsDto.setUserId(post.getUser().getUserId());
        getAllPostsDto.setPostId(post.getPostId());
        getAllPostsDto.setTitle(post.getTitle());
        getAllPostsDto.setContent(post.getContent());
        getAllPostsDto.setViewCount(post.getViewCount());
        getAllPostsDto.setIsPublished(post.getIsPublished());
        getAllPostsDto.setCreationDate(post.getCreationDate());
        return getAllPostsDto;

    }
}