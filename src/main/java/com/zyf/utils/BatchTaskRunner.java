package com.zyf.utils;

import cn.hutool.core.util.PageUtil;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class BatchTaskRunner {

    private BatchTaskRunner() {
    }

    /**
     * 多线程批量执行任务
     *
     * @param count             计数
     * @param size              尺寸
     * @param pageQueryFunction 页面查询功能
     * @return {@link List}<{@link T}>
     */
    public static <T> List<T> execute(int count, int size, BiFunction<Integer, Integer, List<T>> pageQueryFunction) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        PageUtil.setFirstPageNo(1);

        int curPage = 1;
        BatchTask<T> task = new BatchTask<>(count, size, pageQueryFunction, curPage);
        return forkJoinPool.invoke(task);
    }

    /**
     * 多线程批量执行任务
     *
     * @param taskList  待处理的集合
     * @param threshold 分片的阈值
     * @param action    消费函数
     */
    public static <T> void execute(List<T> taskList, int threshold, Consumer<List<T>> action) {
        new BatchAction<T>(taskList, threshold, action).invoke();
    }

    /**
     * 多线程批量执行任务
     *
     * @param taskList  待处理的集合
     * @param threshold 分片的阈值
     * @param action    消费函数
     */
    public static <T, R> List<R> execute(List<T> taskList, int threshold, Function<List<T>, List<R>> action) {
        return new BatchTask2<T, R>(taskList, threshold, action).invoke();
    }

    private static class BatchAction<T> extends RecursiveAction {
        private int threshold = 1000;
        private final List<T> taskList;
        private final Consumer<List<T>> action;

        private BatchAction(List<T> taskList, int threshold, Consumer<List<T>> action) {
            this.taskList = taskList;
            this.threshold = threshold;
            this.action = action;
        }

        @Override
        protected void compute() {
            if (taskList.size() <= threshold) {
                this.action.accept(taskList);
            } else {
                int middle = (int) Math.ceil(taskList.size() / 2.0);
                List<T> leftList = taskList.subList(0, middle);
                List<T> rightList = taskList.subList(middle, taskList.size());
                BatchAction<T> left = new BatchAction<>(leftList, threshold, action);
                BatchAction<T> right = new BatchAction<>(rightList, threshold, action);
                ForkJoinTask.invokeAll(left, right);
            }
        }

    }

    private static class BatchTask<T> extends RecursiveTask<List<T>> {
        private final int count;
        private final int size;
        private int curPage;
        private final BiFunction<Integer, Integer, List<T>> pageQueryFunction;

        public BatchTask(int count, int size, BiFunction<Integer, Integer, List<T>> pageQueryFunction, int curPage) {
            this.count = count;
            this.size = size;
            this.pageQueryFunction = pageQueryFunction;
            this.curPage = curPage;
        }

        @Override
        protected List<T> compute() {
            final int[] transed = PageUtil.transToStartEnd(curPage, size);
            final int tempCount = this.count - transed[0];
            if (tempCount <= size) {
                return pageQueryFunction.apply(curPage, tempCount);
            } else {
                final int[] startEnd = PageUtil.transToStartEnd(curPage, size);
                final int endIndex = startEnd[1];
                BatchTask<T> leftTask = new BatchTask<>(endIndex, size, pageQueryFunction, curPage);
                BatchTask<T> rightTask = new BatchTask<>(this.count, size, pageQueryFunction, ++curPage);

                // 部分同步部分异步
//                leftTask.fork();
//                List<T> rightResult = rightTask.compute();
//                List<T> leftResult = leftTask.join();

                // 完全异步
                leftTask.fork();
                rightTask.fork();
                List<T> leftResult = leftTask.join();
                List<T> rightResult = rightTask.join();

                rightResult.addAll(leftResult);
                return rightResult;
            }
        }
    }

    private static class BatchTask2<T, R> extends RecursiveTask<List<R>> {
        private final List<T> taskList;
        private final int size;
        private final Function<List<T>, List<R>> func;

        public BatchTask2(List<T> taskList, int size, Function<List<T>, List<R>> func) {
            this.taskList = taskList;
            this.size = size;
            this.func = func;
        }

        @Override
        protected List<R> compute() {
            if (taskList.size() <= size) {
                return func.apply(taskList);
            } else {
                int middle = (int) Math.ceil(taskList.size() / 2.0);
                List<T> leftList = taskList.subList(0, middle);
                List<T> rightList = taskList.subList(middle, taskList.size());
                BatchTask2<T, R> leftTask = new BatchTask2<>(leftList, size, func);
                BatchTask2<T, R> rightTask = new BatchTask2<>(rightList, size, func);

                // 部分同步部分异步
//                leftTask.fork();
//                List<T> rightResult = rightTask.compute();
//                List<T> leftResult = leftTask.join();

                // 完全异步
                leftTask.fork();
                rightTask.fork();
                List<R> leftResult = leftTask.join();
                List<R> rightResult = rightTask.join();

                rightResult.addAll(leftResult);
                return rightResult;
            }
        }
    }

//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class TestUser {
//        private String name;
//    }
//
//    public static void main(String[] args) {
//        {
//            int size = 3;
//            final List<TestUser> list = new ArrayList<>();
//            list.add(new TestUser("1"));
//            list.add(new TestUser("2"));
//            list.add(new TestUser("3"));
//            list.add(new TestUser("4"));
//            list.add(new TestUser("5"));
//            list.add(new TestUser("6"));
//            final List<TestUser> users = execute(list, size, tempList -> {
//                final List<String> collect = tempList.stream().map(e -> e.getName()).collect(Collectors.toList());
//                System.out.printf("[%s]: %s\n", Thread.currentThread().getName(), CollUtil.join(collect, ","));
//                return tempList;
//            });
//            for (TestUser user : users) {
//                System.out.println(user.getName());
//            }
//        }
//        {
//            int count = 3001;
//            int size = 1000;
//            execute(count, size, (currentPage, pageSize) -> {
//                List<TestUser> users = new ArrayList<>();
//                for (int i = 1; i <= pageSize; i++) {
//                    System.out.printf("[%s]: %s\n", Thread.currentThread().getName(), currentPage + "_" + i);
//                }
//                return users;
//            });
//        }
//        {
//            List<Integer> allTasks = Arrays.asList(1, 2, 3, 4, 5);
//            int taskPerThread = 2;
//            BatchTaskRunner.execute(allTasks, taskPerThread, tasks -> {
//                System.out.printf("[%s]: %s\n", Thread.currentThread().getName(), CollUtil.join(tasks, ","));
//            });
//        }
//    }

}
