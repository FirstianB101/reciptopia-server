package kr.reciptopia.reciptopiaserver.domain.dto;

import static kr.reciptopia.reciptopiaserver.domain.dto.helper.CollectorHelper.byLinkedHashMapWithKey;
import static kr.reciptopia.reciptopiaserver.domain.dto.helper.InitializationHelper.noInit;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.model.Reply;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Streamable;

public interface ReplyDto {

    interface Bulk {

        @With
        record Result(@NotEmpty Map<Long, ReplyDto.Result> replies) {

            @Builder
            public Result(
                @Singular
                Map<Long, ReplyDto.Result> replies
            ) {
                this.replies = replies;
            }

            public static Result of(Page<Reply> replies) {
                return Result.builder()
                    .replies(replies.stream()
                        .map(ReplyDto.Result::of)
                        .collect(byLinkedHashMapWithKey(ReplyDto.Result::id)))
                    .build();
            }
        }
    }

    @With
    @Builder
    record Create(
        @NotNull Long ownerId,
        @NotNull Long commentId,
        @NotBlank
        @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
        String content) {

        public Reply asEntity(
            Function<? super Reply, ? extends Reply> initialize) {
            return initialize.apply(Reply.builder()
                .content(content)
                .build());
        }

        public Reply asEntity() {
            return asEntity(noInit());
        }
    }

    @With
    @Builder
    record Update(
        @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
        String content) {

    }

    @With
    @Builder
    record Result(
        @NotNull Long id,
        @NotNull Long ownerId,
        @NotNull Long commentId,
        @NotBlank
        @Size(min = 1, max = 50, message = "content는 1 ~ 50자 이여야 합니다!")
        String content) {

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