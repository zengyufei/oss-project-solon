package com.zyf.utils;

import cn.hutool.core.bean.BeanDesc;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.IterUtil;
import cn.hutool.core.comparator.CompareUtil;
import cn.hutool.core.convert.ConverterRegistry;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.zyf.common.enums.BaseEnum;
import com.zyf.utils.bean.BeanValidators;
import com.zyf.utils.converters.CustomStringConverter;
import com.zyf.utils.lambda.*;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.core.handle.Context;
import sun.misc.Unsafe;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 工具类
 *
 * @author zengyufei
 * @date 2021/11/22
 */
@Slf4j
public class PkUtil {

    private final static Pattern compile = Pattern.compile("(.*?)(\\d+)(\\.\\d+)?");

    private PkUtil() {
    }


    static {
        ConverterRegistry converterRegistry = ConverterRegistry.getInstance();
        // 此处做为示例自定义String转换，因为Hutool中已经提供String转换，请尽量不要替换
        // 替换可能引发关联转换异常（例如覆盖String转换会影响全局）
        converterRegistry.putCustom(String.class, CustomStringConverter.class);
    }


    public static boolean containsAny(Object a, Object b) {
        return ContainsAnyUtil.containsAny(a, b, true);
    }

    public static boolean containsAny(Object a, Object b, boolean ignoreCase) {
        return ContainsAnyUtil.containsAny(a, b, ignoreCase);
    }

    public static boolean eqAny(Object a, Object b) {
        return EqUtil.eq(a, b, true);
    }

    public static boolean eqAny(Object a, Object b, boolean ignoreCase) {
        return EqAnyUtil.eqAny(a, b, ignoreCase);
    }

    public static boolean eq(Object a, Object b) {
        return EqAnyUtil.eqAny(a, b, true);
    }

    public static boolean eq(Object a, Object b, boolean ignoreCase) {
        return EqUtil.eq(a, b, ignoreCase);
    }

    public static boolean eqIgnoreCase(Object a, Object b, Consumer<Boolean> run) {
        final boolean eq = EqUtil.eq(a, b, true);
        run.accept(eq);
        return eq;
    }

    public static boolean isNotNull(Object value) {
        return !isNull(value);
    }

    public static boolean isEmpty(Object value) {
        return isNull(value);
    }

    public static boolean isNotEmpty(Object value) {
        return isNotNull(value);
    }

    public static boolean isNull(Object value) {
        // 基本数据类型包装类、数字类型、日期类型三种、对象
        if (ObjectUtil.isNull(value)) {
            return true;
        }
        // 基本数据类型
        else if (value.getClass().isPrimitive()) {
            return false;
        }
        // 空字符串，null字符串, undefined字符串
        else if (value instanceof CharSequence) {
            return StrUtil.isBlankOrUndefined((CharSequence) value);
        }
        // 数组
        else if (ArrayUtil.isArray(value)) {
            return ArrayUtil.isEmpty(value);
        }
        // 集合
        else if (value instanceof Iterable) {
            return IterUtil.isEmpty((Iterable<?>) value);
        }
        // 哈希结构
        else if (value instanceof Map) {
            return MapUtil.isEmpty((Map<?, ?>) value);
        }
        else {
            return ObjectUtil.isNull(value);
        }
    }

    /**
     * 检查枚举范围
     *
     * @param enumClass 枚举类
     * @param values    值
     */
    public static <T extends Enum<?>> void checkEnumRange(Class<T> enumClass,
                                                          Function<T, String> keyExtractor,
                                                          Function<String, String> errorMsgConsumer,
                                                          String... values) {
        T[] enumConstants = enumClass.getEnumConstants();
        String tip = Arrays.stream(enumConstants)
                .map(e -> keyExtractor.apply(e) + "=" + e.name())
                .collect(Collectors.joining(", "));
        for (String value : values) {
            boolean inEnum = Arrays.stream(enumConstants)
                    .anyMatch(e -> keyExtractor.apply(e).equals(value));
            if (!inEnum) {
                throw new RuntimeException(errorMsgConsumer.apply(tip));
            }
        }
    }

    /**
     * 构建树
     *
     * @param originList            平面列表
     * @param getIdFieldValue       获取id
     * @param getParentIdFieldValue 获取父id
     * @param childrenFieldName     字段名
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> buildTree(Collection<T> originList,
                                        Function<String, Boolean> parentIdIsRootCheck,
                                        Function<T, String> getIdFieldValue,
                                        Function<T, String> getParentIdFieldValue,
                                        String childrenFieldName) {
        //按原数组顺序构建父级数据Map，使用Optional考虑pId为null
        Map<String, List<T>> childrenMap = LamUtil.groupByToBeanMap(originList, getParentIdFieldValue);
        List<T> result = new ArrayList<>();
        for (T node : originList) {
            final String id = getIdFieldValue.apply(node);
            final String parentId = getParentIdFieldValue.apply(node);
            //添加到下级数据中
            if (childrenMap.containsKey(id)) {
                setChildrenProperty(node, childrenMap.get(id), childrenFieldName);
            }
            //如里是根节点，加入结构
            if (parentIdIsRootCheck.apply(parentId)) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * 构建树
     *
     * @param originList            平面列表
     * @param getIdFieldValue       获取id
     * @param getParentIdFieldValue 获取父id
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> buildTree(Collection<T> originList,
                                        Function<String, Boolean> parentIdIsRootCheck,
                                        Function<T, String> getIdFieldValue,
                                        Function<T, String> getParentIdFieldValue,
                                        BiConsumer<T, List<T>> setChildrenProperty) {
        //按原数组顺序构建父级数据Map，使用Optional考虑pId为null
        Map<String, List<T>> childrenMap = LamUtil.groupByToBeanMap(originList, getParentIdFieldValue);

        List<T> result = new ArrayList<>();
        for (T node : originList) {
            final String id = getIdFieldValue.apply(node);
            final String parentId = getParentIdFieldValue.apply(node);
            //添加到下级数据中
            if (childrenMap.containsKey(id)) {
                setChildrenProperty.accept(node, childrenMap.get(id));
            }
            //如里是根节点，加入结构
            if (parentIdIsRootCheck.apply(parentId)) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * 构建树
     *
     * @param originList            平面列表
     * @param getIdFieldValue       获取id
     * @param getParentIdFieldValue 获取父id
     * @return {@link List}<{@link T}>
     */
    public static <T> List<JSONObject> buildTree(Collection<T> originList,
                                                 Function<String, Boolean> parentIdIsRootCheck,
                                                 PkMap.SFunction<T, String> getIdFieldValue,
                                                 Function<T, String> getParentIdFieldValue) {
        return buildTree(originList, parentIdIsRootCheck, getIdFieldValue, getParentIdFieldValue, "children");
    }

    /**
     * 构建树
     *
     * @param originList            平面列表
     * @param getIdFieldValue       获取id
     * @param getParentIdFieldValue 获取父id
     * @return {@link List}<{@link JSONObject}>
     */
    public static List<JSONObject> buildJsonTree(Collection<JSONObject> originList,
                                                 Function<String, Boolean> parentIdIsRootCheck,
                                                 Function<JSONObject, String> getIdFieldValue,
                                                 Function<JSONObject, String> getParentIdFieldValue) {
        final String childrenFieldName = "children";

        Map<String, List<JSONObject>> childrenMap = LamUtil.groupByToBeanMap(originList, getParentIdFieldValue);
        List<JSONObject> rootNodes = new ArrayList<>();
        for (JSONObject node : originList) {
            //添加到下级数据中
            final String id = getIdFieldValue.apply(node);
            final String parentId = getParentIdFieldValue.apply(node);
            final JSONArray children = (JSONArray) node.computeIfAbsent(childrenFieldName, k -> new JSONArray());
            if (childrenMap.containsKey(id)) {
                children.addAll(childrenMap.get(id));
            }
            //如里是根节点，加入结构
            if (parentIdIsRootCheck.apply(parentId)) {
                rootNodes.add(node);
            }
        }
        return rootNodes;
    }

