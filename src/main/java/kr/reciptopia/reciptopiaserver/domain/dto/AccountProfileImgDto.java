package kr.reciptopia.reciptopiaserver.domain.dto;

import static kr.reciptopia.reciptopiaserver.domain.dto.CollectorHelper.byLinkedHashMapWithKey;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import kr.reciptopia.reciptopiaserver.domain.model.AccountProfileImg;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;

public interface AccountProfileImgDto {

    interface Bulk {

        @With
        record Result(
            @NotEmpty Map<Long, AccountProfileImgDto.Result> accountProfileImgs) {

            @Builder
            public Result(
                @Singular
                Map<Long, AccountProfileImgDto.Result> accountProfileImgs) {
                this.accountProfileImgs = accountProfileImgs;
            }

            public static Bulk.Result of(Page<AccountProfileImg> accountProfileImgs) {
                return Bulk.Result.builder()
                    .accountProfileImgs(accountProfileImgs.stream()
                        .map(AccountProfileImgDto.Result::of)
                        .collect(byLinkedHashMapWithKey(AccountProfileImgDto.Result::id)))
                    .build();
            }
        }

    }


    @With
    @Builder
    record Result(
        @NotNull Long id,
        @NotEmpty String uploadFileName,
        @NotEmpty String storeFileName,
        @NotNull Long ownerId) {

        public static Result of(AccountProfileImg entity) {
            return Result.builder()
                .id(entity.getId())
                .uploadFileName(entity.getUploadFileName())
                .storeFileName(entity.getStoreFileName())
                .ownerId(entity.getOwner().getId())
                .build();
        }
    }

}
