package com.app.util;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

public class WebDriverPool {

	private Hashtable<Integer, Crawling> pool;
	private Queue<Integer> queue;
	
	private static class SingletonHandler {
		private static final WebDriverPool INSTANCE = new WebDriverPool(); 
	}
	
	private WebDriverPool() {
		pool = new Hashtable<Integer, Crawling>();
		queue = new LinkedList<>();
		
		for (int i = 0; i < Config.WEB_DRIVER_COUNT; i++) {
			Integer index = new Integer(i);
			pool.put(index, new Crawling(index));
			queue.add(index);
		}
	}
	
	public static WebDriverPool getInstance() {
		return SingletonHandler.INSTANCE;
	}
	
	public Crawling getCrawling() {
		Integer index = null;
		
		synchronized(this) {
			while (queue.isEmpty()) {
				try {
					wait(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			index = (Integer) queue.remove();
		}
		
		Crawling crawling = (Crawling) pool.get(index);
		return crawling;
	}
	
	public void release(Crawling crawling) {
		synchronized(this) {
			queue.add(crawling.getIndex());
			
			notifyAll();
		}
	}
}
