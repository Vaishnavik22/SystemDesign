/*
Given a URL startUrl and an interface HtmlParser, implement a Multi-threaded web crawler to crawl all links that are under the same hostname as startUrl.

Return all URLs obtained by your web crawler in any order.

Your crawler should:

- Start from the page: startUrl
- Call HtmlParser.getUrls(url) to get all URLs from a webpage of a given URL.
- Do not crawl the same link twice.
- Explore only the links that are under the same hostname as startUrl.
*/


/**
 * // This is the HtmlParser's API interface.
 * // You should not implement it, or speculate about its implementation
 * interface HtmlParser {
 *     public List<String> getUrls(String url) {}
 * }
 */
class Solution {
    
    HtmlParser htmlParser = null;
    List<String> result = null;
    Queue<Future> tasks = null;
    // LinkedBlockingQueue is a blocking implementation - will block until queue is not empty.
    // Use ConcurrentLinkedQueue instead which is a non-blocking queue
    
    ConcurrentLinkedQueue<String> queue = null;
    HashSet<String> visitedUrls = null;
    String hostname = null;
    
    private class Crawler implements Callable<Integer> {
        String startUrl;
        
        public Crawler(String url) {
            this.startUrl = url;
        }
        
        public Integer call() {
            List<String> newUrls = htmlParser.getUrls(this.startUrl);
            
            for (String url : newUrls) {
                queue.offer(url);
            }
            return 0;
        }
    }
    
    public List<String> crawl(String startUrl, HtmlParser htmlParser) {
        this.htmlParser = htmlParser;
        result = new ArrayList<>();
        tasks = new LinkedList<>();
        queue = new ConcurrentLinkedQueue<>();
        visitedUrls = new HashSet<>();
        hostname = getHostname(startUrl);
        
        queue.offer(startUrl);
        
        ExecutorService executor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
        
        while (true) {
            if (!queue.isEmpty()) {
                String curUrl = queue.poll();
                if (getHostname(curUrl).equals(hostname) && !visitedUrls.contains(curUrl)) {
                    System.out.println("Processing URL: " + curUrl);
                    result.add(curUrl);
                    visitedUrls.add(curUrl);
                    tasks.add(executor.submit(new Crawler(curUrl)));
                }
            } else {
                if (!tasks.isEmpty()) {
                    System.out.println("No urls in queue");
                    try {
                        Future task = tasks.poll();
                        System.out.println("Getting task");
                        task.get();
                    } catch (Exception e) {
                        System.out.println("Exception: " + e.getMessage());
                    }
                } else {
                    break;
                }
            }
        }
        
        return result;
    }
    
    private String getHostname(String url) {
        url = url.substring(7);
        return url.split("/")[0];
    }
}