package kr.reciptopia.reciptopiaserver.config.querydsl;

import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPQLQuery;
import java.util.List;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PagingUtil {

    private final EntityManager em;

    private Querydsl getQuerydsl(Class<?> clazz) {
        PathBuilder<?> builder = new PathBuilderFactory().create(clazz);
        return new Querydsl(em, builder);
    }

    public <T> PageImpl<T> getPageImpl(Pageable pageable, JPQLQuery<T> query, Class clazz) {
        long totalCount = query.fetch().size();
        List<T> results = getQuerydsl(clazz).applyPagination(pageable, query).fetch();
        return new PageImpl<>(results, pageable, totalCount);
    }

    public static <T> List<T> getPage(List<T> entity, Pageable pageable) {
        return entity.subList(
            (int) pageable.getOffset(),
            (int) Math.min((pageable.getOffset() + pageable.getPageSize()),
                entity.size() - pageable.getOffset()));
    }
}