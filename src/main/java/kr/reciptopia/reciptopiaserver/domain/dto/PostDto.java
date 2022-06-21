package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Streamable;

public interface PostDto {

    interface Bulk {

        @With
        record Result(
            @NotEmpty Map<Long, PostDto.Result> posts) {

            @Builder
            public Result(
                @Singular
                Map<Long, PostDto.Result> posts
            ) {
                this.posts = posts;
            }

            public static Result of(Page<Post> posts) {
                return Result.builder()
                    .posts((Map<? extends Long, ? extends PostDto.Result>) posts.stream()
                        .map(PostDto.Result::of)
                        .collect(
                            Collectors.toMap(
                                PostDto.Result::id,
                                result -> result,
                                (x, y) -> y,
                                LinkedHashMap::new)))
                    .build();
            }
        }

        @With
        record ResultWithCommentAndLikeTagCount(
            @NotEmpty Map<Long, PostDto.ResultWithCommentAndLikeTagCount> postWithCommentAndLikeTagCounts) {

            @Builder
            public ResultWithCommentAndLikeTagCount(
                @Singular
                Map<Long, PostDto.ResultWithCommentAndLikeTagCount> postWithCommentAndLikeTagCounts
            ) {
                this.postWithCommentAndLikeTagCounts = postWithCommentAndLikeTagCounts;
            }

            public static ResultWithCommentAndLikeTagCount of(Page<Post> posts,
                Function<Long, Integer> commentCount, Function<Long, Integer> likeTagCount) {
                return ResultWithCommentAndLikeTagCount.builder()
                    .postWithCommentAndLikeTagCounts(
                        (Map<? extends Long, ? extends PostDto.ResultWithCommentAndLikeTagCount>) posts.stream()
                            .map(post -> PostDto.ResultWithCommentAndLikeTagCount.of(post,
                                commentCount.apply(post.getId()), likeTagCount.apply(post.getId())))
                            .collect(
                                Collectors.toMap(
                                    resultWithCommentAndLikeTagCount -> resultWithCommentAndLikeTagCount.post().id,
                                    result -> result,
                                    (x, y) -> y,
                                    LinkedHashMap::new)))
                    .build();
            }
        }
    }

    @With
    record Create(
        @NotNull Long ownerId,
        @NotBlank @Size(min = 1, max = 30, message = "title은 1 ~ 30자 이여야 합니다!") String title,
        String content, List<String> pictureUrls) {

        @Builder
        public Create(
            Long ownerId,
            String title,
            String content,
            @Singular
                List<String> pictureUrls) {
            this.ownerId = ownerId;
            this.title = title;
            this.content = content;
            this.pictureUrls = pictureUrls;
        }

        public Post asEntity(
            Function<? super Post, ? extends Post> initialize) {
            return initialize.apply(Post.builder()
                .title(title)
                .content(content)
                .pictureUrls(pictureUrls)
                .build());
        }

        public Post asEntity() {
            return asEntity(noInit());
        }

        private <T> Function<? super T, ? extends T> noInit() {
            return (arg) -> arg;
        }
    }

    @With
    record Update(
        @Size(min = 1, max = 30, message = "title은 1 ~ 30자 이여야 합니다!") String title,
        String content, List<String> pictureUrls) {

        @Builder
        public Update(
            String title,

            String content,

            @Singular
            List<String> pictureUrls) {
            this.title = title;
            this.content = content;
            this.pictureUrls = pictureUrls;
        }
    }

    @With
    record ResultWithCommentAndLikeTagCount(Result post, int commentCount, int likeTagCount) {

        @Builder
        public ResultWithCommentAndLikeTagCount {
        }

        public static ResultWithCommentAndLikeTagCount of(Post post, int commentCount,
            int likeTagCount) {
            return ResultWithCommentAndLikeTagCount.builder()
                .post(Result.of(post))
                .commentCount(commentCount)
                .likeTagCount(likeTagCount)
                .build();
        }
    }

    @With
    record Result(
        @NotNull Long id,
        @NotNull Long ownerId,
        @NotBlank @Size(min = 1, max = 30, message = "title은 1 ~ 30자 이여야 합니다!") String title,
        String content,
        List<String> pictureUrls,
        Long views) {

        @Builder
        public Result {

        }

        public static Result of(Post entity) {
            return Result.builder()
                .id(entity.getId())
                .ownerId(entity.getOwner().getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .pictureUrls(entity.getPictureUrls())
                .views(entity.getViews())
                .build();
        }

        public static List<Result> of(Streamable<Post> entities) {
            return entities.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }
}
