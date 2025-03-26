package com.zyf.utils.lambda;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 工具类
 *
 * @author zengyufei
 * @date 2021/11/22
 */
@Slf4j
public class LamUtil {


    // =================================================================================================================
    // =================================================================================================================
    // =================================================================================================================


    // =================================================================================================================
    // =================================================================================================================
    // =================================================================================================================

    /**
     * 将一个 List 转为字符串并使用指定的分隔符进行连接。这个方法遵循空值不处理的原则。
     *
     * @param originList 要处理的 List，该方法会删除其中的 null 值
     * @param delimiter  分隔符，用于连接 List 中的元素
     * @return 连接后的字符串
     */
    public static String join(List<String> originList, String delimiter) {
        if (CollUtil.isEmpty(originList)) {
            return null;
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return null;
        }
        return originList.stream()
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * 将一个 List 转为字符串并使用指定的分隔符进行连接。这个方法遵循空值不处理的原则。
     *
     * @param originList 要处理的 List，该方法会删除其中的 null 值
     * @param delimiter  分隔符，用于连接 List 中的元素
     * @param mapper     转换函数，用于将 List 中的元素转为字符串
     * @return 连接后的字符串
     */
    public static <T> String join(Collection<T> originList, String delimiter, Function<T, String> mapper) {
        if (CollUtil.isEmpty(originList)) {
            return null;
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return null;
        }
        return originList.stream()
                .map(mapper)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.joining(delimiter));
    }

