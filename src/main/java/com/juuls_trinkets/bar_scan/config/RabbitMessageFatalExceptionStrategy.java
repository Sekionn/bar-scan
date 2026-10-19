package com.juuls_trinkets.bar_scan.config;

import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler.DefaultExceptionStrategy;
import org.springframework.amqp.rabbit.listener.FatalExceptionStrategy;

import com.juuls_trinkets.bar_scan.exception.UnassignedShopMessageException;

public class RabbitMessageFatalExceptionStrategy implements FatalExceptionStrategy {

    private final DefaultExceptionStrategy defaultExceptionStrategy = new DefaultExceptionStrategy();

    @Override
    public boolean isFatal(Throwable throwable) {
        return defaultExceptionStrategy.isFatal(throwable)
                || containsUnassignedShopMessageFailure(throwable);
    }

    private boolean containsUnassignedShopMessageFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof UnassignedShopMessageException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
