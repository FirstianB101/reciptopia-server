package kr.reciptopia.reciptopiaserver.domain.dto;

import com.querydsl.core.Tuple;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import kr.reciptopia.reciptopiaserver.domain.error.exception.InvalidTupleTypeException;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.UserRole;
import lombok.Builder;
import lombok.Singular;
import lombok.With;
import org.springframework.data.domain.Page;
import org.springframework.data.util.Streamable;

public interface AccountDto {

    interface Bulk {

        @With
        record Result(
            @NotEmpty Map<Long, AccountDto.Result> accounts
        ) {
            private static int POST_ID_TUPLE_INDEX = 0, ACCOUNT_TUPLE_INDEX = 1;

            @Builder
            public Result(
                @Singular
                    Map<Long, AccountDto.Result> accounts
            ) {
                this.accounts = accounts;
            }

            public static Result of(Page<?> tuples) {
                return Result.builder()
                    .accounts(getAccounts(tuples))
                    .build();
            }

            private static LinkedHashMap<Long, AccountDto.Result> getAccounts(Page<?> tuples) {
                return tuples.stream()
                    .collect(collectToLinkedHashMap());
            }

            private static Collector<Object, ?, LinkedHashMap<Long, AccountDto.Result>> collectToLinkedHashMap() {
                return Collectors.toMap(
                    setKey(),
                    setValue(),
                    (x, y) -> y,
                    LinkedHashMap::new);
            }

            private static Function<Object, AccountDto.Result> setValue() {
                return tuple -> {
                    if (tuple instanceof Tuple)
                        return AccountDto.Result.of(
                            Objects.requireNonNull(
                                ((Tuple) tuple).get(ACCOUNT_TUPLE_INDEX, Account.class)));
                    else if (tuple instanceof Account)
                        return AccountDto.Result.of((Account) tuple);
                    else
                        throw new InvalidTupleTypeException();
                };
            }

            private static Function<Object, Long> setKey() {
                return tuple -> {
                    if (tuple instanceof Tuple)
                        return ((Tuple) tuple).get(POST_ID_TUPLE_INDEX, Long.class);
                    else if (tuple instanceof Account)
                        return ((Account) tuple).getId();
                    else
                        throw new InvalidTupleTypeException();
                };
            }
        }
    }

    @With
    record Create(
        @NotNull @Email(message = "????????? ????????? ????????????.") String email,
        @NotEmpty @Size(min = 8, max = 16, message = "password??? 8 ~ 16??? ????????? ?????????!") String password,
        @NotBlank @Size(min = 5, max = 16, message = "nickname??? 5 ~ 16??? ????????? ?????????!") String nickname) {

        @Builder
        public Create {

        }

        public Account asEntity(
            Function<? super Account, ? extends Account> initialize) {
            return initialize.apply(Account.builder()
                .email(email)
                .nickname(nickname)
                .password(password)
                .build());
        }

        public Account asEntity() {
            return asEntity(noInit());
        }

        private <T> Function<? super T, ? extends T> noInit() {
            return (arg) -> arg;
        }
    }

    @With
    record Update(
        @Email(message = "????????? ????????? ????????????.") String email,
        @Size(min = 8, max = 16, message = "password??? 8 ~ 16??? ????????? ?????????!") String password,
        @Size(min = 5, max = 16, message = "nickname??? 5 ~ 16??? ????????? ?????????!") String nickname,
        String profilePictureUrl) {

        @Builder
        public Update {
        }
    }

    @With
    record CheckDuplicationResult(@NotNull Boolean exists) {

        @Builder
        public CheckDuplicationResult {
        }
    }

    @With
    record Result(
        @NotNull Long id,
        @NotNull @Email(message = "????????? ????????? ????????????.") String email,
        @NotBlank @Size(min = 5, max = 16, message = "nickname??? 5 ~ 16??? ????????? ?????????!") String nickname,
        String profilePictureUrl,
        UserRole role) {

        @Builder
        public Result {

        }

        public static Result of(Account entity) {
            return Result.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .nickname(entity.getNickname())
                .profilePictureUrl(entity.getProfilePictureUrl())
                .role(entity.getRole())
                .build();
        }

        public static List<Result> of(Streamable<Account> entities) {
            return entities.stream()
                .map(Result::of)
                .collect(Collectors.toList());
        }
    }

}
