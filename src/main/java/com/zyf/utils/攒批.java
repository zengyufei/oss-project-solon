package com.zyf.utils;

import cn.hutool.core.collection.CollUtil;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
public class 攒批<T> {
    @Setter
    private volatile BlockingQueue<T> queue;
    @Setter
    private volatile int size;
    @Setter
    private volatile int times;
    @Setter
    private volatile ThreadPoolExecutor executorService;

    private final AtomicBoolean isStart = new AtomicBoolean(false);
    private final AtomicBoolean isTake = new AtomicBoolean(false);
    private final AtomicBoolean isClose = new AtomicBoolean(false);
    private final AtomicBoolean isEnd = new AtomicBoolean(false);

    private final AtomicInteger timeSecondCounter = new AtomicInteger(0);

    private final List<T> datas = new ArrayList<>();

    public 攒批(BlockingQueue<T> queue, int size, int times) {
        this.queue = queue;
        this.size = size;
        this.times = times;
    }

    public 攒批(BlockingQueue<T> queue, int size, int times, ThreadPoolExecutor executorService) {
        this.queue = queue;
        this.size = size;
        this.times = times;
        this.executorService = executorService;
    }

    public boolean getIfNot(boolean right) {
        return get(right) && CollUtil.isNotEmpty(datas);
    }

    private boolean get(boolean right) {
        // 如果内部状态未开始，则开始
        if (!isStart.get()) {
            start();
        }

        // 如果外部通知要完成，则返回true
        if (right) {
            close();
        }

        // 如果内部状态已完成，则返回true
        if (isTake.get()) {
            return true;
        }


        return false;
    }

    public List<T> takeDatas() {
        if (CollUtil.isEmpty(datas)) {
            throw new NullPointerException("数据为空");
        }
        final List<T> newList = new ArrayList<>(datas);
        datas.clear();

        isTake.set(false);
        timeSecondCounter.set(0);

        return newList;
    }

    private void start() {
        isStart.set(true);
        if (executorService == null) {
            new Thread(this::excute).start();
        } else {
            executorService.execute(this::excute);
        }
    }

    public void close() {
        isClose.set(true);
    }

    public boolean isClose() {
        return isClose.get() && !isTake.get() && isEnd.get();
    }

    private void excute() {
        int ss = 0;
        final int timeout = 10;
        final int rate = 1000 / timeout;
        // 不断循环判断
        while (true) {
            // 如果容器不为空，且容量达到阈值，则停止
            if (isTake.get()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(timeout);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                continue;
            }
            ss++;
            int get = timeSecondCounter.get();
            if (ss >= rate) {
                // 不断计时
                get = timeSecondCounter.incrementAndGet();
                ss = 0;
            }

            // 如果此时队列中有数据，且大于等于阈值，则倒腾到容器中，并设置已完成，且停止计时，等待下一次启动
            final int queueSize = queue.size();
            if (queueSize >= size) {
                queue.drainTo(datas, size);
                isTake.set(true);
            } else if (queueSize > 0 && queueSize < size) {
                if (this.isClose.get()) {
                    queue.drainTo(datas, size);
                    isTake.set(true);
                    isEnd.set(true);
                } else if (get >= times) {
                    queue.drainTo(datas, size);
                    isTake.set(true);
//                    isEnd.set(true);
                   // close();
                }
            } else if (queueSize <= 0) {
                if (this.isClose.get()) {
                    isEnd.set(true);
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(timeout);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (isEnd.get()) {
                break;
            }
        }
    }

}
