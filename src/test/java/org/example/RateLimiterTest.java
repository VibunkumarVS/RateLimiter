package org.example;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class RateLimiterTest {

    @Before
    public void changeLogLevel() {
        Configurator.setAllLevels("", Level.ALL);
    }

    @Test
    public void testTryConsume_SingleThread_ValidScenario() throws InterruptedException, ExecutionException {
        Integer refreshInterval = 2;
        RateLimiter rateLimiter = new RateLimiter(5, refreshInterval);

        int iterations=0;
        while (iterations<10) {
            System.out.println("Iteration: " + iterations++);
            Assert.assertTrue(rateLimiter.tryConsume(5));
            Assert.assertFalse(rateLimiter.tryConsume(1));

            Thread.sleep(refreshInterval*1000);

            Boolean response = rateLimiter.tryConsume(5);
            System.out.println("Response from call " + response);

            Assert.assertEquals(response, Boolean.TRUE);
            Assert.assertFalse(rateLimiter.tryConsume(1));

            Thread.sleep(refreshInterval*1000);
        }
    }
}
