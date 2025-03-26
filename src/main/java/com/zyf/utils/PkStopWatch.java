package com.zyf.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PkStopWatch {

    /**
     * 创建计时任务（秒表）
     *
     * @param id 用于标识秒表的唯一ID
     * @return PkStopWatch
     * @since 5.5.2
     */
    public static PkStopWatch create(String id) {
        return new PkStopWatch(id);
    }

    /**
     * 秒表唯一标识，用于多个秒表对象的区分
     */
    private final String id;
    private final Map<String, TaskInfo> taskMap = new LinkedHashMap<>();
    private final Map<Integer, Integer> taskCountMap = new HashMap<>();
    private final Map<Integer, Long> taskTotalMap = new HashMap<>();

    // ------------------------------------------------------------------------------------------- Constructor start

    /**
     * 构造，不启动任何任务
     */
    public PkStopWatch() {
        this(StrUtil.EMPTY);
    }

    /**
     * 构造，不启动任何任务
     *
     * @param id 用于标识秒表的唯一ID
     */
    public PkStopWatch(String id) {
        this.id = id;
    }

    // ------------------------------------------------------------------------------------------- Constructor end

    /**
     * 获取StopWatch 的ID，用于多个秒表对象的区分
     *
     * @return the ID 默认为空字符串
     * @see #PkStopWatch(String)
     */
    public String getId() {
        return this.id;
    }

    /**
     * 开始指定名称的新任务
     *
     * @param taskName 新开始的任务名称
     * @throws IllegalStateException 前一个任务没有结束
     */
    public TaskInfo start(int level, String taskName) throws IllegalStateException {
        final TaskInfo taskInfo = new TaskInfo(taskName, level, System.nanoTime(), false);
        taskMap.put(taskName, taskInfo);
        return taskInfo;
    }

    /**
     * 开始指定名称的新任务
     *
     * @param taskName 新开始的任务名称
     * @throws IllegalStateException 前一个任务没有结束
     */
    public TaskInfo start1( String taskName) throws IllegalStateException {
        return start(1, taskName);
    }


    /**
     * 开始指定名称的新任务
     *
     * @param taskName 新开始的任务名称
     * @throws IllegalStateException 前一个任务没有结束
     */
    public TaskInfo start2( String taskName) throws IllegalStateException {
        return start(2, taskName);
    }


    /**
     * 开始指定名称的新任务
     *
     * @param taskName 新开始的任务名称
     * @throws IllegalStateException 前一个任务没有结束
     */
    public TaskInfo start3( String taskName) throws IllegalStateException {
        return start(3, taskName);
    }


    /**
     * 开始指定名称的新任务
     *
     * @param taskName 新开始的任务名称
     * @throws IllegalStateException 前一个任务没有结束
     */
    public TaskInfo start4( String taskName) throws IllegalStateException {
        return start(4, taskName);
    }

    /**
     * 停止当前任务
     *
     * @throws IllegalStateException 任务没有开始
     */
    public void stop(String taskName) throws IllegalStateException {
        final TaskInfo taskInfo = taskMap.get(taskName);
        stop(taskInfo);
    }

    /**
     * 停止当前任务
     *
     * @throws IllegalStateException 任务没有开始
     */
    public void stop(TaskInfo taskInfo) throws IllegalStateException {
        final int level = taskInfo.getLevel();
        final long startTimeNanos = taskInfo.getTimeNanos();
        final long lastTime = System.nanoTime() - startTimeNanos;
        taskInfo.setTimeNanos(lastTime);
        taskInfo.setStop(true);

        long total = taskTotalMap.computeIfAbsent(level, k -> 0L);
        total += lastTime;
        taskTotalMap.put(level, total);

        final Integer count = taskCountMap.computeIfAbsent(level, k -> 0);
        taskCountMap.put(level, count + 1);
    }

    /**
     * 获取所有任务的总花费时间
     *
     * @param unit 时间单位，{@code null}表示默认{@link TimeUnit#NANOSECONDS}
     * @return 花费时间
     * @since 5.7.16
     */
    public long getTotal(int level, TimeUnit unit) {
        long totalTimeNanos = 0;
        for (Map.Entry<String, TaskInfo> entry : taskMap.entrySet()) {
            final TaskInfo taskInfo = entry.getValue();
            if (taskInfo.getLevel() == level) {
                totalTimeNanos += taskInfo.getTimeNanos();
            }
        }
        return unit.convert(totalTimeNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * 获取所有任务的总花费时间
     *
     * @param unit 时间单位，{@code null}表示默认{@link TimeUnit#NANOSECONDS}
     * @return 花费时间
     * @since 5.7.16
     */
    public long getTotal(TimeUnit unit) {
        long totalTimeNanos = 0;
        final int minKey = getMinKey();
        for (Map.Entry<String, TaskInfo> entry : taskMap.entrySet()) {
            final TaskInfo taskInfo = entry.getValue();
            if (taskInfo.getLevel() == minKey) {
                totalTimeNanos += taskInfo.getTimeNanos();
            }
        }
        return unit.convert(totalTimeNanos, TimeUnit.NANOSECONDS);
    }

    private Integer getMinKey() {
        return taskTotalMap.keySet().stream().min(Integer::compareTo).orElse(1);
    }

    /**
     * 获取所有任务的总花费时间（纳秒）
     *
     * @return 所有任务的总花费时间（纳秒）
     * @see #getTotalTimeMillis()
     * @see #getTotalTimeSeconds()
     */
    public long getTotalTimeNanos() {
        return taskTotalMap.get(getMinKey());
    }

    /**
     * 获取所有任务的总花费时间（毫秒）
     *
     * @return 所有任务的总花费时间（毫秒）
     * @see #getTotalTimeNanos()
     * @see #getTotalTimeSeconds()
     */
    public long getTotalTimeMillis() {
        return getTotal(TimeUnit.MILLISECONDS);
    }

    /**
     * 获取所有任务的总花费时间（秒）
     *
     * @return 所有任务的总花费时间（秒）
     * @see #getTotalTimeNanos()
     * @see #getTotalTimeMillis()
     */
    public double getTotalTimeSeconds() {
        return DateUtil.nanosToSeconds(taskTotalMap.get(getMinKey()));
    }

    /**
     * 获取任务数
     *
     * @return 任务数
     */
    public int getTaskCount() {
        return taskCountMap.get(getMinKey());
    }

    /**
     * 获取任务数
     *
     * @return 任务数
     */
    public int getTaskCount(int level) {
        return taskCountMap.get(level);
    }

    /**
     * 获取任务信息，类似于：
     * <pre>
     *     PkStopWatch '[id]': running time = [total] ns
     * </pre>
     *
     * @return 任务信息
     */
    public String shortSummary() {
        return shortSummary(null);
    }

    /**
     * 获取任务信息，类似于：
     * <pre>
     *     PkStopWatch '[id]': running time = [total] [unit]
     * </pre>
     *
     * @param unit 时间单位，{@code null}则默认为{@link TimeUnit#NANOSECONDS}
     * @return 任务信息
     */
    public String shortSummary(TimeUnit unit) {
        if (null == unit) {
            unit = TimeUnit.NANOSECONDS;
        }
        StringBuilder sb = new StringBuilder(StrUtil.format("PkStopWatch '{}': ", this.id));

        final List<Integer> levelSortList = getLevelSortList();
        for (int i = 0; i < levelSortList.size(); i++) {
            final Integer level = levelSortList.get(i);
            sb.append("\n");
            String sp = StrUtil.repeat("--", i);
            sb.append(sp);
            sb.append(StrUtil.format("level {} running time = {} {}", level, getTotal(level, unit), DateUtil.getShotName(unit)));
        }

        return sb.toString();
    }

    /**
     * 生成所有任务的一个任务花费时间表，单位纳秒
     *
     * @return 任务时间表
     */
    public String prettyPrint() {
        return prettyPrint(null);
    }

    /**
     * 生成所有任务的一个任务花费时间表
     *
     * @param unit 时间单位，{@code null}则默认{@link TimeUnit#NANOSECONDS} 纳秒
     * @return 任务时间表
     * @since 5.7.16
     */
    public String prettyPrint(TimeUnit unit) {
        if (null == unit) {
            unit = TimeUnit.NANOSECONDS;
        }

        final StringBuilder sb = new StringBuilder(shortSummary(unit));
        sb.append(FileUtil.getLineSeparator());

        if (MapUtil.isEmpty(taskMap)) {
            sb.append("No task info kept");
        } else {
            sb.append("---------------------------------------------").append(FileUtil.getLineSeparator());
            sb.append(DateUtil.getShotName(unit)).append("         %     Task name").append(FileUtil.getLineSeparator());
            sb.append("---------------------------------------------").append(FileUtil.getLineSeparator());

            final NumberFormat nf = NumberFormat.getNumberInstance();
            nf.setMinimumIntegerDigits(9);
            nf.setGroupingUsed(false);

            final NumberFormat pf = NumberFormat.getPercentInstance();
            pf.setMinimumIntegerDigits(2);
            pf.setGroupingUsed(false);

            final List<Integer> levelSortList = getLevelSortList();
            for (Map.Entry<String, TaskInfo> entry : taskMap.entrySet()) {
                final String key = entry.getKey();
                final TaskInfo taskInfo = entry.getValue();
                final int level = taskInfo.getLevel();
                final Long totalTimeNanos = taskTotalMap.get(level);
                String sp = StrUtil.repeat("--", levelSortList.indexOf(level));
                sb.append(sp);
                sb.append(nf.format(taskInfo.getTime(unit))).append("  ");
                sb.append(pf.format((double) taskInfo.getTimeNanos() / totalTimeNanos)).append("   ");
                sb.append(key).append(FileUtil.getLineSeparator());
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(shortSummary());
        sb.append("\n");

        if (MapUtil.isNotEmpty(taskMap)) {
            final List<Integer> levelSortList = getLevelSortList();
            for (Map.Entry<String, TaskInfo> entry : taskMap.entrySet()) {
                final TaskInfo taskInfo = entry.getValue();
                final int level = taskInfo.getLevel();
                final Long totalTimeNanos = taskTotalMap.get(level);
                String sp = StrUtil.repeat("--", levelSortList.indexOf(level));
                sb.append(sp);
                sb.append("[");
                sb.append(taskInfo.getTaskName()).append("] took ").append(taskInfo.getTimeNanos()).append(" ns");
                long percent = Math.round(100.0 * taskInfo.getTimeNanos() / totalTimeNanos);
                sb.append(" = ").append(percent).append("%");
                sb.append(";");
                sb.append("\n");
            }
        } else {
            sb.append("; no task info kept");
        }
        return sb.toString();
    }

    private List<Integer> getLevelSortList() {
        final Set<Integer> keys = taskCountMap.keySet();
        return keys.stream().sorted(Comparator.comparingInt(Math::abs)).toList();
    }

    public void stopAll() {
        for (Map.Entry<String, TaskInfo> entry : taskMap.entrySet()) {
            final TaskInfo taskInfo = entry.getValue();
            if (!taskInfo.isStop()) {
                stop(taskInfo);
            }
        }
    }

    /**
     * 存放任务名称和花费时间对象
     *
     * @author Looly
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static final class TaskInfo {

        private String taskName;
        private int level;
        private long timeNanos;
        private boolean isStop;

        /**
         * 获取指定单位的任务花费时间
         *
         * @param unit 单位
         * @return 任务花费时间
         * @since 5.7.16
         */
        public long getTime(TimeUnit unit) {
            return unit.convert(this.timeNanos, TimeUnit.NANOSECONDS);
        }

        /**
         * 获取任务花费时间（单位：纳秒）
         *
         * @return 任务花费时间（单位：纳秒）
         * @see #getTimeMillis()
         * @see #getTimeSeconds()
         */
        public long getTimeNanos() {
            return this.timeNanos;
        }

        /**
         * 获取任务名
         *
         * @return 任务名
         */
        public String getTaskName() {
            return this.taskName;
        }


        /**
         * 获取任务花费时间（单位：毫秒）
         *
         * @return 任务花费时间（单位：毫秒）
         * @see #getTimeNanos()
         * @see #getTimeSeconds()
         */
        public long getTimeMillis() {
            return getTime(TimeUnit.MILLISECONDS);
        }

        /**
         * 获取任务花费时间（单位：秒）
         *
         * @return 任务花费时间（单位：秒）
         * @see #getTimeMillis()
         * @see #getTimeNanos()
         */
        public double getTimeSeconds() {
            return DateUtil.nanosToSeconds(this.timeNanos);
        }
    }

//    public static void main(String[] args) throws InterruptedException {
//        final String foreachKey = "开始遍历";
//
//        final PkStopWatch sw = PkStopWatch.create("test");
//
//        final TaskInfo start = sw.start1("开始任务");
//
//        sw.start(1, "初始化参数");
//        final int count = 5;
//        TimeUnit.MILLISECONDS.sleep(RandomUtil.randomInt(1, 200));
//        sw.stop("初始化参数");
//
//        sw.start2(foreachKey);
//
//        sw.start(2, "遍历前定义起始值");
//        final int startIndex = 0;
//        TimeUnit.MILLISECONDS.sleep(RandomUtil.randomInt(1, 100));
//        sw.stop("遍历前定义起始值");
//
//        for (int i = startIndex; i < count; i++) {
//            final String indeName = "遍历值" + i;
//            sw.start3(indeName);
//            System.out.println(i);
//            TimeUnit.MILLISECONDS.sleep(RandomUtil.randomInt(1, 100));
//            sw.stop(indeName);
//        }
//
//        TimeUnit.MILLISECONDS.sleep(RandomUtil.randomInt(1, 100));
//        sw.stop(foreachKey);
//
//        TimeUnit.MILLISECONDS.sleep(RandomUtil.randomInt(1, 100));
//
////        sw.stop(start);
//        sw.stopAll();
//
//
//        System.out.println(sw.prettyPrint(TimeUnit.MILLISECONDS));
//
//    }


}
