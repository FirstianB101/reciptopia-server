package kr.reciptopia.reciptopiaserver.business.service;

import static kr.reciptopia.reciptopiaserver.domain.dto.SearchHistoryDto.Create;
import static kr.reciptopia.reciptopiaserver.domain.dto.SearchHistoryDto.Result;

import java.util.List;
import kr.reciptopia.reciptopiaserver.business.service.authorizer.SearchHistoryAuthorizer;
import kr.reciptopia.reciptopiaserver.business.service.helper.RepositoryHelper;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import kr.reciptopia.reciptopiaserver.domain.model.SearchHistory;
import kr.reciptopia.reciptopiaserver.persistence.repository.SearchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchHistoryService {

    private final SearchHistoryAuthorizer searchHistoryAuthorizer;
    private final SearchHistoryRepository searchHistoryRepository;
    private final RepositoryHelper repoHelper;

    @Transactional
    public Result create(Create dto, Authentication authentication) {
        Account owner = repoHelper.findAccountOrThrow(dto.ownerId());
        searchHistoryAuthorizer.requireByOneself(authentication, owner);

        SearchHistory searchHistory = dto.asEntity(it -> it.withOwner(owner));

        return Result.of(searchHistoryRepository.save(searchHistory));
    }

    public Result read(Long id, Authentication authentication) {
        SearchHistory searchHistory = repoHelper.findSearchHistoryOrThrow(id);
        searchHistoryAuthorizer.requireSearchHistoryOwner(authentication, searchHistory);

        return Result.of(searchHistory);
    }

    public List<Result> search(Long ownerId, Authentication authentication, Pageable pageable) {
        Account owner = repoHelper.findAccountOrThrow(ownerId);
        searchHistoryAuthorizer.requireByOneself(authentication, owner);

        Page<SearchHistory> searchHistoryRepositories = searchHistoryRepository.findAll(pageable);
        return Result.of(searchHistoryRepositories);
    }

    @Transactional
    public void delete(Long id, Authentication authentication) {
        SearchHistory searchHistory = repoHelper.findSearchHistoryOrThrow(id);
        searchHistoryAuthorizer.requireSearchHistoryOwner(authentication, searchHistory);

        searchHistoryRepository.delete(searchHistory);
    }
}
