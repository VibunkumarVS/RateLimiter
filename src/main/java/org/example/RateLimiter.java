package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


public class RateLimiter {

    private AtomicInteger remainingTokens;
    private final int allowedTokens;
    private final Logger log = LogManager.getLogger();
    // Using ScheduledThreadPoolExecutor is a bad design choice as it doesn't guarantee precision in the next task
    // execution. It is inconsistent and will lead to **Race Condition**
    private final ScheduledThreadPoolExecutor executorService;

    public RateLimiter(int maxAllowedTokensPerMin, int refreshIntervalInSeconds) throws ExecutionException, InterruptedException {
        this.allowedTokens = maxAllowedTokensPerMin;
        remainingTokens = new AtomicInteger();
        executorService = new ScheduledThreadPoolExecutor(2);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                refreshTokens();
            }
        }, 0, TimeUnit.SECONDS.toNanos(refreshIntervalInSeconds), TimeUnit.NANOSECONDS);

        while(executorService.getCompletedTaskCount() == 0){
            log.debug("Awaiting initial token updation");
            // do nothing;
        }
    }

    public void refreshTokens() {
        this.remainingTokens.set(allowedTokens);
        log.info("Tokens Refreshed");
    }

    private int getAllowedTokens() {
        return this.remainingTokens.get();
    }

    private void deductTokens(int updatedValue) {
//        if (this.remainingTokens.get() > updatedValue)
//            return;
        this.remainingTokens.set(updatedValue);
    }

    /**
     * The main rate-limiter functions that decides if the requests should be allowed or rate limited
     * @param numberOfRequests No of Requests to be consumed
     * @return If the request is allowed or not
     */
    public Boolean tryConsume(int numberOfRequests) {
        try {
            return checkIfTokensAvailable(numberOfRequests);
        } catch (Exception e) {
            log.error("Unexpected exception occurred. Exception Details: " + e);
        }
        return false;
    }

    private Boolean checkIfTokensAvailable(int numberOfRequests) {
        int currentAllowedTokens = getAllowedTokens();
        log.info("Current number of tokens available: " + currentAllowedTokens);
        if (currentAllowedTokens >= numberOfRequests) {
            deductTokens(currentAllowedTokens-numberOfRequests);
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}
