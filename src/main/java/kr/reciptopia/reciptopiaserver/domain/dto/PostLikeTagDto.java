package kr.reciptopia.reciptopiaserver.domain.dto;

import static kr.reciptopia.reciptopiaserver.domain.dto.CollectorHelper.byListValueLinkedHashMapWithKey;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.PostLikeTag;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Streamable;

public interface PostLikeTagDto {

    interface Bulk {

        record Result(
            Map<Long, List<PostLikeTagDto.Result>> postLikeTags
        ) {

            @Builder
            public Result(
                @NotEmpty
                @Singular
                    Map<Long, List<PostLikeTagDto.Result>> postLikeTags
            ) {
                this.postLikeTags = postLikeTags;
            }

            public interface GroupBy {

                interface Id {

                    static Result of(Page<PostLikeTag> postLikeTags) {
                        return Result.builder()
                            .postLikeTags(getResultMapFromPageWithKey(postLikeTags,
                                PostLikeTagDto.Result::id))
                            .build();
                    }
                }

                interface OwnerId {

                    static Result of(Page<PostLikeTag> postLikeTags) {
                        return Result.builder()
                            .postLikeTags(getResultMapFromPageWithKey(postLikeTags,
                                PostLikeTagDto.Result::ownerId))
                            .build();
                    }
                }
            }

            private static Map<Long, List<PostLikeTagDto.Result>> getResultMapFromPageWithKey(
                Page<PostLikeTag> postLikeTags,
                Function<PostLikeTagDto.Result, Long> getKey) {
                return postLikeTags.stream()
                    .map(PostLikeTagDto.Result::of)
                    .collect(byListValueLinkedHashMapWithKey(getKey));
            }
        }
    }

    @With
    record Create(
        @NotNull Long ownerId,
        @NotNull Long postId) {

        @Builder
        public Create {
        }

        public PostLikeTag asEntity(
            Function<? super PostLikeTag, ? extends PostLikeTag> initialize) {
            return initialize.apply(PostLikeTag.builder()
                .build());
        }

        public PostLikeTag asEntity() {
            return asEntity(noInit());
        }

        private <T> Function<? super T, ? extends T> noInit() {
            return (arg) -> arg;
        }
    }

    @With
    record Result(
        @NotNull Long id,
        @NotNull Long ownerId,
        @NotNull Long postId
    ) {

        @Builder
        public Result {
        }

        public static Result of(PostLikeTag entity) {
            return Result.builder()
                .id(entity.getId())
                .ownerId(entity.getOwner().getId())
                .postId(entity.getPost().getId())
                .build();
        }

        public static List<Result> of(Streamable<PostLikeTag> entities) {
            return entities.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }
}
