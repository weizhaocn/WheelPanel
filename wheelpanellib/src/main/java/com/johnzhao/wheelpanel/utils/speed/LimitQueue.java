package com.johnzhao.wheelpanel.utils.speed;

import java.util.LinkedList;

public class LimitQueue<E> {

    private int limit;
    private LinkedList<E> queue = new LinkedList<E>();

    public LimitQueue(int limit){
        this.limit = limit;
    }

    public void offer(E e){
        if(queue.size() >= limit){
            queue.poll();
        }
        queue.offer(e);
    }

    public E get(int position){
        return queue.get(position);
    }

    public E getLast(){
        return queue.getLast();
    }

    public E getFirst(){
        return queue.getFirst();
    }

    public int getLimit(){
        return limit;
    }

    public int size(){
        return queue.size();
    }

    public void clear(){
        queue.clear();
    }
}
