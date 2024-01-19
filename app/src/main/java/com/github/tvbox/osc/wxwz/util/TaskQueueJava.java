package com.github.tvbox.osc.wxwz.util;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class TaskQueueJava {
    private final String TAG = "TaskQueueJava";
    private BlockingQueue<Runnable> queue;
    private Thread workerThread;
    private boolean isDoingTask = false;


    public TaskQueueJava() {
        // 使用ArrayBlockingQueue作为任务队列，可以根据需要选择其他队列实现
        this.queue = new LinkedBlockingQueue<>();
    }

    public void addTask(Runnable task) {
        boolean res = queue.offer(task); // 将任务添加到队列尾部

        if (isDoingTask||isQueueEmpty()){
            Log.i(TAG,"上一个任务未结束," + (res?"已添加": "未添加") + "任务到列表，当前队列大小:" + queue.size());
            return;
        }else {
            isDoingTask = true;
            Log.i(TAG,"任务开始!");
            // 启动消费者线程，负责执行队列中的任务
            this.workerThread = new Thread(() -> {
                while (!isQueueEmpty()) {
                    try {
                        Runnable Rtask = queue.poll(100, TimeUnit.MILLISECONDS); // 获取并移除队列头部的任务，如果队列为空，则返回null
                        if (Rtask != null) {
                            Rtask.run(); // 执行任务
                        }
                        Log.i(TAG,"任务结束,等候中的数量为:" + queue.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (isQueueEmpty()){
                            isDoingTask = false;
                        }
                    }
                }
                isDoingTask = false;
                Log.i(TAG,"所有任务结束,当前队列大小:" + queue.size());
            });
            workerThread.start();
        }

    }

    public boolean isQueueEmpty() {
        return queue.isEmpty();
    }

    public void stop() {
        queue.clear();
        if (workerThread==null){
            return;
        }
        try {
            // 等待消费者线程执行完毕
            workerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        workerThread.interrupt(); // 停止消费者线程

    }
}

