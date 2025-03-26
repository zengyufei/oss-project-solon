package com.zyf.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.data.tran.TranUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 异步工具类，封装意义在于用于业务代码编写简洁，注意不可以用在事务方法！！！
 * @author zyf
 * @date 2024/05/30
 */
@Slf4j
public class PkSync {
    private static Executor poolExecutor;
    private final List<CompletableFuture<Void>> futures;
    private final ConcurrentStopWatch sw;

    public PkSync(Executor poolExecutor) {
        this(StrUtil.EMPTY, poolExecutor);
    }

    public PkSync(String id, Executor poolExecutor) {
        PkSync.poolExecutor = poolExecutor;
        this.futures = new ArrayList<>();
        this.sw = new ConcurrentStopWatch(id);
    }

    public static PkSync of(Executor poolExecutor) {
        return of(StrUtil.EMPTY, poolExecutor);
    }

    public static PkSync of(String id, Executor poolExecutor) {
        return new PkSync(id, poolExecutor);
    }

    public PkSync add(String taskName, Runnable runnable) {
        return add(taskName, runnable, true);
    }

    public PkSync add(String taskName, Runnable runnable, boolean isCheckTran) {
        boolean inTransaction = TranUtils.inTrans();
        if (isCheckTran && inTransaction) {
            throw new RuntimeException(StrUtil.format("当前处于事务状态无法跨线程, 请尝试使用 本类的countDownLatch() 方法 "));
        } else {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                sw.start(taskName);
                try {
                    runnable.run();
                } catch (Exception e) {
                    log.error("{} 执行错误", taskName);
                    throw new RuntimeException(e);
                } finally {
                    sw.stop(taskName);
                }
            }, poolExecutor)
//                .exceptionally(e -> {
//                    throw new RuntimeException(e);
//                })
                    ;
            futures.add(future);
        }

        return this;
    }

    public void clear() {
        futures.clear();
    }

    public void waitAll(long timeout, TimeUnit unit) throws Exception {
        waitAll(timeout, unit, true);
    }

    public void waitAll(long timeout, TimeUnit unit, boolean isCheckTran) throws Exception {
        if (!futures.isEmpty()) {
            boolean inTransaction = TranUtils.inTrans();
            if (isCheckTran && inTransaction) {
                throw new RuntimeException(StrUtil.format("当前处于事务状态无法跨线程, 请尝试使用 本类的countDownLatch() 方法 "));
            } else {
                final CompletableFuture[] futuresArray = futures.toArray(CompletableFuture[]::new);
                final CompletableFuture<Void> future = CompletableFuture.allOf(futuresArray);
                try {
                    future.get(timeout, unit);
                } catch (TimeoutException e) {
                    log.error("超过执行设定的时间");
                    sw.setMax(timeout, unit);
                    throw e;
                } finally {
                    log.info(sw.prettyPrint(TimeUnit.MILLISECONDS));
                }
            }

        } else {
            log.warn("没有任务执行");
        }
    }

}
