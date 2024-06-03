import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.Future;
public class Hazel {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Enter the name for thread 1:");
            String thread1Name = scanner.nextLine();
            System.out.println("Enter the name for thread 2:");
            String thread2Name = scanner.nextLine();
            ExecutorService executor = Executors.newFixedThreadPool(2, new NamedThreadFactory());
            System.out.println(thread1Name + " is in NEW state.");
            Future<?> future1 = executor.submit(new MyRunnable(thread1Name));
            System.out.println(thread2Name + " is in NEW state.");
            Future<?> future2 = executor.submit(new MyRunnable(thread2Name));
            System.out.println("\nStarting the threads...");
            executor.shutdown();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                System.out.println("Executor shutdown timed out. Forcing shutdown...");
                executor.shutdownNow();
            }
            if (future1.isDone() && future2.isDone()) {
                System.out.println("\nAfter sleep...");
                System.out.println(thread1Name + " state: " + Thread.State.TERMINATED);
                System.out.println(thread2Name + " state: " + Thread.State.TERMINATED);
            }
        } catch (InterruptedException e) {
            System.out.println("Thread execution interrupted.");
            e.printStackTrace();
        }
    }
    static class MyRunnable implements Runnable {
        private final String threadName;
        private static final Lock lock = new ReentrantLock();
        private static final AtomicInteger counter = new AtomicInteger();
        public MyRunnable(String threadName) {
            this.threadName = threadName;
        }
        public void run() {
            long startTime = System.currentTimeMillis();
            System.out.println("Thread " + threadName + " is in " + Thread.State.RUNNABLE + " state.");
            try {
                lock.lock();
                counter.incrementAndGet();
                Thread.sleep(2000);
                System.out.println("Thread " + threadName + " is in " + Thread.State.TERMINATED + " state after running for " + (System.currentTimeMillis() - startTime) + " ms.");
            } catch (InterruptedException e) {
                System.out.println("Thread " + threadName + " was interrupted.");
            } finally {
                lock.unlock();
            }
        }
    }
    static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            String namePrefix = "MyThreadPool-Thread-";
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
