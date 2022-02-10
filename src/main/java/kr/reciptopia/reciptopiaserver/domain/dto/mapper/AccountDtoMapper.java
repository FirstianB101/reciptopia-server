package kr.reciptopia.reciptopiaserver.domain.dto.mapper;

import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.AccountDto.Result;

import kr.reciptopia.reciptopiaserver.domain.model.Account;

public class AccountDtoMapper implements DtoMapper<Account, Create, Result> {

    @Override
    public Account asEntity(Create dto) {
        return Account.builder()
            .email(dto.getEmail())
            .password(dto.getPassword())
            .nickname(dto.getNickname())
            .build();
    }

    @Override
    public Result asResultDto(Account account) {
        return Result.builder()
            .email(account.getEmail())
            .nickname(account.getNickname())
            .profilePictureUrl(account.getProfilePictureUrl())
            .build();
    }
}
