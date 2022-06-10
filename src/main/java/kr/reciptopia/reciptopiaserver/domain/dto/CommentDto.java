package kr.reciptopia.reciptopiaserver.domain.dto;

import java.time.LocalDateTime;
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
        record Result(@NotEmpty Map<Long, CommentDto.Result> comments) {

            @Builder
            public Result(
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
        @NotNull Long ownerId,
        @NotNull Long postId,
        @NotBlank @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!") String content) {

        @Builder
        public Create {

        }

        public Comment asEntity() {
            return Comment.builder()
                .content(content)
                .build();
        }

    }

    @With
    record Update(@Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!") String content) {

        @Builder
        public Update {

        }
    }

    @With
    record Result(
        @NotNull Long id,
        @NotNull Long ownerId,
        @NotNull Long postId,
        @NotBlank @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!") String content,
        LocalDateTime createTime,
        @NotNull LocalDateTime modifiedTime) {

        @Builder
        public Result {

        }

        public static Result of(Comment entity) {
            return Result.builder()
                .id(entity.getId())
                .ownerId(entity.getOwner().getId())
                .postId(entity.getPost().getId())
                .content(entity.getContent())
                .createTime(entity.getCreatedDate())
                .modifiedTime(entity.getModifiedDate())
                .build();
        }

        public static List<Result> of(Streamable<Comment> entities) {
            return entities.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }
}