    /**
     * 构建树
     *
     * @param originList            平面列表
     * @param getIdFieldValue       获取id
     * @param getParentIdFieldValue 获取父id
     * @return {@link List}<{@link T}>
     */
    public static <T> List<JSONObject> buildTree(Collection<T> originList,
                                                 Function<String, Boolean> parentIdIsRootCheck,
                                                 PkMap.SFunction<T, String> getIdFieldValue,
                                                 Function<T, String> getParentIdFieldValue,
                                                 String childrenFieldName) {

        final Map<String, JSONObject> nodeMap = new LinkedHashMap<>();
        final Map<String, List<JSONObject>> childrenMap = new LinkedHashMap<>();

        // 第一次遍历：构建节点映射和子节点映射
        for (T item : originList) {
            final String id = getIdFieldValue.apply(item);
            final String parentId = getParentIdFieldValue.apply(item);
            final JSONObject node = new JSONObject(item);

            nodeMap.put(id, node);
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(node);
        }

        final List<JSONObject> rootNodes = new ArrayList<>();

        // 第二次遍历：构建树结构
        for (T item : originList) {
            final String id = getIdFieldValue.apply(item);
            final String parentId = getParentIdFieldValue.apply(item);
            final JSONObject node = nodeMap.get(id);

            // 添加子节点
            final List<JSONObject> children = childrenMap.get(id);
            if (children != null && !children.isEmpty()) {
                node.put(childrenFieldName, new JSONArray(children));
            }

            // 如果是根节点，添加到结果列表
            if (parentIdIsRootCheck.apply(parentId)) {
                rootNodes.add(node);
            }
        }

        return rootNodes;
    }


