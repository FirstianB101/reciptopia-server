package kr.reciptopia.reciptopiaserver.domain.dto.helper;

import com.querydsl.core.Tuple;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CollectorHelper {

    private static final int ID_TUPLE_INDEX = 0, RESULT_DTO_TUPLE_INDEX = 1;

    public static <E, R> Collector<? super Tuple, ?, Map<Long, List<R>>> byLinkedHashMapWithTuple(
        Class<E> entitiyClazz, Function<E, R> resultDtoOfEntity) {
        return Collectors.toMap(
            getKeyFromTuple(),
            getResultsFromTuple(entitiyClazz, resultDtoOfEntity),
            mergeSameKey(),
            LinkedHashMap::new);
    }

    public static <R> Collector<R, ?, Map<Long, List<R>>> byListValueLinkedHashMapWithKey(
        Function<R, Long> getId) {
        return Collectors.toMap(
            getId,
            CollectorHelper::convertToList,
            mergeSameKey(),
            LinkedHashMap::new);
    }

    public static <R> Collector<R, ?, Map<Long, R>> byLinkedHashMapWithKey(
        Function<R, Long> getId) {
        return Collectors.toMap(
            getId,
            result -> result,
            (x, y) -> y,
            LinkedHashMap::new);
    }

    public static <R> BinaryOperator<List<R>> mergeSameKey() {
        return (x, y) -> {
            x.addAll(y);
            return x;
        };
    }

    public static <E, R> Function<? super Tuple, List<R>> getResultsFromTuple
        (Class<E> entitiyClazz, Function<E, R> resultDtoOfEntity) {
        return tuple -> {
            E entity = Objects.requireNonNull(tuple.get(RESULT_DTO_TUPLE_INDEX, entitiyClazz));
            return convertToList(resultDtoOfEntity.apply(entity));
        };
    }

    static <T> List<T> convertToList(T element) {
        var results = new ArrayList<T>();
        results.add(element);
        return results;
    }

    private static Function<? super Tuple, Long> getKeyFromTuple() {
        return tuple -> tuple.get(ID_TUPLE_INDEX, Long.class);
    }

    public static <T> Function<? super T, ? extends T> noInit() {
        return (arg) -> arg;
    }
}
