package kr.reciptopia.reciptopiaserver.helper;

import java.util.UUID;
import kr.reciptopia.reciptopiaserver.domain.dto.AccountDto;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.UserRole;

public class AccountHelper {

    private static final String EMAIL_POSTFIX = "@test.com";
    private static final String ARBITRARY_PASSWORD = "this!sPassw0rd";
    private static final String ARBITRARY_NICKNAME = "nickname";

    private static String getArbitraryEmail() {
        return getShortUUID() + EMAIL_POSTFIX;
    }

    private static String getShortUUID() {
        return UUID.randomUUID().toString().substring(8);
    }

    public static Account anAccount() {
        Account account = Account.builder()
            .email(getArbitraryEmail())
            .password(ARBITRARY_PASSWORD)
            .nickname(ARBITRARY_NICKNAME)
            .role(UserRole.USER)
            .build();
        account.setId(0L);
        return account;
    }

    public static AccountDto.Create anAccountCreateDto() {
        return AccountDto.Create.builder()
            .email(getArbitraryEmail())
            .password(ARBITRARY_PASSWORD)
            .nickname(ARBITRARY_NICKNAME)
            .build();
    }

    public static AccountDto.Update anAccountUpdateDto() {
        return AccountDto.Update.builder()
            .email(getArbitraryEmail())
            .password(ARBITRARY_PASSWORD)
            .nickname(ARBITRARY_NICKNAME)
            .build();
    }

    public static AccountDto.Result anAccountResultDto() {
        String uuid = UUID.randomUUID().toString().substring(8);
        return AccountDto.Result.builder()
            .email(getArbitraryEmail())
            .nickname(ARBITRARY_NICKNAME)
            .role(UserRole.USER)
            .build();
    }

}