    /**
     * 根据传入的 Predicate 列表过滤 List 中的元素，并返回过滤后的 List。这个方法遵循空值不处理的原则。
     * filtersToList(list, e->e.age>10, e->age<50) 等同 where age>10 and age<50
     *
     * @param originList 要处理的 List，该方法会删除其中的 null 值
     * @param filters    要用于过滤的 Predicate 列表  @return 过滤后的 List
     */
    @SafeVarargs
    public static <T> List<T> filtersToList(Collection<T> originList,
                                            Predicate<T>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return Arrays.stream(filters)
                .reduce(Predicate::and)
                .map(originList.stream()::filter)
                .orElse(Stream.empty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 根据传入的 Predicate 列表过滤 List 中的元素，并返回去重后的 List。这个方法遵循空值不处理和去重后顺序不变的原则。
     *
     * @param originList 要处理的 List，该方法会删除其中的 null 值
     * @param filters    要用于过滤的 Predicate 列表
     * @return 过滤后去重的 List
     */
    @SafeVarargs
    public static <T> List<T> filterDistinctToList(Collection<T> originList,
                                                   Predicate<T>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }

        return Arrays.stream(filters)
                .reduce(Predicate::and)
                .map(originList.stream()::filter)
                .orElse(Stream.empty())
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * (支持多字段共同去重) 根据传入的 Predicate 列表过滤 List 中的元素，并返回去重后的 List。这个方法遵循空值不处理和去重后顺序不变的原则。
     *
     * @param originList 要处理的 List，该方法会删除其中的 null 值
     * @param filters    要用于过滤的 Predicate 列表
     * @return 过滤后去重的 List
     */
    @SafeVarargs
    public static <T, U extends Comparable<? super U>> List<T> filterDistinctsToList(Collection<T> originList,
                                                                                     Function<T, U> function,
                                                                                     Predicate<T>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }

        return Arrays.stream(filters)
                .reduce(Predicate::and)
                .map(originList.stream()::filter)
                .orElse(Stream.empty())
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(() -> {
                            final Comparator<T> comparing = Comparator.comparing(function);
                            return new TreeSet<>(comparing);
                        }), ArrayList::new));
    }

    /**
     * 根据传入的 Predicate 列表过滤 List 中的元素，然后再将剩余的元素转换成一个新的元素，最后返回转换后的 List。这个方法遵循空值不处理的原则。
     *
     * @param originList 要处理的 List，该方法会删除其中的 null 值
     * @param function   转换函数，用于将剩余的元素转换成一个新的元素。
     * @param filters    要用于过滤的 Predicate 列表
     * @return 过滤并转换后的 List
     */
    @SafeVarargs
    public static <T, R> List<R> filtersMapToList(Collection<T> originList,
                                                  Function<T, R> function,
                                                  Predicate<T>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                .map(function)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 根据传入的 Predicate 列表过滤 List 中的元素，然后再将剩余的元素转换成一个新的元素，最后返回去重后的 List。这个方法遵循空值不处理和去重后顺序不变的原则。
     *
     * @param originList 要处理的 List，该方法会删除其中的 null 值
     * @param function   转换函数，用于将剩余的元素转换成一个新的元素。
     * @param filters    要用于过滤的 Predicate 列表
     * @return 过滤并转换后去重的 List
     */
    @SafeVarargs
    public static <T, R> List<R> filtersDistinctMapToList(Collection<T> originList,
                                                          Function<T, R> function,
                                                          Predicate<T>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                .distinct()
                .map(function)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 根据传入的 Predicate 列表过滤 List 中的元素，然后再将剩余的元素转换成一个新的元素，最后返回先转换后去重的 List。这个方法遵循空值不处理和去重后顺序不变的原则。
     *
     * @param originList 要处理的 List，该方法会删除其中的 null 值
     * @param function   转换函数，用于将剩余的元素转换成一个新的元素。
     * @param filters    要用于过滤的 Predicate 列表
     * @return 过滤并转换后先去重的 List
     */
    @SafeVarargs
    public static <T, R> List<R> filtersMapDistinctToList(Collection<T> originList,
                                                          Function<T, R> function,
                                                          Predicate<T>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                .map(function)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 过滤空值并进行去重，将结果转换成列表
     *
     * @param originList 源列表
     * @param function   转换函数
     * @param filters    过滤条件
     * @param <R>        目标类型
     * @return 过滤、去重、转换后的列表
     */
    @SafeVarargs
    public static <R> List<R> filterBlankDistinctMapToList(List<String> originList,
                                                           Function<String, R> function,
                                                           Predicate<String>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeFilter(originList, StrUtil::isBlank);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                .map(function)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 平铺成列表
     *
     * @param originList 列表
     * @param function   映射器
     * @return {@link List}<{@link R}>
     */
    public static <T, R> List<R> filtersFlatMapToList(List<T> originList,
                                                      Function<T, List<R>> function,
                                                      Predicate<T>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                .flatMap(function.andThen(List::stream))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 平铺成列表
     *
     * @param originList 列表
     * @param function   映射器
     * @return {@link List}<{@link R}>
     */
    public static <T, R> List<R> flatMapToList(List<T> originList, Function<T, List<R>> function) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .flatMap(function.andThen(List::stream))
                .collect(Collectors.toCollection(ArrayList::new));
    }


    /**
     * 对元素进行转换并过滤空值，最后进行去重，将结果转换成字符串列表
     *
     * @param originList 源列表
     * @param function   转换函数
     * @param filters    过滤条件
     * @return 转换、过滤、去重后的字符串列表
     */
    @SafeVarargs
    public static <T> List<String> mapFiltersBlankDistinctToList(Collection<T> originList,
                                                                 Function<T, String> function,
                                                                 Predicate<String>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .map(function)
                .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 过滤空值并进行去重，将结果转换成列表
     *
     * @param originList 源列表
     * @param filters    过滤条件
     * @return 过滤、去重后的列表
     */
    @SafeVarargs
    public static List<String> filterBlankDistinctToList(List<String> originList,
                                                         Predicate<String>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeFilter(originList, StrUtil::isBlank);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 对列表进行去重操作
     *
     * @param originList   待去重的列表
     * @param keyExtractor 提取关键字的函数
     * @param <T>          元素类型
     * @return 去重后的列表
     */
    public static <T> List<T> distinctToList(Collection<T> originList, Function<? super T, ?> keyExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .filter(distinctByKey(keyExtractor))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 对列表进行去重操作
     *
     * @param originList 待去重的列表
     * @param <T>        元素类型
     * @return 去重后的列表
     */
    public static <T> List<T> distinctToList(Collection<T> originList) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 根据给定的关键字提取函数，返回一个去重的Predicate
     *
     * @param keyExtractor 提取关键字的函数
     * @param <T>          元素类型
     * @return 去重的Predicate
     */
    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * 删除为空的元素
     *
     * @param originList 源列表
     * @param <T>        元素类型
     * @return 删除null元素后的列表
     */
    public static <T> List<T> removeNull(Collection<T> originList) {
        return removeFilter(originList, Objects::isNull);
    }

    /**
     * 删除符合指定条件的元素
     *
     * @param originList       源列表
     * @param removeConditions 删除条件
     * @param <T>              元素类型
     * @return 删除符合条件的元素后的列表
     */
    @SafeVarargs
    public static <T> List<T> removeFilter(Collection<T> originList, Predicate<? super T>... removeConditions) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .filter(item -> Stream.of(removeConditions).noneMatch(removeCondition -> removeCondition.test(item)))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 匹配给定的过滤条件并返回一个Optional对象，表示列表中第一个匹配的元素
     *
     * @param originList 源列表
     * @param filters    过滤条件
     * @param <T>        元素类型
     * @return 匹配到的元素的Optional，为空表示未匹配到
     */
    @SafeVarargs
    public static <T> Optional<T> filtersToFindFirstOptional(Collection<T> originList,
                                                             Predicate<T>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return Optional.empty();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return Optional.empty();
        }
        try {
            return originList.stream()
                    .filter(Objects::nonNull)
                    .filter(Stream.of(filters).filter(Objects::nonNull).reduce(Predicate::and).orElse(t -> true))
                    .findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * 匹配给定的过滤条件并返回列表中第一个匹配的元素
     *
     * @param originList 源列表
     * @param filters    过滤条件
     * @param <T>        元素类型
     * @return 匹配到的元素，为空表示未匹配到
     */
    @SafeVarargs
    public static <T> T filtersToFindFirst(Collection<T> originList,
                                           Predicate<T>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return null;
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return null;
        }
        try {
            return originList.stream()
                    .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 对元素进行转换并匹配给定的过滤条件，返回第一个匹配的元素
     *
     * @param originList 源列表
     * @param mapper     转换函数
     * @param filters    过滤条件
     * @param <T>        元素类型
     * @param <U>        目标类型
     * @return 匹配到的元素，为空表示未匹配到
     */
    @SafeVarargs
    public static <T, U> U mapFiltersToFindFirst(Collection<T> originList,
                                                 Function<T, U> mapper,
                                                 Predicate<U>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return null;
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return null;
        }
        try {
            return originList.stream()
                    .map(mapper)
                    .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断是否存在任意一个元素匹配给定的过滤条件
     *
     * @param originList 源列表
     * @param mapper     过滤条件
     * @param <T>        元素类型
     * @return 如果列表中存在任意一个元素匹配给定的过滤条件，返回true，否则返回false
     */
    public static <T> boolean anyMatch(Collection<T> originList,
                                       Predicate<T> mapper) {
        if (CollUtil.isEmpty(originList)) {
            return false;
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return false;
        }
        return originList.stream().anyMatch(mapper);
    }

    public static <T, U> List<T> filterMatchToList(Collection<T> originList,
                                                   Collection<U> targetList,
                                                   Function<T, String> sourceKeyExtractor,
                                                   Function<U, String> filterKeyExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        targetList = removeNull(targetList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        Collection<U> finalTargetList = targetList;
        return originList.stream()
                .filter(a -> finalTargetList.stream().anyMatch(b -> filterKeyExtractor.apply(b).equalsIgnoreCase(sourceKeyExtractor.apply(a))))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 判断是否所有元素都不匹配给定的过滤条件
     *
     * @param originList 源列表
     * @param mapper     过滤条件
     * @param <T>        元素类型
     * @return 如果列表中不存在任意一个元素匹配给定的过滤条件，返回true，否则返回false
     */
    public static <T> boolean noneMatch(Collection<T> originList,
                                        Predicate<T> mapper) {
        if (CollUtil.isEmpty(originList)) {
            return false;
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return false;
        }
        return originList.stream().noneMatch(mapper);
    }

    /**
     * 判断是否存在任意一个经过给定函数转换后的元素匹配给定的过滤条件
     *
     * @param originList 源列表
     * @param mapper     转换函数
     * @param predicate  过滤条件
     * @param <T>        元素类型
     * @param <R>        转换后的类型
     * @return 如果列表中存在任意一个元素经过给定函数转换后匹配给定的过滤条件，返回true，否则返回false
     */
    public static <T, R> boolean mapAnyMatch(Collection<T> originList,
                                             Function<T, R> mapper,
                                             Predicate<R> predicate) {
        if (CollUtil.isEmpty(originList)) {
            return false;
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return false;
        }
        return originList.stream().map(mapper).anyMatch(predicate);
    }

    /**
     * 判断是否存在任意一个经过给定函数转换并去重后的元素匹配给定的过滤条件
     *
     * @param originList 源列表
     * @param mapper     转换函数
     * @param predicate  过滤条件
     * @param <T>        元素类型
     * @param <R>        转换后的类型
     * @return 如果列表中存在任意一个元素经过给定函数转换后并且去重后匹配给定的过滤条件，返回true，否则返回false
     */
    public static <T, R> boolean mapDistinctAnyMatch(Collection<T> originList,
                                                     Function<T, R> mapper,
                                                     Predicate<R> predicate) {
        if (CollUtil.isEmpty(originList)) {
            return false;
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return false;
        }
        return originList.stream().map(mapper).distinct().anyMatch(predicate);
    }

    /**
     * 对每个元素进行多次转换，并将其结果合并成一个列表
     *
     * @param originList 源列表
     * @param filters    转换函数
     * @param <T>        元素类型
     * @param <R>        转换后的类型
     * @return 转换后的列表
     */
    @SafeVarargs
    public static <T, R> List<R> mapToList(Collection<T> originList,
                                           Function<T, R>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .flatMap(t -> Arrays.stream(filters)
                        .filter(Objects::nonNull)
                        .map(f -> f.apply(t))
                        .filter(Objects::nonNull))
                .collect(Collectors.toCollection(ArrayList::new));
    }


    @SafeVarargs
    public static <S, T, R> List<T> mapValuesFlatToList(Map<S, List<R>> origin,
                                                        Function<R, T>... filters) {
        if (MapUtil.isEmpty(origin)) {
            return new ArrayList<>();
        }
        return origin.values()
                .stream()
                .flatMap(Collection::stream)
                .flatMap(t -> Arrays.stream(filters)
                        .filter(Objects::nonNull)
                        .map(f -> f.apply(t))
                        .filter(Objects::nonNull))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 对每个元素进行多次转换并去重，将其结果合并成一个列表
     *
     * @param originList 源列表
     * @param filters    转换函数
     * @param <T>        元素类型
     * @param <R>        转换后的类型
     * @return 转换、去重后的列表
     */
    @SafeVarargs
    public static <T, R> List<R> mapDistinctToList(Collection<T> originList,
                                                   Function<T, R>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .flatMap(t -> Arrays.stream(filters)
                        .filter(Objects::nonNull)
                        .map(f -> f.apply(t))
                        .filter(Objects::nonNull))
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 对元素进行转换并进行去重，最后进行过滤，将结果转换成列表
     *
     * @param originList 源列表
     * @param mapper     转换函数
     * @param filters    过滤条件
     * @param <T>        元素类型
     * @param <R>        转换后的类型
     * @return 转换、去重、过滤后的列表
     */
    @SafeVarargs
    public static <T, R> List<R> mapFiltersDistinctToList(Collection<T> originList,
                                                          Function<T, R> mapper,
                                                          Predicate<R>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 过滤后处理集合成新的List集合
     *
     * @param originList 原始集合
     * @param mapper     处理方法
     * @param filters    过滤条件，可以添加多个，所有条件同时成立才添加到新的集合中
     * @return 新的List集合
     */
    @SuppressWarnings({"all"})
    public static <T, R> List<R> mapFiltersToList(Collection<T> originList,
                                                  Function<T, R> mapper,
                                                  Predicate<R>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 过滤后去重处理成新的List集合
     *
     * @param originList 原始集合
     * @param mapper     处理方法
     * @param filters    过滤条件，可以添加多个，所有条件同时成立才添加到新的集合中
     * @return 新的List集合
     */
    @SuppressWarnings({"all"})
    public static <T, R> List<R> mapDistinctFiltersToList(Collection<T> originList,
                                                          Function<T, R> mapper,
                                                          Predicate<R>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .distinct()
                .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * 将List中的元素转化为Map，指定Key的提取方法
     *
     * @param originList   原始List集合
     * @param keyExtractor Key提取方法
     * @return Map结果
     */
    public static <K, V> Map<K, V> toBeanMap(Collection<V> originList,
                                             Function<V, K> keyExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList
                .stream()
                .filter(Objects::nonNull)
                .filter(element -> keyExtractor.apply(element) != null)
                .collect(Collectors.toMap(keyExtractor, Function.identity(), (v1, v2) -> v1));
    }

    /**
     * 将List中的元素转化为Map，指定Key的提取方法，Value为不同来源元素的合并结果
     *
     * @param originList   原始List集合
     * @param keyExtractor Key提取方法
     * @return Map结果
     */
    public static <K, V> Map<K, V> filterToBeanMap(Collection<V> originList,
                                                   Predicate<V> filter,
                                                   Function<V, K> keyExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList
                .stream()
                .filter(Objects::nonNull)
                .filter(filter)
                .filter(element -> keyExtractor.apply(element) != null)
                .collect(Collectors.toMap(keyExtractor, Function.identity()));
    }

    /**
     * 将List中的元素转化为Map，指定Key的提取方法，Value为不同来源元素的合并结果
     *
     * @param originList   原始List集合
     * @param keyExtractor Key提取方法
     * @return Map结果
     */
    public static <K, V> Map<K, V> filterToBeanMergeMap(Collection<V> originList,
                                                        Predicate<V> filter,
                                                        Function<V, K> keyExtractor,
                                                        BinaryOperator<V> mergeExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList
                .stream()
                .filter(filter)
                .filter(Objects::nonNull)
                .filter(element -> keyExtractor.apply(element) != null)
                .collect(Collectors.toMap(keyExtractor, Function.identity(), mergeExtractor));
    }

    /**
     * 将List中的元素转化为Map，指定Key的提取方法，Value为不同来源元素的合并结果
     *
     * @param originList   原始List集合
     * @param keyExtractor Key提取方法
     * @return Map结果
     */
    @SafeVarargs
    public static <K, V> Map<K, V> filtersToBeanMap(Collection<V> originList,
                                                    Function<V, K> keyExtractor,
                                                    Predicate<V>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList
                .stream()
                .filter(Objects::nonNull)
                .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                .filter(element -> keyExtractor.apply(element) != null)
                .collect(Collectors.toMap(keyExtractor, Function.identity()));
    }


    @SafeVarargs
    public static <T> long count(Collection<T> originList, Predicate<T>... filters) {
        return originList.stream()
                .filter(Objects::nonNull)
                .filter(Stream.of(filters).reduce(Predicate::and).orElse(t -> true))
                .count();
    }

    /**
     * 将List中的元素转化为Map，指定Key的提取方法
     *
     * @param originList   原始List集合
     * @param keyExtractor Key提取方法
     * @return Map结果
     */
    public static <K, V> Map<K, V> toBeanLinkedMap(Collection<V> originList,
                                                   Function<V, K> keyExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList
                .stream()
                .filter(Objects::nonNull)
                .filter(element -> keyExtractor.apply(element) != null)
                .collect(Collectors.toMap(keyExtractor, Function.identity(), (v1, v2) -> v1, LinkedHashMap::new));
    }

    /**
     * 将List中的元素转化为Map，指定Key的提取方法和Value的提取方法
     *
     * @param originList     原始List集合
     * @param keyExtractor   Key提取方法
     * @param valueExtractor Value提取方法
     * @return Map结果
     */
    public static <K, V, S> Map<K, S> toMap(Collection<V> originList,
                                            Function<V, K> keyExtractor,
                                            Function<V, S> valueExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }

        Map<K, S> map = new HashMap<>(originList.size());
        for (V v : originList) {
            map.put(keyExtractor.apply(v), valueExtractor.apply(v));
        }
        return map;
    }

    /**
     * 将List中的元素转化为Map，指定Key的提取方法和Value的提取方法
     *
     * @param originList     原始List集合
     * @param keyExtractor   Key提取方法
     * @param valueExtractor Value提取方法
     * @return Map结果
     */
    public static <K, V, S> Map<K, S> toLinkedMap(Collection<V> originList,
                                                  Function<V, K> keyExtractor,
                                                  Function<V, S> valueExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList.stream()
                .filter(item -> keyExtractor.apply(item) != null && valueExtractor.apply(item) != null)
                .collect(Collectors.toMap(keyExtractor, valueExtractor, (k1, k2) -> k1, LinkedHashMap::new));
    }

    /**
     * 将List中的元素转化为Map，指定Key的提取方法，Value为不同Source的合并结果
     *
     * @param originList     原始List集合
     * @param keyExtractor   Key提取方法
     * @param valueExtractor Value提取方法
     * @return Map结果
     */
    public static <K, V, S> Map<K, S> toMergeMap(Collection<V> originList,
                                                 Function<V, K> keyExtractor,
                                                 Function<V, S> valueExtractor,
                                                 BinaryOperator<S> mergeExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList.stream()
                .filter(item -> keyExtractor.apply(item) != null && valueExtractor.apply(item) != null)
                .collect(Collectors.toMap(keyExtractor, valueExtractor, mergeExtractor));
    }

    /**
     * 将List中的元素转化为Map，指定Key的提取方法，Value为不同Source的合并结果
     *
     * @param originList     原始List集合
     * @param keyExtractor   Key提取方法
     * @param valueExtractor Value提取方法
     * @return Map结果
     */
    public static <K, V, S> Map<K, S> toMergeLinkedMap(Collection<V> originList,
                                                       Function<V, K> keyExtractor,
                                                       Function<V, S> valueExtractor,
                                                       BinaryOperator<S> mergeExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList.stream()
                .filter(item -> keyExtractor.apply(item) != null && valueExtractor.apply(item) != null)
                .collect(Collectors.toMap(keyExtractor, valueExtractor, mergeExtractor, LinkedHashMap::new));
    }

    /**
     * 将List中的元素转化为Map，指定Key的提取方法，Value为不同来源元素的合并结果
     *
     * @param originList   原始List集合
     * @param keyExtractor Key提取方法
     * @return Map结果
     */
    public static <K, V> Map<K, V> toBeanMergeMap(Collection<V> originList,
                                                  Function<V, K> keyExtractor,
                                                  BinaryOperator<V> mergeExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList
                .stream()
                .filter(Objects::nonNull)
                .filter(element -> keyExtractor.apply(element) != null)
                .collect(Collectors.toMap(keyExtractor, Function.identity(), mergeExtractor));
    }


    /**
     * 将List中的元素转化为Map，指定Key的提取方法，Value为不同来源元素的合并结果
     *
     * @param originList   原始List集合
     * @param keyExtractor Key提取方法
     * @return Map结果
     */
    public static <K, V> Map<K, V> toBeanMergeLinkedMap(Collection<V> originList,
                                                        Function<V, K> keyExtractor,
                                                        BinaryOperator<V> mergeExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList.stream()
                .filter(item -> keyExtractor.apply(item) != null)
                .collect(Collectors.toMap(keyExtractor, v -> v, mergeExtractor, LinkedHashMap::new));
    }


    /**
     * 根据某一条目必要属性值，取出源列表中符合条件的条目，然后通过需要合并的属性仍聚合为 map 数据。
     *
     * @param <K>            返回的 Map 中键的类型
     * @param <V>            待处理列表中元素的类型
     * @param <S>            返回的 Map 中 value 的类型
     * @param originList     待处理列表数据，不能为 null
     * @param filter         额外过滤器
     * @param keyExtractor   作为 Map 中键的提取逻辑
     * @param valueExtractor 作为 Map 中 value 的提取逻辑
     * @return 经过过滤、转化、聚合操作的 Map
     */
    public static <K, V, S> Map<K, S> filterToMap(Collection<V> originList,
                                                  Predicate<V> filter,
                                                  Function<V, K> keyExtractor,
                                                  Function<V, S> valueExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList.stream()
                .filter(filter)
                .filter(item -> keyExtractor.apply(item) != null && valueExtractor.apply(item) != null)
                .collect(Collectors.toMap(keyExtractor, valueExtractor, (k1, k2) -> k1));
    }

    /**
     * 根据某一条目必要属性值，取出源列表中符合条件的条目，然后通过需要合并的属性仍聚合为 map 数据。
     *
     * @param <K>            返回的 Map 中键的类型
     * @param <V>            待处理列表中元素的类型
     * @param <S>            返回的 Map 中 value 的类型
     * @param originList     待处理列表数据，不能为 null
     * @param filter         额外过滤器
     * @param keyExtractor   作为 Map 中键的提取逻辑
     * @param valueExtractor 作为 Map 中 value 的提取逻辑
     * @param mergeExtractor 合并两个 Value 为一个 Value 的逻辑
     * @return 经过过滤、转化、聚合操作的 Map
     */
    public static <K, V, S> Map<K, S> filterToMergeMap(Collection<V> originList,
                                                       Predicate<V> filter,
                                                       Function<V, K> keyExtractor,
                                                       Function<V, S> valueExtractor,
                                                       BinaryOperator<S> mergeExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList.stream()
                .filter(filter)
                .filter(item -> keyExtractor.apply(item) != null && valueExtractor.apply(item) != null)
                .collect(Collectors.toMap(keyExtractor, valueExtractor, mergeExtractor));
    }

    /**
     * 根据某一条目必要属性值，取出源列表中符合条件的条目，然后通过需要合并的属性仍聚合为 map 数据。
     *
     * @param <K>            返回的 Map 中键的类型
     * @param <V>            待处理列表中元素的类型
     * @param <S>            返回的 Map 中 value 的类型
     * @param originList     待处理列表数据，不能为 null
     * @param filter         额外过滤器
     * @param keyExtractor   作为 Map 中键的提取逻辑
     * @param valueExtractor 作为 Map 中 value 的提取逻辑
     * @param mergeExtractor 合并两个 Value 为一个 Value 的逻辑
     * @return 经过过滤、转化、聚合操作的 Map
     */
    public static <K, V, S> Map<K, S> filterToMergeLinkedMap(Collection<V> originList,
                                                             Predicate<V> filter,
                                                             Function<V, K> keyExtractor,
                                                             Function<V, S> valueExtractor,
                                                             BinaryOperator<S> mergeExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList.stream()
                .filter(filter)
                .filter(item -> keyExtractor.apply(item) != null && valueExtractor.apply(item) != null)
                .collect(Collectors.toMap(keyExtractor, valueExtractor, mergeExtractor, LinkedHashMap::new));
    }

    /**
     * 根据某一条目的必要属性值，取出源列表中符合条件的条目，然后通过需要合并的属性仍聚合为 map 数据。
     *
     * @param <K>            返回的 Map 中键的类型
     * @param <V>            待处理列表中元素的类型
     * @param <S>            返回的 Map 中 value 的类型
     * @param originList     待处理列表数据，不能为 null
     * @param keyExtractor   作为 Map 中键的提取逻辑
     * @param valueExtractor 作为 Map 中 value 的提取逻辑
     * @param mergeExtractor 合并两个 Value 为一个 Value 的逻辑
     * @param filters        额外过滤器组，每个过滤器会被 and 组合起来。如果没有过滤器，可以不传
     * @return 经过过滤、转化、聚合操作的 Map
     */
    @SafeVarargs
    public static <K, V, S> Map<K, S> filtersToMergeMap(Collection<V> originList,
                                                        Function<V, K> keyExtractor,
                                                        Function<V, S> valueExtractor,
                                                        BinaryOperator<S> mergeExtractor,
                                                        Predicate<V>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList.stream()
                .filter(Arrays.stream(filters).reduce(Predicate::and)
                        .orElse(t -> true))
                .filter(item -> keyExtractor.apply(item) != null && valueExtractor.apply(item) != null)
                .collect(Collectors.toMap(keyExtractor, valueExtractor, mergeExtractor));
    }

    /**
     * 根据某一条目的必要属性值，取出源列表中符合条件的条目，然后通过需要合并的属性仍聚合为 map 数据。
     *
     * @param <K>            返回的 Map 中键的类型
     * @param <V>            待处理列表中元素的类型
     * @param <S>            返回的 Map 中 value 的类型
     * @param originList     待处理列表数据，不能为 null
     * @param keyExtractor   作为 Map 中键的提取逻辑
     * @param valueExtractor 作为 Map 中 value 的提取逻辑
     * @param mergeExtractor 合并两个 Value 为一个 Value 的逻辑
     * @param filters        额外过滤器组，每个过滤器会被 and 组合起来。如果没有过滤器，可以不传
     * @return 经过过滤、转化、聚合操作的 Map
     */
    @SafeVarargs
    public static <K, V, S> Map<K, S> filtersToMergeLinkedMap(Collection<V> originList,
                                                              Function<V, K> keyExtractor,
                                                              Function<V, S> valueExtractor,
                                                              BinaryOperator<S> mergeExtractor,
                                                              Predicate<V>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return originList.stream()
                .filter(Arrays.stream(filters).reduce(Predicate::and)
                        .orElse(t -> true))
                .filter(item -> keyExtractor.apply(item) != null && valueExtractor.apply(item) != null)
                .collect(Collectors.toMap(keyExtractor, valueExtractor, mergeExtractor, LinkedHashMap::new));
    }


    /**
     * 根据某一条目必要属性值，对待处理列表中的数据进行分组操作，返回一个 Map，Map 的键为对应的属性字段，Map 的值为具有对应属性值的列表。
     *
     * @param <K>          返回的 Map 中键的类型
     * @param <V>          待处理列表中元素的类型
     * @param originList   待处理列表数据，不能为 null
     * @param keyExtractor 作为 Map 中键的提取逻辑
     * @return 经过过滤、转化、分组操作的 Map
     */
    @SafeVarargs
    public static <K, V> Map<K, List<V>> filtersGroupByToBeanMap(Collection<V> originList,
                                                                 Function<V, K> keyExtractor,
                                                                 Predicate<V>... filters) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        Map<K, List<V>> map = new HashMap<>(originList.size());
        for (V v : originList) {
            boolean isValid = false;
            for (Predicate<V> filter : filters) {
                if (filter.test(v)) {
                    isValid = true;
                }
            }
            if (isValid) {
                final List<V> vs = map.computeIfAbsent(keyExtractor.apply(v), k -> new ArrayList<>());
                vs.add(v);
            }
        }
        return map;
        // Collectors.groupingBy 会对key使用Objects.requireNonNull校验key不为空
//        return originList.stream()
//                .filter(Arrays.stream(filters).reduce(Predicate::and).orElse(t -> true))
//                .collect(Collectors.groupingBy(keyExtractor));
    }


    /**
     * 根据某一条目必要属性值，对待处理列表中的数据进行分组操作，返回一个 Map，Map 的键为对应的属性字段，Map 的值为具有对应属性值的列表。
     *
     * @param <K>          返回的 Map 中键的类型
     * @param <V>          待处理列表中元素的类型
     * @param originList   待处理列表数据，不能为 null
     * @param keyExtractor 作为 Map 中键的提取逻辑
     * @return 经过过滤、转化、分组操作的 Map
     */
    public static <K, V, U> Map<K, List<U>> mapGroupByToBeanMap(Collection<V> originList,
                                                                Function<V, U> mapper,
                                                                Function<U, K> keyExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        Map<K, List<U>> map = new HashMap<>(originList.size());
        for (V v : originList) {
            final U u = mapper.apply(v);
            final List<U> us = map.computeIfAbsent(keyExtractor.apply(u), k -> new ArrayList<>());
            us.add(u);
        }
        return map;
        // Collectors.groupingBy 会对key使用Objects.requireNonNull校验key不为空
//        return originList.stream()
//                .filter(Arrays.stream(filters).reduce(Predicate::and).orElse(t -> true))
//                .collect(Collectors.groupingBy(keyExtractor));
    }


    /**
     * 根据某一条目必要属性值，对待处理列表中的数据进行分组操作，返回一个 Map，Map 的键为对应的属性字段，Map 的值为具有对应属性值的列表。
     *
     * @param <K>          返回的 Map 中键的类型
     * @param <V>          待处理列表中元素的类型
     * @param originList   待处理列表数据，不能为 null
     * @param keyExtractor 作为 Map 中键的提取逻辑
     * @return 经过过滤、转化、分组操作的 Map
     */
    public static <K, V> Map<K, List<V>> groupByToBeanMap(Collection<V> originList,
                                                          Function<V, K> keyExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        Map<K, List<V>> map = new HashMap<>(originList.size());
        for (V v : originList) {
            final List<V> vs = map.computeIfAbsent(keyExtractor.apply(v), k -> new ArrayList<>());
            vs.add(v);
        }
        return map;
        // Collectors.groupingBy 会对key使用Objects.requireNonNull校验key不为空
//        return originList.stream().collect(Collectors.groupingBy(keyExtractor));
    }

    /**
     * 根据某一条目必要属性值，对待处理列表中的数据进行分组操作，返回一个 Map，Map 的键为对应的属性字段，Map 的值为具有对应属性值的另一个属性列表经过转化逻辑之后得到的数据列表。
     *
     * @param <K>            返回的 Map 中键的类型
     * @param <V>            待处理列表中元素的类型
     * @param <U>            待处理列表中每个元素的另一个属性的类型
     * @param originList     待处理列表数据，不能为 null
     * @param keyExtractor   作为 Map 中键的提取逻辑
     * @param valueExtractor 作为 Map 中 value 的提取逻辑
     * @return 经过过滤、转化、分组操作的 Map
     */
    public static <K, V, U> Map<K, List<U>> groupByToMap(Collection<V> originList,
                                                         Function<V, K> keyExtractor,
                                                         Function<V, U> valueExtractor) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        Map<K, List<U>> map = new HashMap<>(originList.size());
        for (V v : originList) {
            final List<U> us = map.computeIfAbsent(keyExtractor.apply(v), k -> new ArrayList<>());
            us.add(valueExtractor.apply(v));
        }
        return map;
        // Collectors.groupingBy 会对key使用Objects.requireNonNull校验key不为空
//        return originList.stream()
//                .collect(Collectors.groupingBy(keyExtractor, Collectors.mapping(valueExtractor, Collectors.toList())));
    }

    public static <K, V, U extends Comparable<? super U>> List<V> groupByMaxValueToList(Collection<V> originList,
                                                                                        Function<V, K> keyExtractor,
                                                                                        Function<? super V, ? extends U> function) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .collect(Collectors.groupingBy(keyExtractor,
                        Collectors.maxBy(Comparator.comparing(function, Comparator.nullsLast(U::compareTo)))))
                .values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static <K, V, U extends Comparable<? super U>> List<V> groupByMinValueToList(Collection<V> originList,
                                                                                        Function<V, K> keyExtractor,
                                                                                        Function<? super V, ? extends U> function) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        return originList.stream()
                .collect(Collectors.groupingBy(keyExtractor,
                        Collectors.minBy(Comparator.comparing(function, Comparator.nullsLast(U::compareTo)))))
                .values()
                .stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toCollection(ArrayList::new));
    }


    /**
     * 对待处理列表进行升序排列（null 值放在最前面），返回一个新的列表
     *
     * @param <T>        待处理列表中元素的类型
     * @param <U>        待处理列表中每个元素的属性 usedForSort 的类型，必须实现 Comparable 接口
     * @param originList 待处理列表数据，不能为 null
     * @param function   作为元素属性 usedForSort 的提取逻辑
     * @return 排序后的新列表
     */
    public static <T, U extends Comparable<? super U>> List<T> sort(Collection<T> originList,
                                                                    Function<? super T, ? extends U> function,
                                                                    boolean isAsc,
                                                                    boolean isNullLast) {
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new ArrayList<>();
        }
        Comparator<T> comparator;
        if (isNullLast) {
            comparator = Comparator.comparing(function, Comparator.nullsLast(U::compareTo));
        }
        else {
            comparator = Comparator.comparing(function, Comparator.nullsFirst(U::compareTo));
        }

        if (!isAsc) {
            comparator = comparator.reversed();
        }

        Comparator<T> finalComparator = comparator;
        return originList.stream()
                .collect(
                        Collectors.collectingAndThen(Collectors.toCollection(() ->
                                new TreeSet<>(finalComparator)
                        ), ArrayList::new));
    }

    /**
     * 根据某个比较逻辑，计算出两个列表之前的差别，返回一个数组，数组中依次为 符合条件的新增条目列表，符合条件的删除条目列表，符合条件的未变更条目列表
     *
     * @param <T>     待处理列表中元素的类型
     * @param oldList 旧的列表数据，不能为 null
     * @param newList 新的列表数据，不能为 null
     * @param mapper  用来判断元素是否为同一个元素的比较逻辑
     * @return 包含三个 List 的数组，分别为 新增元素列表，删除元素列表，未变更元素列表
     */
    public static <T> PkDiff<T> getDiff(List<T> oldList,
                                        List<T> newList,
                                        BiFunction<T, T, Boolean> mapper) {
        // 计算交集
        List<T> existsList = oldList.stream()
                .filter(s -> newList.stream().anyMatch(t -> mapper.apply(t, s)))
                .collect(Collectors.toCollection(ArrayList::new));

        Map<T, T> map = new LinkedHashMap<>();
        for (T t1 : existsList) {
            for (T t2 : newList) {
                if (mapper.apply(t1, t2)) {
                    map.put(t1, t2);
                    break;
                }
            }
        }

        // 计算差集（新增的对象，即在新列表中，但是不在旧列表中的对象）
        List<T> stayAddIds = newList.stream()
                .filter(s -> existsList.stream().noneMatch(t -> mapper.apply(t, s)))
                .collect(Collectors.toCollection(ArrayList::new));

        // 计算差集（删除的对象，即在旧列表中，但是不在新列表中的对象）
        List<T> stayDelIds = oldList.stream()
                .filter(s -> existsList.stream().noneMatch(t -> mapper.apply(t, s)))
                .collect(Collectors.toCollection(ArrayList::new));
        return new PkDiff<T>(stayAddIds, stayDelIds, map);
    }


    /**
     * 根据某个比较逻辑，计算出两个列表之前的差别，返回一个数组，数组中依次为 符合条件的新增条目列表，符合条件的删除条目列表，符合条件的未变更条目列表
     *
     * @param <T>     待处理列表中元素的类型
     * @param oldList 旧的列表数据，不能为 null
     * @param newList 新的列表数据，不能为 null
     * @param keyExtractor 用来判断元素是否为同一个元素的比较逻辑
     * @return 包含三个 List 的数组，分别为 新增元素列表，删除元素列表，未变更元素列表
     */
    public static <T, R> PkDiff2<T, R> getDiff2(List<T> oldList,
                                               List<R> newList,
                                               BiFunction<T, R, Boolean> keyExtractor) {
         // 计算交集
        List<T> existsList = oldList.stream()
                .filter(s -> newList.stream().anyMatch(t -> keyExtractor.apply(s, t)))
                .collect(Collectors.toCollection(ArrayList::new));

        Map<T, R> map = new LinkedHashMap<>();
        for (T t : existsList) {
            for (R r : newList) {
                if (keyExtractor.apply(t, r)) {
                    map.put(t, r);
                    break;
                }
            }
        }

        // 计算差集（新增的对象，即在新列表中，但是不在旧列表中的对象）
        List<R> addList = newList.stream()
                .filter(s -> existsList.stream().noneMatch(t -> keyExtractor.apply(t, s)))
                .collect(Collectors.toCollection(ArrayList::new));

        // 计算差集（删除的对象，即在旧列表中，但是不在新列表中的对象）
        List<T> delList = oldList.stream()
                .filter(s -> newList.stream().noneMatch(t -> keyExtractor.apply(s, t)))
                .collect(Collectors.toCollection(ArrayList::new));

        return new PkDiff2<>(addList, delList, map);
    }


    // =================================================================================================================
    // =================================================================================================================
    // =================================================================================================================


    // =================================================================================================================
    // =================================================================================================================
    // =================================================================================================================

    public static <T> String getFieldName(PkMap.SFunction<T, ?> sFunction) {
        return PkMap.getField(sFunction);
    }

    public static <T> Method getGetter(Class<T> clazz, PkMap.SFunction<T, ?> sFunction) {
        return BeanUtil.getBeanDesc(clazz).getGetter(getFieldName(sFunction));
    }

    public static <T> Method getGetter(Class<T> clazz, final String fieldName) {
        return BeanUtil.getBeanDesc(clazz).getGetter(fieldName);
    }

    public static <T> LambdaBuilder<T> build(Supplier<T> constructor) {
        return LambdaBuilder.builder(constructor);
    }

    public static <T, U> boolean hasDuplicates(List<T> list, Function<T, U> keyExtractor) {
        return list.stream().map(keyExtractor).distinct().count() != list.size();
    }

    public static <S extends Serializable> boolean hasDuplicates(List<S> list) {
        return list.stream().distinct().count() != list.size();
    }

    public static <K, S, V> Map<K, S> filtersToMap(Collection<V> originList, Function<V, K> keyExtractor, Function<V, S> valueExtractor, Predicate<V>[] filters) {
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        originList = removeNull(originList);
        if (CollUtil.isEmpty(originList)) {
            return new HashMap<>();
        }
        return Arrays.stream(filters)
                .reduce(Predicate::and)
                .map(originList.stream()::filter)
                .orElse(Stream.empty())
                .filter(item -> keyExtractor.apply(item) != null && valueExtractor.apply(item) != null)
                .collect(Collectors.toMap(keyExtractor, valueExtractor, (k1, k2) -> k1));
    }
}
