package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.Post;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Streamable;

public interface PostDto {

    interface Bulk {

        @With
        record Result(Map<Long, PostDto.Result> posts) {

            @Builder
            public Result(
                @NotEmpty
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
            Map<Long, PostDto.ResultWithCommentAndLikeTagCount> postWithCommentAndLikeTagCounts) {

            @Builder
            public ResultWithCommentAndLikeTagCount(
                @NotEmpty
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
        Long ownerId, String title, String content) {

        @Builder
        public Create(
            @NotNull
                Long ownerId,

            @NotEmpty
                String title,

            String content) {
            this.ownerId = ownerId;
            this.title = title;
            this.content = content;
        }

        public Post asEntity(
            Function<? super Post, ? extends Post> initialize) {
            return initialize.apply(Post.builder()
                .title(title)
                .content(content)
                .build());
        }

        public Post asEntity() {
            return asEntity(noInit());
        }

        public Create withOwnerId(Long ownerId) {
            return this.ownerId != null && this.ownerId.equals(ownerId) ? this : Create.builder()
                .ownerId(ownerId)
                .title(title)
                .content(content)
                .build();
        }

        private <T> Function<? super T, ? extends T> noInit() {
            return (arg) -> arg;
        }
    }

    @With
    record Update(String title, String content) {

        @Builder
        public Update(
            String title,

            String content) {
            this.title = title;
            this.content = content;
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
        Long id, Long ownerId,
        String title, String content, Long views) {

        @Builder
        public Result(
            @NotNull
                Long id,

            @NotNull
                Long ownerId,

            @NotEmpty
                String title,

            String content,

            Long views) {
            this.id = id;
            this.ownerId = ownerId;
            this.title = title;
            this.content = content;
            this.views = views;
        }

        public static Result of(Post entity) {
            return Result.builder()
                .id(entity.getId())
                .ownerId(entity.getOwner().getId())
                .title(entity.getTitle())
                .content(entity.getContent())
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