    /**
     * 设置children属性
     *
     * @param node              节点
     * @param children          儿童
     * @param childrenFieldName 字段名
     */
    private static <T> void setChildrenProperty(T node, List<T> children, String childrenFieldName) {
        // 这里根据具体情况设置子节点属性，例如使用反射设置属性
        try {
            Field childrenField = node.getClass().getDeclaredField(childrenFieldName);
            childrenField.setAccessible(true);
            childrenField.set(node, children);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对树所有子节点按comparator排序
     *
     * @param tree        需要排序的树
     * @param comparator  排序规则Comparator，如：Comparator.comparing(MenuVo::getRank)按Rank正序 ,(x,y)->y.getRank().compareTo(x.getRank())，按Rank倒序
     * @param getChildren 获取下级数据方法，如：MenuVo::getSubMenus
     * @param <T>         泛型实体对象
     * @return 排序好的树
     */
    public static <T> List<T> sort(List<T> tree, Comparator<? super T> comparator, Function<T, List<T>> getChildren) {
        for (T item : tree) {
            List<T> childList = getChildren.apply(item);
            if (childList != null && !childList.isEmpty()) {
                sort(childList, comparator, getChildren);
            }
        }

        tree.sort(comparator);
        return tree;
    }


    /**
     * 将树打平成tree
     *
     * @param tree           需要打平的树
     * @param getSubChildren 设置下级数据方法，如： Menu::getSubMenus,x->x.setSubMenus(null)
     * @param setSubChildren 将下级数据置空方法，如： x->x.setSubMenus(null)
     * @param <E>            泛型实体对象
     * @return 打平后的数据
     */
    public static <E> List<E> flat(List<E> tree, Function<E, List<E>> getSubChildren, Consumer<E> setSubChildren) {
        List<E> res = new ArrayList<>();
        forPostOrder(tree, item -> {
            setSubChildren.accept(item);
            res.add(item);
        }, getSubChildren);
        return res;
    }


    /**
     * 前序遍历
     *
     * @param tree           需要遍历的树
     * @param consumer       遍历后对单个元素的处理方法，如：x-> System.out.println(x)、 System.out::println打印元素
     * @param setSubChildren 设置下级数据方法，如： Menu::getSubMenus,x->x.setSubMenus(null)
     * @param <E>            泛型实体对象
     */
    public static <E> void forPreOrder(List<E> tree, Consumer<E> consumer, Function<E, List<E>> setSubChildren) {
        for (E l : tree) {
            consumer.accept(l);
            List<E> es = setSubChildren.apply(l);
            if (es != null && es.size() > 0) {
                forPreOrder(es, consumer, setSubChildren);
            }
        }
    }


    /**
     * 层序遍历
     *
     * @param tree           需要遍历的树
     * @param consumer       遍历后对单个元素的处理方法，如：x-> System.out.println(x)、 System.out::println打印元素
     * @param setSubChildren 设置下级数据方法，如： Menu::getSubMenus,x->x.setSubMenus(null)
     * @param <E>            泛型实体对象
     */
    public static <E> void forLevelOrder(List<E> tree, Consumer<E> consumer, Function<E, List<E>> setSubChildren) {
        Queue<E> queue = new LinkedList<>(tree);
        while (!queue.isEmpty()) {
            E item = queue.poll();
            consumer.accept(item);
            List<E> childList = setSubChildren.apply(item);
            if (childList != null && !childList.isEmpty()) {
                queue.addAll(childList);
            }
        }
    }


    /**
     * 后序遍历
     *
     * @param tree           需要遍历的树
     * @param consumer       遍历后对单个元素的处理方法，如：x-> System.out.println(x)、 System.out::println打印元素
     * @param setSubChildren 设置下级数据方法，如： Menu::getSubMenus,x->x.setSubMenus(null)
     * @param <E>            泛型实体对象
     */
    public static <E> void forPostOrder(List<E> tree, Consumer<E> consumer, Function<E, List<E>> setSubChildren) {
        for (E item : tree) {
            List<E> childList = setSubChildren.apply(item);
            if (childList != null && !childList.isEmpty()) {
                forPostOrder(childList, consumer, setSubChildren);
            }
            consumer.accept(item);
        }
    }

    // =================================================================================================================
    // =================================================================================================================
    // =================================================================================================================

    public static <T> List<T>[] getChangeCudAttr(List<T> oldList, List<T> newList) {
        // 交集
        final List<T> existsList = (List<T>) CollUtil.intersection(oldList, newList);
        // 待删除，与交集单差集
        final List<T> stayAddIds = CollUtil.subtractToList(newList, existsList);
        // 待删除，与交集单差集
        final List<T> stayDelIds = CollUtil.subtractToList(oldList, existsList);
        return new List[]{stayAddIds, stayDelIds, existsList};
    }

    /**
     * 计算两个对象列表的交集、并集和差集
     *
     * @param oldList 旧对象列表
     * @param newList 新对象列表
     * @param mapper  判断两个对象是否相等的函数式接口
     * @param <T>     对象类型
     * @return 包含三个列表的数组，分别为差集中需要添加的对象列表、差集中需要删除的对象列表和交集的对象列表
     */
    public static <T> PkDiff<T> getDiff(List<T> oldList,
                                        List<T> newList,
                                        BiFunction<T, T, Boolean> mapper) {
        return LamUtil.getDiff(oldList, newList, mapper);
    }


    /**
     * 计算两个对象列表的交集、并集和差集
     *
     * @param oldList 旧对象列表
     * @param newList 新对象列表
     * @param keyExtractor 判断两个对象是否相等的函数式接口
     * @param <T>     对象类型
     * @return 包含三个列表的数组，分别为差集中需要添加的对象列表、差集中需要删除的对象列表和交集的对象列表
     */
    public static <T, R> PkDiff2<T, R> getDiff2(List<T> oldList,
                                                List<R> newList,
                                                BiFunction<T, R, Boolean> keyExtractor) {
        return LamUtil.getDiff2(oldList, newList, keyExtractor);
    }

    public static Map getStaticValueAsMap(Class<?> targetClass, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        // 这里必须预先实例化对象,否则它的静态字段不会加载
        Field name = targetClass.getField(fieldName);
        // 注意，上面的Field实例是通过Class获取的，但是下面的获取静态属性的值没有依赖到Class
        return (Map) unsafe.getObject(unsafe.staticFieldBase(name), unsafe.staticFieldOffset(name));
    }

    public static String getStaticValueAsString(Class<?> targetClass, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
        theUnsafe.setAccessible(true);
        Unsafe unsafe = (Unsafe) theUnsafe.get(null);
        // 这里必须预先实例化对象,否则它的静态字段不会加载
        Field name = targetClass.getField(fieldName);
        // 注意，上面的Field实例是通过Class获取的，但是下面的获取静态属性的值没有依赖到Class
        return (String) unsafe.getObject(unsafe.staticFieldBase(name), unsafe.staticFieldOffset(name));
    }


    // =================================================================================================================
    // =================================================================================================================
    // =================================================================================================================

    /**
     * 拼接
     *
     * @param originList 源列表
     * @param delimiter  分隔符
     * @return {@link String}
     */
    public static String listJoinToStr(List<String> originList, String delimiter) {
        return LamUtil.join(originList, delimiter);
    }

    /**
     * 拼接
     *
     * @param originList 源列表
     * @param delimiter  分隔符
     * @param mapper     方法
     * @return {@link String}
     */
    public static <T> String listJoinToStr(Collection<T> originList, String delimiter, Function<T, String> mapper) {
        return LamUtil.join(originList, delimiter, mapper);
    }


    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param filters    映射规则集合
     * @param <T>        原数据的元素类型
     * @return List<T>
     */
    @SafeVarargs
    public static <T> List<T> listFiltersToList(Collection<T> originList,
                                                Predicate<T>... filters) {
        return LamUtil.filtersToList(originList, filters);
    }

    public static <T, U> List<T> listFilterMatchToList(Collection<T> originList,
                                                       Collection<U> targetList,
                                                       Function<T, String> sourceKeyExtractor,
                                                       Function<U, String> filterKeyExtractor) {
        return LamUtil.filterMatchToList(originList, targetList, sourceKeyExtractor, filterKeyExtractor);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param filters    映射规则集合
     * @param <T>        原数据的元素类型
     * @return List<T>
     */
    @SafeVarargs
    public static <T> List<T> listFilterDistinctToList(Collection<T> originList,
                                                       Predicate<T>... filters) {
        return LamUtil.filterDistinctToList(originList, filters);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param filters    映射规则集合
     * @param <T>        原数据的元素类型
     * @return List<T>
     */
    @SafeVarargs
    public static <T, U extends Comparable<? super U>> List<T> listFilterDistinctsToList(Collection<T> originList,
                                                                                         Function<T, U> function,
                                                                                         Predicate<T>... filters) {
        return LamUtil.filterDistinctsToList(originList, function, filters);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param filters    映射规则集合
     * @param <T>        原数据的元素类型
     * @return List<T>
     */
    @SafeVarargs
    public static <T, R> List<R> listFiltersMapToList(Collection<T> originList,
                                                      Function<T, R> function,
                                                      Predicate<T>... filters) {
        return LamUtil.filtersMapToList(originList, function, filters);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param filters    映射规则集合
     * @param <T>        原数据的元素类型
     * @return List<T>
     */
    @SafeVarargs
    public static <T, R> List<R> listFiltersDistinctMapToList(Collection<T> originList,
                                                              Function<T, R> function,
                                                              Predicate<T>... filters) {
        return LamUtil.filtersDistinctMapToList(originList, function, filters);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param filters    映射规则集合
     * @param <T>        原数据的元素类型
     * @return List<T>
     */
    @SafeVarargs
    public static <T, R> List<R> listFiltersMapDistinctToList(Collection<T> originList,
                                                              Function<T, R> function,
                                                              Predicate<T>... filters) {
        return LamUtil.filtersMapDistinctToList(originList, function, filters);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param filters    映射规则集合
     * @return List<T>
     */
    @SafeVarargs
    public static <R> List<R> listFiltersBlankDistinctMapToList(List<String> originList,
                                                                Function<String, R> function,
                                                                Predicate<String>... filters) {
        return LamUtil.filterBlankDistinctMapToList(originList, function, filters);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param filters    映射规则集合
     * @param <T>        原数据的元素类型
     * @return List<T>
     */
    @SafeVarargs
    public static <T> List<String> listMapFiltersBlankDistinctToList(Collection<T> originList,
                                                                     Function<T, String> function,
                                                                     Predicate<String>... filters) {
        return LamUtil.mapFiltersBlankDistinctToList(originList, function, filters);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param filters    映射规则集合
     * @return List<T>
     */
    @SafeVarargs
    public static List<String> listFiltersBlankDistinctToList(List<String> originList,
                                                              Predicate<String>... filters) {
        return LamUtil.filterBlankDistinctToList(originList, filters);
    }

    /**
     * list根据对象指定属性去重
     *
     * @param originList：要操作的list集合
     * @param keyExtractor:         去重属性
     * @return List<T>
     */
    public static <T> List<T> listDistinctTolist(Collection<T> originList, Function<? super T, ?> keyExtractor) {
        return LamUtil.distinctToList(originList, keyExtractor);
    }

    /**
     * 删除空
     *
     * @param originList 列表
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> listRemoveNullToList(Collection<T> originList) {
        return LamUtil.removeNull(originList);
    }

    /**
     * 过滤移除
     *
     * @param originList       源列表
     * @param removeConditions 删除条件
     * @return {@link List}<{@link T}>
     */
    @SafeVarargs
    public static <T> List<T> listRemoveFiltersToList(Collection<T> originList, Predicate<? super T>... removeConditions) {
        return LamUtil.removeFilter(originList, removeConditions);
    }

    /**
     * 找到
     *
     * @param originList 原数据
     * @param filters    映射规则集合
     * @param <T>        原数据的元素类型
     * @return Optional<T>
     */
    @SafeVarargs
    public static <T> Optional<T> listFiltersToFindFirstOptional(Collection<T> originList,
                                                                 Predicate<T>... filters) {
        return LamUtil.filtersToFindFirstOptional(originList, filters);
    }

    @SafeVarargs
    public static <T> T listFiltersToFindFirst(Collection<T> originList,
                                               Predicate<T>... filters) {
        return LamUtil.filtersToFindFirst(originList, filters);
    }

    @SafeVarargs
    public static <T, U> U listMapFiltersToFindFirst(Collection<T> originList,
                                                     Function<T, U> mapper,
                                                     Predicate<U>... filters) {
        return LamUtil.mapFiltersToFindFirst(originList, mapper, filters);
    }


    /**
     * 匹配
     *
     * @param originList 原数据
     * @param mapper     映射规则
     * @param <T>        原数据的元素类型
     * @return boolean
     */
    public static <T> boolean listAnyMatch(Collection<T> originList,
                                           Predicate<T> mapper) {
        return LamUtil.anyMatch(originList, mapper);
    }


    /**
     * 匹配
     *
     * @param originList 原数据
     * @param mapper     映射规则
     * @param <T>        原数据的元素类型
     * @return boolean
     */
    public static <T> boolean listNoneMatch(Collection<T> originList,
                                            Predicate<T> mapper) {
        return LamUtil.noneMatch(originList, mapper);
    }


    /**
     * 不匹配
     *
     * @param str 字符
     * @param fns 实体字段
     * @param <T> 原数据的元素类型
     * @return boolean
     */
    @SafeVarargs
    public static <T> boolean strNotInList(String str, PkMap.SFunction<T, ?>... fns) {
        return !strInList(str, fns);
    }

    /**
     * 拷贝时过滤
     *
     * @param fns 实体字段
     * @param <T> 原数据的元素类型
     * @return boolean
     */
    @SafeVarargs
    public static <T> CopyOptions copyIgnoreProperties(PkMap.SFunction<T, ?>... fns) {
        return CopyOptions.create().setPropertiesFilter((field, o) -> PkUtil.strNotInList(field.getName(), fns));
    }

    /**
     * 匹配
     *
     * @param str 字符
     * @param fns 实体字段
     * @param <T> 原数据的元素类型
     * @return boolean
     */
    @SafeVarargs
    public static <T> boolean strInList(String str,
                                        PkMap.SFunction<T, ?>... fns) {
        return PkUtil.getFieldNames(fns).contains(str);
    }

    /**
     * map后match
     *
     * @param originList 原数据
     * @param mapper     映射规则
     * @param <T>        原数据的元素类型
     * @param <R>        新数据的元素类型
     * @return boolean
     */
    public static <T, R> boolean listMapAnyMatch(T[] originList,
                                                 Function<T, R> mapper,
                                                 Predicate<R> predicate) {
        return LamUtil.mapAnyMatch(Arrays.asList(originList), mapper, predicate);
    }

    /**
     * map后match
     *
     * @param originList 原数据
     * @param mapper     映射规则
     * @param <T>        原数据的元素类型
     * @param <R>        新数据的元素类型
     * @return boolean
     */
    public static <T, R> boolean listMapAnyMatch(Collection<T> originList,
                                                 Function<T, R> mapper,
                                                 Predicate<R> predicate) {
        return LamUtil.mapAnyMatch(originList, mapper, predicate);
    }

    /**
     * map后match
     *
     * @param originList 原数据
     * @param mapper     映射规则
     * @param <T>        原数据的元素类型
     * @param <R>        新数据的元素类型
     * @return boolean
     */
    public static <T, R> boolean listMapDistinctAnyMatch(Collection<T> originList,
                                                         Function<T, R> mapper,
                                                         Predicate<R> predicate) {
        return LamUtil.mapDistinctAnyMatch(originList, mapper, predicate);
    }

    @SafeVarargs
    public static <S, T, R> List<T> listMapValuesFlatToList(Map<S, List<R>> origin,
                                                            Function<R, T>... filters) {
        return LamUtil.mapValuesFlatToList(origin, filters);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param filters    映射规则
     * @param <T>        原数据的元素类型
     * @param <R>        新数据的元素类型
     * @return List<R>
     */
    @SafeVarargs
    public static <T, R> List<R> listMapToList(Collection<T> originList, Function<T, R>... filters) {
        return LamUtil.mapToList(originList, filters);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param filters    映射规则
     * @param <T>        原数据的元素类型
     * @param <R>        新数据的元素类型
     * @return List<R>
     */
    @SafeVarargs
    public static <T, R> List<R> listMapDistinctToList(Collection<T> originList, Function<T, R>... filters) {
        return LamUtil.mapDistinctToList(originList, filters);
    }

    public static <T> List<T> listDistinctToList(Collection<T> originList) {
        return LamUtil.distinctToList(originList);
    }

    @SafeVarargs
    public static <T, R> List<R> listMapFiltersDistinctToList(Collection<T> originList,
                                                              Function<T, R> mapper,
                                                              Predicate<R>... filters) {
        return LamUtil.mapDistinctFiltersToList(originList, mapper, filters);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param mapper     映射规则
     * @param <T>        原数据的元素类型
     * @param <R>        新数据的元素类型
     * @return List<R>
     */
    @SuppressWarnings({"all"})
    public static <T, R> List<R> mapFiltersDistinctToList(Collection<T> originList,
                                                          Function<T, R> mapper,
                                                          Predicate<R>... filters) {
        return LamUtil.mapFiltersDistinctToList(originList, mapper, filters);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param mapper     映射规则
     * @param <T>        原数据的元素类型
     * @param <R>        新数据的元素类型
     * @return List<R>
     */
    @SuppressWarnings({"all"})
    public static <T, R> List<R> listMapFiltersToList(Collection<T> originList,
                                                      Function<T, R> mapper,
                                                      Predicate<R>... filters) {
        return LamUtil.mapFiltersToList(originList, mapper, filters);
    }

    /**
     * 将List映射为List，比如List<Person> personList转为List<String> nameList
     *
     * @param originList 原数据
     * @param mapper     映射规则
     * @param <T>        原数据的元素类型
     * @param <R>        新数据的元素类型
     * @return List<R>
     */
    @SuppressWarnings({"all"})
    public static <T, R> List<R> listMapDistinctFiltersToList(Collection<T> originList,
                                                              Function<T, R> mapper,
                                                              Predicate<R>... filters) {
        return LamUtil.mapDistinctFiltersToList(originList, mapper, filters);
    }


    @SafeVarargs
    public static <T> long listCount(List<T> taskOrders, Predicate<T>... filters) {
        return LamUtil.count(taskOrders, filters);
    }

    /**
     * 将List转为Map
     *
     * @param originList   原数据
     * @param keyExtractor Key的抽取规则
     * @param <K>          Key
     * @param <V>          Value
     * @return Map<K, V>
     */
    public static <K, V> Map<K, V> listToBeanMap(Collection<V> originList,
                                                 Function<V, K> keyExtractor) {
        return LamUtil.toBeanMap(originList, keyExtractor);
    }

    /**
     * 将List转为Map
     *
     * @param originList   原数据
     * @param keyExtractor Key的抽取规则
     * @param <K>          Key
     * @param <V>          Value
     * @return Map<K, V>
     */
    public static <K, V> void listToBeanMap(Map<K, V> map, Collection<V> originList,
                                            Function<V, K> keyExtractor) {
        map.putAll(LamUtil.toBeanMap(originList, keyExtractor));
    }

    public static <K, V> Map<K, V> listToBeanLinkedMap(Collection<V> originList,
                                                       Function<V, K> keyExtractor) {

        return LamUtil.toBeanLinkedMap(originList, keyExtractor);
    }

    /**
     * 将List转为Map
     *
     * @param originList   原数据
     * @param keyExtractor Key的抽取规则
     * @param <K>          Key
     * @param <V>          Value
     * @return Map<K, V>
     */
    public static <K, V, S> Map<K, S> listToMap(Collection<V> originList,
                                                Function<V, K> keyExtractor,
                                                Function<V, S> valueExtractor) {
        return LamUtil.toMap(originList, keyExtractor, valueExtractor);
    }

    public static <K, V, S> Map<K, S> listFilterToMap(Collection<V> originList,
                                                      Predicate<V> filter,
                                                      Function<V, K> keyExtractor,
                                                      Function<V, S> valueExtractor) {
        return LamUtil.filterToMap(originList, filter, keyExtractor, valueExtractor);
    }

    public static <K, V> Map<K, V> listFilterToBeanMap(Collection<V> originList,
                                                       Predicate<V> filter,
                                                       Function<V, K> keyExtractor) {
        return LamUtil.filterToBeanMap(originList, filter, keyExtractor);
    }

    public static <K, V> Map<K, V> listFilterToBeanMergeMap(Collection<V> originList,
                                                            Predicate<V> filter,
                                                            Function<V, K> keyExtractor,
                                                            BinaryOperator<V> mergeExtractor) {
        return LamUtil.filterToBeanMergeMap(originList, filter, keyExtractor, mergeExtractor);
    }

    public static <K, V> Map<K, V> listFiltersToBeanMap(Collection<V> originList,
                                                        Function<V, K> keyExtractor,
                                                        Predicate<V>... filters) {
        return LamUtil.filtersToBeanMap(originList, keyExtractor, filters);
    }

    public static <K, V, S> Map<K, S> listFiltersToMap(Collection<V> originList,
                                                       Function<V, K> keyExtractor,
                                                       Function<V, S> valueExtractor,
                                                       Predicate<V>... filters) {
        return LamUtil.filtersToMap(originList, keyExtractor, valueExtractor, filters);
    }

    public static <K, V, S> Map<K, S> listToLinkedMap(Collection<V> originList,
                                                      Function<V, K> keyExtractor,
                                                      Function<V, S> valueExtractor) {
        return LamUtil.toLinkedMap(originList, keyExtractor, valueExtractor);

    }

    /**
     * 将List转为Map
     *
     * @param originList   原数据
     * @param keyExtractor Key的抽取规则
     * @param <K>          Key
     * @param <V>          Value
     * @return Map<K, V>
     */
    public static <K, V, S> Map<K, S> listToMergeMap(Collection<V> originList,
                                                     Function<V, K> keyExtractor,
                                                     Function<V, S> valueExtractor,
                                                     BinaryOperator<S> mergeExtractor) {
        return LamUtil.toMergeMap(originList, keyExtractor, valueExtractor, mergeExtractor);
    }

    public static <K, V, S> Map<K, S> listToMergeLinkedMap(Collection<V> originList,
                                                           Function<V, K> keyExtractor,
                                                           Function<V, S> valueExtractor,
                                                           BinaryOperator<S> mergeExtractor) {
        return LamUtil.toMergeLinkedMap(originList, keyExtractor, valueExtractor, mergeExtractor);
    }


    /**
     * 将List转为Map
     *
     * @param originList   原数据
     * @param keyExtractor Key的抽取规则
     * @param <K>          Key
     * @param <V>          Value
     * @return Map<K, V>
     */
    public static <K, V> Map<K, V> listToBeanMergeMap(Collection<V> originList,
                                                      Function<V, K> keyExtractor,
                                                      BinaryOperator<V> mergeExtractor) {
        return LamUtil.toBeanMergeMap(originList, keyExtractor, mergeExtractor);
    }

    public static <K, V> Map<K, V> listToBeanMergeLinkedMap(Collection<V> originList,
                                                            Function<V, K> keyExtractor,
                                                            BinaryOperator<V> mergeExtractor) {
        return LamUtil.toBeanMergeLinkedMap(originList, keyExtractor, mergeExtractor);
    }


    /**
     * 将List转为Map
     *
     * @param originList   原数据
     * @param keyExtractor Key的抽取规则
     * @param <K>          Key
     * @param <V>          Value
     * @return Map<K, V>
     */
    public static <K, V, S> Map<K, S> listFilterToMergeMap(Collection<V> originList,
                                                           Predicate<V> filter,
                                                           Function<V, K> keyExtractor,
                                                           Function<V, S> valueExtractor,
                                                           BinaryOperator<S> mergeExtractor) {
        return LamUtil.filterToMergeMap(originList, filter, keyExtractor, valueExtractor, mergeExtractor);
    }

    public static <K, V, S> Map<K, S> listFilterToMergeLinkedMap(Collection<V> originList,
                                                                 Predicate<V> filter,
                                                                 Function<V, K> keyExtractor,
                                                                 Function<V, S> valueExtractor,
                                                                 BinaryOperator<S> mergeExtractor) {
        return LamUtil.filterToMergeLinkedMap(originList, filter, keyExtractor, valueExtractor, mergeExtractor);
    }

    /**
     * 将List转为Map
     *
     * @param originList   原数据
     * @param keyExtractor Key的抽取规则
     * @param <K>          Key
     * @param <V>          Value
     * @return Map<K, V>
     */
    @SafeVarargs
    public static <K, V, S> Map<K, S> listFiltersToMergeMap(Collection<V> originList,
                                                            Function<V, K> keyExtractor,
                                                            Function<V, S> valueExtractor,
                                                            BinaryOperator<S> mergeExtractor,
                                                            Predicate<V>... filters) {
        return LamUtil.filtersToMergeMap(originList, keyExtractor, valueExtractor, mergeExtractor, filters);
    }

    @SafeVarargs
    public static <K, V, S> Map<K, S> listFiltersToMergeLinkedMap(Collection<V> originList,
                                                                  Function<V, K> keyExtractor,
                                                                  Function<V, S> valueExtractor,
                                                                  BinaryOperator<S> mergeExtractor,
                                                                  Predicate<V>... filters) {
        return LamUtil.filtersToMergeLinkedMap(originList, keyExtractor, valueExtractor, mergeExtractor, filters);
    }

    /**
     * 将List分组
     *
     * @param originList   原数据
     * @param keyExtractor Key的抽取规则
     * @param <K>          Key
     * @param <V>          Value
     * @return Map<K, List < V>>
     */
    public static <K, V> Map<K, List<V>> listGroupByToBeanMap(Collection<V> originList,
                                                              Function<V, K> keyExtractor) {
        return LamUtil.groupByToBeanMap(originList, keyExtractor);
    }

    /**
     * 将List分组
     *
     * @param originList   原数据
     * @param keyExtractor Key的抽取规则
     * @param <K>          Key
     * @param <V>          Value
     * @return Map<K, List < V>>
     */
    @SafeVarargs
    public static <K, V> Map<K, List<V>> listFiltersGroupByToBeanMap(Collection<V> originList,
                                                                     Function<V, K> keyExtractor,
                                                                     Predicate<V>... filters) {
        return LamUtil.filtersGroupByToBeanMap(originList, keyExtractor, filters);
    }

    /**
     * 将List分组
     *
     * @param originList   原数据
     * @param keyExtractor Key的抽取规则
     * @param <K>          Key
     * @param <V>          Value
     * @return Map<K, List < V>>
     */
    public static <K, V, U> Map<K, List<U>> listMapGroupByToBeanMap(Collection<V> originList,
                                                                    Function<V, U> mapper,
                                                                    Function<U, K> keyExtractor) {
        return LamUtil.mapGroupByToBeanMap(originList, mapper, keyExtractor);
    }

    public static <K, V, U extends Comparable<? super U>> List<V> listGroupByMaxValueToList(Collection<V> originList,
                                                                                            Function<V, K> keyExtractor,
                                                                                            Function<? super V, ? extends U> function) {
        return LamUtil.groupByMaxValueToList(originList, keyExtractor, function);
    }

    public static <K, V, U extends Comparable<? super U>> List<V> listGroupByMinValueToList(Collection<V> originList,
                                                                                            Function<V, K> keyExtractor,
                                                                                            Function<? super V, ? extends U> function) {
        return LamUtil.groupByMinValueToList(originList, keyExtractor, function);
    }

    /**
     * 将List分组
     *
     * @param originList   原数据
     * @param keyExtractor Key的抽取规则
     * @param <K>          Key
     * @param <V>          Value
     * @return Map<K, List < V>>
     */
    public static <K, V, U> Map<K, List<U>> listGroupByToMap(Collection<V> originList,
                                                             Function<V, K> keyExtractor,
                                                             Function<V, U> valueExtractor) {
        return LamUtil.groupByToMap(originList, keyExtractor, valueExtractor);
    }

    /**
     * 平铺成列表
     *
     * @param originList 列表
     * @param function   映射器
     * @return {@link List}<{@link R}>
     */
    public static <T, R> List<R> listFlatMapToList(List<T> originList,
                                                   Function<T, List<R>> function) {
        return LamUtil.flatMapToList(originList, function);
    }

    /**
     * 平铺成列表
     *
     * @param originList 列表
     * @param function   映射器
     * @return {@link List}<{@link R}>
     */
    public static <T, R> List<R> listFiltersFlatMapToList(List<T> originList,
                                                          Function<T, List<R>> function,
                                                          Predicate<T>... filters) {
        return LamUtil.filtersFlatMapToList(originList, function, filters);
    }


    /**
     * 按照属性排序
     *
     * @param originList 原数据
     * @param <T>        原数据的元素类型
     * @return List<T>
     */
    public static <T, U extends Comparable<? super U>> List<T> sortAscLastNullToList(Collection<T> originList,
                                                                                     Function<? super T, ? extends U> function) {
        return LamUtil.sort(originList, function, true, true);
    }


    /**
     * 按照属性排序
     *
     * @param originList 原数据
     * @param <T>        原数据的元素类型
     * @return List<T>
     */
    public static <T, U extends Comparable<? super U>> List<T> sortAscFirstNullToList(Collection<T> originList,
                                                                                      Function<? super T, ? extends U> function) {
        return LamUtil.sort(originList, function, true, false);
    }

    /**
     * 按照属性排序倒序
     *
     * @param originList 源列表
     * @param mapper     方法
     * @return {@link String}
     */
    public static <T, U extends Comparable<? super U>> List<T> sortDescLastNullToList(Collection<T> originList,
                                                                                      Function<? super T, ? extends U> mapper) {
        return LamUtil.sort(originList, mapper, false, true);
    }


    /**
     * 按照属性排序倒序
     *
     * @param originList 源列表
     * @param mapper     方法
     * @return {@link String}
     */
    public static <T, U extends Comparable<? super U>> List<T> sortDescFirstNullToList(Collection<T> originList,
                                                                                       Function<? super T, ? extends U> mapper) {
        return LamUtil.sort(originList, mapper, false, false);
    }


    // =================================================================================================================
    // =================================================================================================================
    // =================================================================================================================


    public static <K, V> PkMap<K, V> newPkMap() {
        return new PkMap<>();
    }

    public static <T> String getFieldName(PkMap.SFunction<T, ?> fn) {
        return PkMap.getField(fn);
    }

    @SafeVarargs
    public static <T> List<String> getFieldNames(PkMap.SFunction<T, ?>... fns) {
        return Arrays.stream(fns)
                .map(PkMap::getField)
                .collect(Collectors.toList());
    }

    @SafeVarargs
    public static <T> String[] getFieldNameAttr(PkMap.SFunction<T, ?>... fns) {
        return Arrays.stream(fns)
                .map(PkMap::getField)
                .toArray(String[]::new);
    }

    public static <T> Method getGetter(Class<T> clazz, PkMap.SFunction<T, ?> sFunction) {
        return LamUtil.getGetter(clazz, sFunction);
    }

    public static <T> Method getGetter(Class<T> clazz, final String fieldName) {
        return LamUtil.getGetter(clazz, fieldName);
    }

    public static <T> PkMap.SFunction<T, ?> getSFunction(Class<T> clazz, PkMap.SFunction<T, ?> sFunction) {
        final BeanDesc beanDesc = BeanUtil.getBeanDesc(clazz);
        final Method disabledMethod = beanDesc.getGetter(PkUtil.getFieldName(sFunction));
        return SFunctionUtil.create(clazz, disabledMethod);
    }


    public static boolean lazyBoolean(Supplier<Boolean> lazyFunc) {
        return lazyFunc.get();
    }

    public static <T> LambdaBuilder<T> build(Supplier<T> constructor) {
        return LambdaBuilder.builder(constructor);
    }

    public static <T> T getOneByMap(Map<String, T> map, String key, Supplier<T> func) {
        T t;
        if (map.containsKey(key)) {
            t = map.get(key);
        }
        else {
            t = func.get();
            map.put(key, t);
        }
        return t;
    }

    public static boolean isBlank(String val, Consumer<String> func) {
        if (StrUtil.isBlank(val)) {
            func.accept(val);
            return true;
        }
        return false;
    }

    public static <T> void blankAndSet(Supplier<T> supplier, Consumer<T> consumer, Supplier<T> func) {
        if (isNull(supplier.get())) {
            consumer.accept(func.get());
        }
    }


    public static boolean isNotBlank(String val, Consumer<String> func) {
        if (StrUtil.isNotBlank(val)) {
            func.accept(val);
            return true;
        }
        return false;
    }


    public static <T> boolean isNotEmpty(T val, Consumer<T> func) {
        if (isNotEmpty(val)) {
            func.accept(val);
            return true;
        }
        return false;
    }


    public static <T, X extends Serializable> void mapContainsKey(Map<X, T> map, X key, Consumer<T> func) {
        mapHasKey(map, key, func);
    }

    public static <T, X extends Serializable> void mapHasKey(Map<X, T> map, X key, Consumer<T> func) {
        if (map.containsKey(key)) {
            func.accept(map.get(key));
        }
    }

    /**
     * 消费originList具体属性，匹配mapList的具体属性(HashMap key方式匹配)，当匹配成功，提供匹配成功的原始对象和匹配对象供使用者消费
     *
     * @param originList   原始list
     * @param originIdFunc 原始具体属性获取方式
     * @param mapList      匹配list
     * @param mapIdFunc    匹配具体属性获取方式
     * @param func         匹配成功的消费
     */
    public static <V, T, X extends Serializable> void mapHasKey(Collection<V> originList, Function<V, X> originIdFunc,
                                                                Collection<T> mapList, Function<T, X> mapIdFunc,
                                                                BiConsumer<V, T> func) {
        final Map<X, T> xtMap = listToBeanMap(mapList, mapIdFunc);
        for (V v : originList) {
            final X x = originIdFunc.apply(v);
            if (xtMap.containsKey(x)) {
                func.accept(v, xtMap.get(x));
            }
        }
    }

    public static <T> Optional<T> op(T o) {
        return Optional.ofNullable(o);
    }

    public static <T extends Serializable> Optional<List<T>> op(List<T> list) {
        return Optional.ofNullable(list);
    }


    public static String checkStr(String value, Supplier<String> errorMsg, boolean... isThrowException) {
        return PkUtil.op(value)
                .filter(StrUtil::isNotBlank)
                .orElseGet(() -> {
                    if (isThrowException.length == 0 || isThrowException[0]) {
                        throw new RuntimeException(errorMsg.get());
                    }
                    return value;
                });
    }

    public static <T extends Serializable> T checkObj(T value, Supplier<String> errorMsg, boolean... isThrowException) {
        return PkUtil.op(value)
                .filter(PkUtil::isNotNull)
                .orElseGet(() -> {
                    if (isThrowException.length == 0 || isThrowException[0]) {
                        throw new RuntimeException(errorMsg.get());
                    }
                    return value;
                });
    }

    public static <T extends Serializable> List<T> checkList(List<T> values, Supplier<String> errorMsg, boolean... isThrowException) {
        return PkUtil.op(values)
                .filter(CollUtil::isNotEmpty)
                .orElseGet(() -> {
                    if (isThrowException.length == 0 || isThrowException[0]) {
                        throw new RuntimeException(errorMsg.get());
                    }
                    return values;
                });
    }

    public static <T> T throwNull(T o, Supplier<String> func) {
        if (isNull(o)) {
            throw new RuntimeException(func.get());
        }
        return o;
    }

    public static <T> T throwNull(boolean condition, Supplier<T> objFunc, Supplier<String> func) {
        if (!condition) {
            return null;
        }
        final T t = objFunc.get();
        if (isNull(t)) {
            throw new RuntimeException(func.get());
        }
        return t;
    }

    public static void throwBool(boolean checkResult, Supplier<String> func) {
        if (checkResult) {
            throw new RuntimeException(func.get());
        }
    }

    public static void throwBlank(String val, Supplier<String> func) {
        if (StrUtil.isBlank(val)) {
            throw new RuntimeException(func.get());
        }
    }

    public static void throwEnum(BaseEnum baseEnum, String enumVal, Supplier<String> func) {
        if (StrUtil.isBlank(enumVal)) {
            throw new RuntimeException(func.get());
        }
        if (baseEnum.getKey().equals(enumVal)) {
            throw new RuntimeException(func.get());
        }
    }

    public static boolean eqEnum(BaseEnum baseEnum, String enumVal) {
        if (StrUtil.isBlank(enumVal)) {
            return false;
        }
        return baseEnum.getKey().equals(enumVal);
    }

    public static <T> T getIgnoreError(Supplier<T> supplier) {
        return getIgnoreError(supplier, null);
    }


    public static <T> T getIgnoreError(Supplier<T> supplier,T defaultValue) {
        try {
            return supplier.get();
        } catch (Exception ignored) {
        }
        return defaultValue;
    }


    public interface ImportHandler {
        boolean accept();
    }

    public static void setDownFileName(String fileFullName) {
        setDownFileName(Objects.requireNonNull(Context.current()), fileFullName);
    }

    public static void setDownFileName(Context current, String fileFullName) {
        String newFileName = URLEncoder.encode(fileFullName, StandardCharsets.UTF_8);
        String outFileName = StrUtil.format("attachment;filename={};filename*=utf-8''{};charset=utf-8", newFileName, newFileName);
        current.headerSet("Content-Disposition", outFileName);
    }

    public static <T> String importData(List<T> vos, ImportHandler consumer, Function<T, String> tip) {
        if (CollUtil.isEmpty(vos)) {
            throw new RuntimeException("导入数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (int i = 0; i < vos.size(); i++) {
            final T t = vos.get(i);
            try {

                BeanValidators.validateWithException(t);
                successNum++;
            } catch (Exception e) {
                failureNum++;
                String msg = "<br/>" + failureNum + "、第" + (i + 1) + "条\"" + tip.apply(t) + "\"导入失败：";
                failureMsg.append(msg).append(StrUtil.subAfter(e.getMessage(), ": ", true));
                log.error(msg, e);
            }
        }

        if (failureNum > 0) {
            failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new RuntimeException(failureMsg.toString());
        }
        else {
            // 1 ============================ begin ==========================
            final boolean accept = consumer.accept();
            if (!accept) {
                successNum = 0;
            }
            // 2 ============================ end ============================
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条！");
        }
        return successMsg.toString();
    }


    public static <T, U, X extends Serializable> void remoteLeftJoin(List<T> vos, Function<T, X> idsExtractor,
                                                                     Function<List<X>, List<U>> func, Function<U, X> idsGetter,
                                                                     BiConsumer<T, U> setter) {
        if (CollUtil.isEmpty(vos)) {
            return;
        }
        final List<X> list = PkUtil.listMapToList(vos, idsExtractor);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        final Map<X, U> beanMap = new HashMap<>(vos.size());
        // 异步查询，默认1000切片
        PkUtil.batchVoid(list, 2000, tempList -> beanMap.putAll(Optional.ofNullable(func.apply(tempList))
                .filter(CollUtil::isNotEmpty)
                .map(dtos -> PkUtil.listToBeanMap(dtos, idsGetter))
                .orElseGet(HashMap::new)));
        // 同步查询
//        final Map<X, U> beanMap = Optional.ofNullable(func.apply(list))
//                .filter(e -> e.getCode() == 200)
//                .map(R::getData)
//                .filter(CollUtil::isNotEmpty)
//                .map(dtos -> PkUtil.listToBeanMap(dtos, idsGetter))
//                .orElseGet(HashMap::new);
        if (MapUtil.isEmpty(beanMap)) {
            return;
        }
        vos.forEach(vo -> PkUtil.mapHasKey(beanMap, idsExtractor.apply(vo), dto -> setter.accept(vo, dto)));
    }

    /**
     * 多线程批量执行任务
     *
     * @param action 行动
     * @param list   列表
     * @param size   尺寸
     */
    public static <T, R> List<R> batch(List<T> list, int size, Function<List<T>, List<R>> action) {
        return BatchTaskRunner.execute(list, size, action);
    }


    /**
     * 多线程批量执行任务, 默认阈值1000
     *
     * @param action 行动
     * @param list   列表
     */
    public static <T, R> List<R> batch(List<T> list, Function<List<T>, List<R>> action) {
        return BatchTaskRunner.execute(list, 1000, action);
    }

    /**
     * 多线程批量执行任务
     *
     * @param action 行动
     * @param list   列表
     * @param size   尺寸
     */
    public static <T> void batchVoid(List<T> list, int size, Consumer<List<T>> action) {
        BatchTaskRunner.execute(list, size, action);
    }


    /**
     * 多线程批量执行任务, 默认阈值1000
     *
     * @param action 行动
     * @param list   列表
     */
    public static <T> void batchVoid(List<T> list, Consumer<List<T>> action) {
        BatchTaskRunner.execute(list, 1000, action);
    }

    /**
     * 多线程批量执行任务
     *
     * @param action 行动
     * @param count  计数
     * @param size   尺寸
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> batch(int count, int size, BiFunction<Integer, Integer, List<T>> action) {
        return BatchTaskRunner.execute(count, size, action);
    }

    /**
     * 多线程批量执行任务, 默认阈值1000
     *
     * @param action 行动
     * @param count  计数
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> batch(int count, BiFunction<Integer, Integer, List<T>> action) {
        return BatchTaskRunner.execute(count, 1000, action);
    }


    public static List<String> getQuoteStrs(List<String> ruleSources) {
        List<String> newStrs = new ArrayList<>();
        if (CollUtil.isNotEmpty(ruleSources)) {
            for (String ruleSource : ruleSources) {
                final String format = StrUtil.format("\"{}\"", ruleSource);
                newStrs.add(format);
            }
        }
        return newStrs;
    }

    public static boolean isRangeValues(String value, String rangeDescription) {
        return isRangeValues(value, rangeDescription, null);
    }

    public static boolean isRangeValues(String value, String rangeDescription, String dot) {
        if (StrUtil.isBlank(value)) {
            return false;
        }
        List<String> mulitValues;
        if (rangeDescription.contains(",")) {
            mulitValues = StrUtil.split(rangeDescription, ",");
        }
        else {
            mulitValues = Collections.singletonList(rangeDescription);
        }
        for (String singleValue : mulitValues) {
            if (StrUtil.count(singleValue, dot) > 1) {
                continue;
            }
            dot = StrUtil.blankToDefault(dot, "-");
            if (singleValue.contains(dot)) {
                List<String> leftAndRight = StrUtil.split(singleValue, dot);
                if (leftAndRight.size() != 2) {
                    continue;
                }
                String before = StrUtil.nullToEmpty(leftAndRight.get(0)).toUpperCase();
                String after = StrUtil.nullToEmpty(leftAndRight.get(1)).toUpperCase();

                final String valueUpperCase = StrUtil.sub(value.toUpperCase(), 0, Math.max(before.length(), after.length()));
                if (CompareUtil.compare(valueUpperCase, before) >= 0 && CompareUtil.compare(valueUpperCase, after) <= 0) {
                    return true;
                }
            }
            else {
                String rightValue = StrUtil.nullToEmpty(singleValue).toUpperCase();
                if (rightValue.length() > 0) {
                    final String valueUpperCase = StrUtil.sub(value.toUpperCase(), 0, rightValue.length());
                    if (CompareUtil.compare(valueUpperCase, rightValue) == 0) {
                        return true;
                    }
                }
            }
//            if (!singleValue.contains(dot)) {
//                return false;
//            }
        }
        return false;
    }

    public static List<String> getRangeValues(String rangeDescription) {
        return getRangeValues(rangeDescription, 1);
    }

    public static List<String> getRangeValues(String rangeDescription, double step) {
        List<String> result = new ArrayList<>();

        String[] parts = rangeDescription.split("-");
        String start = parts[0];
        String end = parts[1];

        // 匹配中英数混合字符串中的数字（包括小数）
        final List<String> startAllGroups = PkUtil.listFiltersBlankDistinctToList(ReUtil.getAllGroups(compile, start, false));
        final List<String> endAllGroups = PkUtil.listFiltersBlankDistinctToList(ReUtil.getAllGroups(compile, end, false));

        // 提取匹配到的数字
        String startDotStr = null;
        String startNumStr = null;
        String startCalStr = null;
        if (startAllGroups.size() == 1) {
            startNumStr = startAllGroups.get(0);
        }
        else if (startAllGroups.size() == 2) {
            if (StrUtil.startWith(startAllGroups.get(1), '.')) {
                startNumStr = startAllGroups.get(0);
                startCalStr = startAllGroups.get(1);
            }
            else {
                startDotStr = startAllGroups.get(0);
                startNumStr = startAllGroups.get(1);
            }
        }
        else if (startAllGroups.size() == 3) {
            startDotStr = startAllGroups.get(0);
            startNumStr = startAllGroups.get(1);
            startCalStr = startAllGroups.get(2);
        }
        else {
            throw new RuntimeException("异常");
        }

        String endDotStr = null;
        String endNumStr = null;
        String endCalStr = null;
        if (endAllGroups.size() == 1) {
            endNumStr = endAllGroups.get(0);
        }
        else if (endAllGroups.size() == 2) {
            if (StrUtil.startWith(endAllGroups.get(1), '.')) {
                endNumStr = endAllGroups.get(0);
                endCalStr = endAllGroups.get(1);
            }
            else {
                endDotStr = endAllGroups.get(0);
                endNumStr = endAllGroups.get(1);
            }
        }
        else if (endAllGroups.size() == 3) {
            endDotStr = endAllGroups.get(0);
            endNumStr = endAllGroups.get(1);
            endCalStr = endAllGroups.get(2);
        }
        else {
            throw new RuntimeException("异常");
        }

        if ((startDotStr == null && endDotStr == null) || StrUtil.equals(startDotStr, endDotStr)) {
            BigDecimal startDecimal = new BigDecimal(startNumStr);
            BigDecimal endDecimal = new BigDecimal(endNumStr);
            int numLength = startNumStr.length();
            if (startCalStr != null) {
                numLength += startCalStr.length();
                startDecimal = new BigDecimal(startNumStr + startCalStr);
                endDecimal = new BigDecimal(endNumStr + endCalStr);
            }

            BigDecimal current = startDecimal;
            while (current.compareTo(endDecimal) <= 0) {

                String plainString = current.toPlainString();
                if (startCalStr == null) {
                    plainString = current.setScale(0, RoundingMode.CEILING).toPlainString();
                }
                String valueString;
                if (startDotStr != null) {
                    valueString = startDotStr + StrUtil.fill(plainString, '0', numLength, true);
                }
                else {
                    valueString = StrUtil.fill(plainString, '0', numLength, true);
                }
                result.add(valueString);
                current = current.add(BigDecimal.valueOf(step));
            }
        }
        else {
            if (startDotStr != null && endDotStr != null && startDotStr.length() == endDotStr.length()) {
                final String maxNumber = StrUtil.fill("", '9', endNumStr.length(), true);
                for (int length = startDotStr.length(); length > 0; length--) {
                    final int lastIndex = length - 1;
                    final String prefixStartStr = startDotStr.substring(0, lastIndex);
                    final String prefixEndStr = endDotStr.substring(0, lastIndex);
                    char startCharAt = startDotStr.charAt(lastIndex);
                    char endCharAt = endDotStr.charAt(lastIndex);
                    final int len = (endCharAt - startCharAt) + 1;
                    if (endCharAt - startCharAt > 0) {
                        for (int i = 0; i < len; i++) {
                            BigDecimal startDecimal = new BigDecimal(startNumStr);
                            BigDecimal endDecimal = new BigDecimal(maxNumber);
                            int numLength = startNumStr.length();
                            if (startCalStr != null) {
                                numLength += startCalStr.length();
                                startDecimal = new BigDecimal(startNumStr + startCalStr);
                                endDecimal = new BigDecimal(maxNumber + endCalStr);
                            }
                            BigDecimal current = startDecimal;
                            while (current.compareTo(endDecimal) <= 0) {
                                String plainString = current.toPlainString();
                                if (startCalStr == null) {
                                    plainString = current.setScale(0, RoundingMode.CEILING).toPlainString();
                                }
                                final String fill = StrUtil.fill(plainString, '0', numLength, true);
                                String valueString = prefixStartStr + startCharAt + fill;
                                result.add(valueString);
                                current = current.add(BigDecimal.valueOf(step));

                                if (startCalStr != null) {
                                    if (i == len - 1 && StrUtil.equals(fill, endNumStr + endCalStr)) {
                                        break;
                                    }
                                }
                                else {
                                    if (i == len - 1 && StrUtil.equals(fill, endNumStr)) {
                                        break;
                                    }
                                }

                            }

                            ++startCharAt;
                        }
                    }

                }
            }
            else {
                throw new RuntimeException("暂时无法处理前缀长度不一样的情况");
            }
        }


        return result;
    }


    public long getTime(Date date) {
        return date.getTime();
    }


    public static long getTime(LocalDate localDate) {
        return LocalDateTimeUtil.toEpochMilli(localDate);
    }

    public static long getTime(TemporalAccessor temporalAccessor) {
        return LocalDateTimeUtil.toEpochMilli(temporalAccessor);
    }


    public static <T, U> boolean listHasDuplicates(List<T> list, Function<T, U> keyExtractor) {
        return LamUtil.hasDuplicates(list, keyExtractor);
    }

    public static <S extends Serializable> boolean listHasDuplicates(List<S> list) {
        return LamUtil.hasDuplicates(list);
    }

    public interface CallbackTask<R> {
        R execute();

        default void onSuccess(R r) {
        }

        default void onFailure(Throwable t) {
        }
    }

    /**
     * 借助 CompletableFuture 来实现异步行为。
     * 不会抛出异常，在 onFailure 中处理异常
     *
     * @param executeTask
     * @param <R>
     * @return
     */
    public static <R> CompletableFuture<R> async(CallbackTask<R> executeTask) {
        return async(executeTask, null);
    }

    /**
     * 借助 CompletableFuture 来实现异步行为。
     * 不会抛出异常，在 onFailure 中处理异常
     *
     * @param executeTask
     * @param <R>
     * @return
     */
    public static <R> CompletableFuture<R> async(CallbackTask<R> executeTask, R defaultValue) {
        return CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return executeTask.execute();
                    } catch (Exception exception) {
                        throw new RuntimeException(exception.getMessage());
                    }
                })
                .whenComplete((result, throwable) -> {
                    // 不管成功与失败，whenComplete 都会执行，
                    // 通过 throwable == null 跳过执行
                    if (throwable == null) {
                        executeTask.onSuccess(result);
                    }
                })
                .exceptionally(throwable -> {
                    executeTask.onFailure(throwable);
                    // todo 给一个默认值，或者使用 Optional包装一下，否者异常会出现NPE
                    return defaultValue;
                });
    }

    public static String getLocalHostStr() {
        try {
            InetAddress candidateAddress = null;

            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface iface = networkInterfaces.nextElement();
                // 该网卡接口下的ip会有多个，也需要一个个的遍历，找到自己所需要的
                for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                    InetAddress inetAddr = inetAddrs.nextElement();
                    // 排除loopback回环类型地址（不管是IPv4还是IPv6 只要是回环地址都会返回true）
                    if (!inetAddr.isLoopbackAddress()) {
                        if (inetAddr.isSiteLocalAddress()) {
                            // 如果是site-local地址，就是它了 就是我们要找的
                            // ~~~~~~~~~~~~~绝大部分情况下都会在此处返回你的ip地址值~~~~~~~~~~~~~
                            return inetAddr.getHostAddress();
                        }

                        // 若不是site-local地址 那就记录下该地址当作候选
                        if (candidateAddress == null) {
                            candidateAddress = inetAddr;
                        }

                    }
                }
            }

            // 如果出去loopback回环地之外无其它地址了，那就回退到原始方案吧
            final InetAddress inetAddress = candidateAddress == null ? InetAddress.getLocalHost() : candidateAddress;
            return inetAddress.getHostAddress();
        } catch (Exception e) {
        }
        return NetUtil.getLocalhostStr();
    }


    public static PkStopWatch stopWatch(String id) {
        return new PkStopWatch(id);
    }

    public static List<Field> getFieldsListWithAnnotation(Class<?> cls, Class<? extends Annotation> annotationCls) {
        if (ObjectUtil.isNull(annotationCls)) {
            throw new RuntimeException("annotationCls 不能为空!");
        }
        List<Field> allFields = getAllFieldsList(cls);
        List<Field> annotatedFields = new ArrayList<>();

        for (Field field : allFields) {
            if (field.getAnnotation(annotationCls) != null) {
                annotatedFields.add(field);
            }
        }

        return annotatedFields;
    }


    public static List<Field> getAllFieldsList(Class<?> cls) {
        if (ObjectUtil.isNull(cls)) {
            throw new RuntimeException("cls 不能为空!");
        }
        List<Field> allFields = new ArrayList<>();

        for (Class<?> currentClass = cls; currentClass != null; currentClass = currentClass.getSuperclass()) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            Collections.addAll(allFields, declaredFields);
        }

        return allFields;
    }
}
