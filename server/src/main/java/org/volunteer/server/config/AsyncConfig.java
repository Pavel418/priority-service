package org.volunteer.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ExecutorService gaExecutor() {
        /* Single background pool – only one optimisation at a time.
           Switch to cached/fixed pool if you plan to run multiple   */
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ga-worker");
            t.setDaemon(true);
            return t;
        });
    }
} 