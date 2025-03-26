package com.zyf.utils.lambda;



import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class PkMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = -5260635176470805065L;
    private static final Map<Class<?>, WeakReference<SerializedLambda>> CLASS_LAMDBA_CACHE = new ConcurrentHashMap<>();

    public <T> boolean containsKey(SFunction<T, ?> key) {
        return super.containsKey(PkMap.getField(key));
    }

    public <T> V remove(SFunction<T, ?> key) {
        return super.remove(key);
    }

    public <T> V putIfAbsent(SFunction<T, ?> key, V v) {
        return super.putIfAbsent((K) PkMap.getField(key), v);
    }

    public <T> V get(SFunction<T, ?> key) {
        return super.get(PkMap.getField(key));
    }

    public <T> V put(SFunction<T, ?> key, V v) {
        return super.put((K) PkMap.getField(key), v);
    }

    /**
     * 支持序列化的 Function
     *
     * @author miemie
     * @since 2018-05-12
     */
    @FunctionalInterface
    public interface SFunction<T, R> extends Function<T, R>, Serializable {
    }

    /**
     * 将bean的属性的get方法，作为lambda表达式传入时，获取get方法对应的属性Field
     *
     * @param fn  lambda表达式，bean的属性的get方法
     * @param <T> 泛型
     * @return 属性对象
     */
    public static <T> String getField(SFunction<T, ?> fn) {
        // 从序列化方法取出序列化的lambda信息
        SerializedLambda serializedLambda = getSerializedLambda(fn);
        // 获取方法名
        String implMethodName = serializedLambda.getImplMethodName();
        String prefix = null;
        if (implMethodName.startsWith("get")) {
            prefix = "get";
        }
        else if (implMethodName.startsWith("is")) {
            prefix = "is";
        }
        if (prefix == null) {
            throw new RuntimeException("get方法名称: " + implMethodName + ", 不符合java bean规范");
        }
        // 截取get/is之后的字符串并转换首字母为小写
        return toLowerCaseFirstOne(implMethodName.replace(prefix, ""));
    }

    /**
     * 将bean的属性的get方法，作为lambda表达式传入时，获取get方法对应的属性Field
     *
     * @param fns lambda表达式，bean的属性的get方法
     * @param <T> 泛型
     * @return 属性对象
     */
    public static <T> List<String> getFields(SFunction<T, ?>... fns) {
        List<SerializedLambda> serializedLambdas = new ArrayList<>();
        List<String> list = new ArrayList<>();
        for (SFunction<T, ?> fn : fns) {
            // 从序列化方法取出序列化的lambda信息
            SerializedLambda serializedLambda = getSerializedLambda(fn);
            serializedLambdas.add(serializedLambda);
        }
        for (SerializedLambda serializedLambda : serializedLambdas) {
            // 获取方法名
            String implMethodName = serializedLambda.getImplMethodName();
            String prefix = null;
            if (implMethodName.startsWith("get")) {
                prefix = "get";
            }
            else if (implMethodName.startsWith("is")) {
                prefix = "is";
            }
            if (prefix == null) {
                throw new RuntimeException("get方法名称: " + implMethodName + ", 不符合java bean规范");
            }
            // 截取get/is之后的字符串并转换首字母为小写
            list.add(toLowerCaseFirstOne(implMethodName.replace(prefix, "")));
        }
        return list;
    }

    /**
     * 关键在于这个方法
     */
    private static SerializedLambda getSerializedLambda(Serializable fn) {
        WeakReference<SerializedLambda> lambdaWeak = CLASS_LAMDBA_CACHE.get(fn.getClass());
        return Optional.ofNullable(lambdaWeak).map(Reference::get).orElseGet(() -> {
            try {
                // 提取SerializedLambda并缓存
                Method method = fn.getClass().getDeclaredMethod("writeReplace");
                boolean isAccessible = method.isAccessible();
                method.setAccessible(Boolean.TRUE);
                SerializedLambda lambda = (SerializedLambda) method.invoke(fn);
                method.setAccessible(isAccessible);
                CLASS_LAMDBA_CACHE.put(fn.getClass(), new WeakReference<>(lambda));
                return lambda;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("提取SerializedLambda异常");
            }
        });
    }


    /**
     * 首字母转小写
     *
     * @param s s
     * @return string
     */
    private static String toLowerCaseFirstOne(String s) {
        if (Character.isLowerCase(s.charAt(0))) {
            return s;
        }
        else {
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
        }
    }

}
