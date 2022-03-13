package kr.reciptopia.reciptopiaserver.helper;

import java.util.function.Consumer;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public record TransactionHelper(
    TransactionTemplate transactionTemplate) {

    // With status argument, return callback result
    public <T> T doInTransaction(TransactionCallback<T> callback) {
        return transactionTemplate.execute(callback);
    }

    // With status argument, return no result
    public void doInTransaction(Consumer<TransactionStatus> callback) {
        transactionTemplate.executeWithoutResult(callback);
    }

    // Without status argument, return callback result
    public <T> T doInTransaction(Supplier<T> callback) {
        return transactionTemplate.execute(status -> callback.get());
    }

    // Without status argument, return no result
    public void doInTransaction(Runnable callback) {
        transactionTemplate.executeWithoutResult(status -> callback.run());
    }

}