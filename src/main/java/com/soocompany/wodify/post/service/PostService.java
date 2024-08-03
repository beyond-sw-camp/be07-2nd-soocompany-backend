package com.soocompany.wodify.post.service;
import com.soocompany.wodify.member.domain.Member;
import com.soocompany.wodify.member.repository.MemberRepository;
import com.soocompany.wodify.post.domain.Comment;
import com.soocompany.wodify.post.domain.Image;
import com.soocompany.wodify.post.domain.Post;
import com.soocompany.wodify.post.dto.*;
import com.soocompany.wodify.post.repository.CommentRepository;
import com.soocompany.wodify.post.repository.ImageRepository;
import com.soocompany.wodify.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.persistence.EntityNotFoundException;
import java.io.IOException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;
    private final ImageRepository imageRepository;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public Post postCreate(String email, PostSaveReqDto dto) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.error("postCreate() : Email 에 해당하는 member 가 없습니다.");
        Member member = memberRepository.findByEmailAndDelYn(email, "N")
            .orElseThrow(() -> new EntityNotFoundException("Email 에 해당하는 member 가 없습니다."));
        Post post = postRepository.save(dto.toEntity(member));
        try {
            for (MultipartFile file : dto.getFiles()) {
                byte[] bytes = file.getBytes();
                String fileName = post.getId() + "_" + file.getOriginalFilename();

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

                PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
                String s3Path = s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(fileName)).toExternalForm();
                Image image = Image.builder()
                        .member(member)
                        .post(post)
                        .fileName(fileName)
                        .s3Path(s3Path)
                        .build();

                imageRepository.save(image);
                post.getFiles().add(image);
            }
            postRepository.save(post);
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패");
        }
        return post;
    }

    public Page<PostListResDto> postListPage(Pageable pageable) {
        Page<Post> posts = postRepository.findAllByDelYn(pageable, "N");
        return posts.map(post -> new PostListResDto().listFromEntity(post));
    }

    public PostDetResDto postDetail(Long id) {
        log.error("postDetail() : 해당 id의 게시글을 찾을 수 없습니다.");
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("해당 id의 게시글을 찾을 수 없습니다."));
        if (post.getDelYn().equals("Y"))
            throw new IllegalArgumentException("삭제된 게시글 입니다.");
        return PostDetResDto.fromEntity(post);
    }

    public void postDelete(Long id, String email) {
        log.error("postDelete() : 해당 id의 게시글을 찾을 수 없습니다.");
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("해당 id의 게시글을 찾을 수 없습니다."));
        log.error("postDelete() : 삭제된 게시글 입니다.");
        if (post.getDelYn().equals("Y"))
            throw new IllegalArgumentException("삭제된 게시글 입니다.");
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.error("postDelete() : Email 에 해당하는 member 가 없습니다.");
        Member member = memberRepository.findByEmailAndDelYn(email, "N")
                .orElseThrow(() -> new EntityNotFoundException("Email 에 해당하는 member 가 없습니다."));
        log.error("postDelete() : 본인의 게시글이 아닙니다.");
        if (!post.getMember().getEmail().equals(email))
            throw new IllegalArgumentException("본인의 게시글이 아닙니다.");
        post.deletePost();
    }

    public PostDetResDto postUpdate(Long id, String email, PostUpdateReqDto postUpdateReqDto) {
        log.error("postUpdate() : 해당 id의 게시글을 찾을 수 없습니다.");
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("해당 id의 게시글을 찾을 수 없습니다."));
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.error("postUpdate() : 삭제된 게시글 입니다.");
        if (post.getDelYn().equals("Y"))
            throw new IllegalArgumentException("삭제된 게시글 입니다.");
        log.error("postUpdate() : Email 에 해당하는 member 가 없습니다.");
        Member member = memberRepository.findByEmailAndDelYn(email, "N")
                .orElseThrow(() -> new EntityNotFoundException("Email 에 해당하는 member 가 없습니다."));
        log.error("postUpdate() : 본인의 게시글이 아닙니다.");
        if (!post.getMember().getEmail().equals(email))
            throw new IllegalArgumentException("본인의 게시글이 아닙니다.");
        post.updatePost(postUpdateReqDto);
        Post savedPost = postRepository.save(post);
        return PostDetResDto.fromEntity(savedPost);
    }

    public void imageDelete(Long id, String email) {
        log.error("imageDelete() : 해당 id의 image 를 찾을 수 없습니다.");
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 id의 image 를 찾을 수 없습니다."));
        log.error("imageDelete() : 삭제된 image 입니다.");
        if (image.getDelYn().equals("Y"))
            throw new IllegalArgumentException("삭제된 image 입니다.");
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.error("imageDelete() : Email 에 해당하는 member 가 없습니다.");
        Member member = memberRepository.findByEmailAndDelYn(email, "N")
                .orElseThrow(() -> new EntityNotFoundException("Email 에 해당하는 member 가 없습니다."));
        log.error("imageDelete() : 본인의 image 가 아닙니다.");
        if (!image.getMember().getEmail().equals(email))
            throw new IllegalArgumentException("본인의 게시글이 아닙니다.");
        image.updateDelYn();
    }

    public Post commentCreate(Long postId, String email, CommentSaveReqDto commentSaveReqDto) {
        log.error("commentCreate() : 해당 id의 게시글을 찾을 수 없습니다.");
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("해당 id의 게시글을 찾을 수 없습니다."));
        log.error("commentCreate() : 삭제된 게시글 입니다.");
        if (post.getDelYn().equals("Y"))
            throw new IllegalArgumentException("삭제된 게시글 입니다.");
        log.error("commentCreate() : Email 에 해당하는 member 가 없습니다.");
        Member member = memberRepository.findByEmailAndDelYn(email, "N")
                .orElseThrow(() -> new EntityNotFoundException("Email 에 해당하는 member 가 없습니다."));
        Comment comment = commentSaveReqDto.toEntity(member, post);
        Comment savedComment = commentRepository.save(comment);
        post.getComments().add(savedComment);
        return postRepository.save(post);
    }

    public void commentDelete(Long id, String email) {
        log.error("commentDelete() : 해당 id의 댓글을 찾을 수 없습니다.");
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 id의 댓글을 찾을 수 없습니다."));
        log.error("commentDelete() : 삭제된 댓글 입니다.");
        if (comment.getDelYn().equals("Y"))
            throw new IllegalArgumentException("삭제된 게시글 입니다.");
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.error("commentDelete() : 해당 Email 의 member 가 없습니다.");
        Member member = memberRepository.findByEmailAndDelYn(email, "N")
                .orElseThrow(() -> new EntityNotFoundException("Email 에 해당하는 member 가 없습니다."));
        log.error("commentDelete() : 본인의 댓글이 아닙니다.");
        if (!comment.getMember().getEmail().equals(email))
            throw new IllegalArgumentException("본인의 댓글이 아닙니다.");
        comment.updateDelYn();
    }

    public PostDetResDto commentUpdate(Long id, String email, CommentUpdateReqDto commentUpdateReqDto) {
        log.error("commentUpdate() : 해당 id의 댓글을 찾을 수 없습니다.");
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("해당 id의 댓글을 찾을 수 없습니다."));
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.error("commentUpdate() : 삭제된 댓글 입니다.");
        if (comment.getDelYn().equals("Y"))
            throw new IllegalArgumentException("삭제된 댓글 입니다.");
        log.error("commentUpdate() : Email 에 해당하는 member 가 없습니다.");
        Member member = memberRepository.findByEmailAndDelYn(email, "N")
                .orElseThrow(() -> new EntityNotFoundException("Email 에 해당하는 member 가 없습니다."));
        log.error("commentUpdate() : 본인의 댓글이 아닙니다.");
        if (!comment.getMember().getEmail().equals(email))
            throw new IllegalArgumentException("본인의 댓글이 아닙니다.");
        comment.updateComment(commentUpdateReqDto);
        return PostDetResDto.fromEntity(comment.getPost());
    }
}