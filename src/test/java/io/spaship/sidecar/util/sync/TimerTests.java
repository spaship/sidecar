package io.spaship.sidecar.util.sync;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TimerTests {


    @SneakyThrows
    @Test
    void givenUsingTimer_whenSchedulingTaskOnce_thenCorrect() {
        System.out.println("execution running on: " + Thread.currentThread().getName());
        TimerTask task = new TimerTask() {
            public void run() {
                System.out.println("Task performed on: " + new Date() + "\n" +
                        "Thread's name: " + Thread.currentThread().getName());
            }
        };
        Timer timer = new Timer("Timer");

        long delay = 500L;
        timer.schedule(task, delay);
        Thread.sleep(5000);
        System.out.println("end of thread sleep");
    }

}
