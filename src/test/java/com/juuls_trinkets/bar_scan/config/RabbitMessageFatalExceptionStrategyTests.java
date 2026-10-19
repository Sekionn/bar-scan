package com.juuls_trinkets.bar_scan.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.juuls_trinkets.bar_scan.exception.ResourceNotFoundException;
import com.juuls_trinkets.bar_scan.exception.UnassignedShopMessageException;

class RabbitMessageFatalExceptionStrategyTests {

    private final RabbitMessageFatalExceptionStrategy strategy = new RabbitMessageFatalExceptionStrategy();

    @Test
    void rejectsUnassignedShopMessageFailuresWithoutRequeueing() {
        Throwable failure = new RuntimeException(new UnassignedShopMessageException());

        assertThat(strategy.isFatal(failure)).isTrue();
    }

    @Test
    void leavesOtherDomainFailuresRetryable() {
        assertThat(strategy.isFatal(new ResourceNotFoundException("User not found"))).isFalse();
    }

    @Test
    void leavesUnexpectedFailuresRetryable() {
        assertThat(strategy.isFatal(new IllegalStateException("Database connection interrupted"))).isFalse();
    }
}
