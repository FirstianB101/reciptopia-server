package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.model.Comment;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Streamable;

public interface CommentDto {

    interface Bulk {

        @With
        record Result(Map<Long, CommentDto.Result> comments) {

            @Builder
            public Result(
                @NotEmpty
                @Singular
                    Map<Long, CommentDto.Result> comments
            ) {
                this.comments = comments;
            }

            public static Result of(Page<Comment> comments) {
                return Result.builder()
                    .comments((Map<? extends Long, ? extends CommentDto.Result>) comments.stream()
                        .map(CommentDto.Result::of)
                        .collect(
                            Collectors.toMap(
                                CommentDto.Result::id,
                                result -> result,
                                (x, y) -> y,
                                LinkedHashMap::new)))
                    .build();
            }
        }
    }

    @With
    record Create(
        Long ownerId, Long postId, String content) {

        @Builder
        public Create(
            @NotNull
                Long ownerId,

            @NotNull
                Long postId,

            @NotBlank
            @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
                String content) {
            this.ownerId = ownerId;
            this.postId = postId;
            this.content = content;
        }

        public Comment asEntity() {
            return Comment.builder()
                .content(content)
                .build();
        }

    }

    @With
    record Update(String content) {

        @Builder
        public Update(
            @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
                String content) {
            this.content = content;
        }
    }

    @With
    record Result(
        Long id, Long ownerId, Long postId, String content) {

        @Builder
        public Result(
            @NotNull
                Long id,

            @NotNull
                Long ownerId,

            @NotNull
                Long postId,

            @NotBlank
            @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
                String content) {
            this.id = id;
            this.ownerId = ownerId;
            this.postId = postId;
            this.content = content;
        }

        public static Result of(Comment entity) {
            return Result.builder()
                .id(entity.getId())
                .ownerId(entity.getOwner().getId())
                .postId(entity.getPost().getId())
                .content(entity.getContent())
                .build();
        }

        public static List<Result> of(Streamable<Comment> entities) {
            return entities.stream()
                .map(comment -> of(comment))
                .collect(Collectors.toList());
        }
    }
}
