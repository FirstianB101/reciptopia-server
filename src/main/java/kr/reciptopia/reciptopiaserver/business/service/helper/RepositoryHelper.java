package kr.reciptopia.reciptopiaserver.business.service.helper;

import javax.persistence.EntityManager;
import kr.reciptopia.reciptopiaserver.domain.model.Account;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public record RepositoryHelper(EntityManager em,
                               ServiceErrorHelper errorHelper) {

    public <T> T findOrThrow(Class<T> clazz, Object primaryKey) throws ResponseStatusException {
        T entity = em.find(clazz, primaryKey);
        if (entity == null) {
            String reason = String.format("%s id was not found", clazz.getName());
            throw errorHelper.notFound(reason);
        }
        return entity;
    }

    public Account findAccountOrThrow(Long id) throws ResponseStatusException {
        return findOrThrow(Account.class, id);
    }
}
