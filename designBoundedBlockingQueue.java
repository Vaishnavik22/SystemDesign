/*
Problem: https://leetcode.com/problems/design-bounded-blocking-queue/

Implement a thread-safe bounded blocking queue that has the following methods:

- BoundedBlockingQueue(int capacity) The constructor initializes the queue with a maximum capacity.
- void enqueue(int element) Adds an element to the front of the queue. If the queue is full, the calling thread is blocked until the queue is no longer full.
- int dequeue() Returns the element at the rear of the queue and removes it. If the queue is empty, the calling thread is blocked until the queue is no longer empty.
- int size() Returns the number of elements currently in the queue.
Your implementation will be tested using multiple threads at the same time. Each thread will either be a producer thread that only makes calls to the enqueue method or a consumer thread that only makes calls to the dequeue method. The size method will be called after every test case.

Please do not use built-in implementations of bounded blocking queue as this will not be accepted in an interview.
*/
import java.util.concurrent.locks.ReentrantLock; 
import java.util.concurrent.locks.Condition;

class BoundedBlockingQueue {
    int queue[] = null;
    int size;
    int tail, head;
    ReentrantLock lock;
    Condition notEmpty, notFull;
    
    public BoundedBlockingQueue(int capacity) {
        lock = new ReentrantLock();
        notFull = lock.newCondition();
        notEmpty = lock.newCondition();
        size = 0;
        tail = 0;
        head = 0;
        queue = new int[capacity];
    }
    
    public void enqueue(int element) throws InterruptedException {
        lock.lock();
        try {
            while (size == queue.length) {
                notFull.await();
            }
            ++size;
            queue[tail] = element;
            tail = (tail + 1) % queue.length;
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }
    
    public int dequeue() throws InterruptedException {
        lock.lock();
        try {
            while (size == 0) {
                notEmpty.await();
            }
            int element = queue[head];
            head = (head + 1) % queue.length;
            --size;
            notFull.signal();
            return element;
        } finally {
            lock.unlock();
        }
    }
    
    public int size() {
        lock.lock();
        try {
            return this.size;
        } finally {
            lock.unlock();
        }
        
    }
}