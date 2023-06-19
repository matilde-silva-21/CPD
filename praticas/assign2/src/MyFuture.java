public class MyFuture<T> {
    private T result;
    private boolean isDone;
    private final Object lock = new Object();

    public MyFuture() {
        isDone = false;
    }

    public void setResult(T result) {
        synchronized (lock) {
            this.result = result;
            isDone = true;
            lock.notifyAll();
        }
    }

    public T get() throws InterruptedException {
        synchronized (lock) {
            while (!isDone) {
                lock.wait();
            }
            return result;
        }
    }

    public boolean isDone() {
        synchronized (lock) {
            return isDone;
        }
    }
}

