package kr.reciptopia.reciptopiaserver.domain.dto;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import lombok.Builder;
import lombok.With;
import org.springframework.data.util.Streamable;

public interface ReplyDto {

    @With
    record Create(
        Long ownerId, Long commentId, String content) {

        @Builder
        public Create(
            @NotNull
                Long ownerId,

            @NotNull
                Long commentId,

            @NotBlank
            @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
                String content) {
            this.ownerId = ownerId;
            this.commentId = commentId;
            this.content = content;
        }

        public Reply asEntity(
            Function<? super Reply, ? extends Reply> initialize) {
            return initialize.apply(Reply.builder()
                .content(content)
                .build());
        }

        public Reply asEntity() {
            return asEntity(noInit());
        }

        private <T> Function<? super T, ? extends T> noInit() {
            return (arg) -> arg;
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
        Long id, Long ownerId, Long commentId, String content) {

        @Builder
        public Result(
            @NotNull
                Long id,

            @NotNull
                Long ownerId,

            @NotNull
                Long commentId,

            @NotBlank
            @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
                String content) {
            this.id = id;
            this.ownerId = ownerId;
            this.commentId = commentId;
            this.content = content;
        }

        public static Result of(Reply entity) {
            return Result.builder()
                .id(entity.getId())
                .ownerId(entity.getOwner().getId())
                .commentId(entity.getComment().getId())
                .content(entity.getContent())
                .build();
        }

        public static List<Result> of(Streamable<Reply> entities) {
            return entities.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }
}