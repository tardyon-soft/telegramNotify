package ru.wildred.telegram.notifier.sample.boot3;

import org.springframework.stereotype.Service;
import ru.wildred.telegram.notifier.core.annotation.NotifyWhen;
import ru.wildred.telegram.notifier.core.annotation.TelegramNotify;
import ru.wildred.telegram.notifier.core.dispatch.ErrorPolicy;
import ru.wildred.telegram.notifier.core.dispatch.ParseMode;

@Service
public class DemoService {
    @TelegramNotify(
            message = "'[BEFORE] method=' + #methodName + ', account=' + #accountId + ', amount=' + #amount",
            condition = "#amount > 0",
            when = NotifyWhen.BEFORE
    )
    public String beforeNotification(String accountId, long amount) {
        return "accepted-" + accountId;
    }

    @TelegramNotify(
            message = "'<b>[SUCCESS]</b> account=' + #accountId + ', result=' + #result",
            condition = "#result.startsWith('ok')",
            when = NotifyWhen.AFTER_SUCCESS,
            parseMode = ParseMode.HTML
    )
    public String successNotification(String accountId) {
        return "ok-" + accountId;
    }

    @TelegramNotify(
            message = "'[FAILURE] account=' + #accountId + ', ex=' + #ex.message",
            when = NotifyWhen.AFTER_FAILURE,
            errorPolicy = ErrorPolicy.LOG_ONLY
    )
    public String failureNotification(String accountId) {
        throw new IllegalStateException("transfer failed for " + accountId);
    }

    @TelegramNotify(
            message = "'[FINALLY] account=' + #accountId + ', result=' + #result + ', ex=' + (#ex != null ? #ex.message : 'none')",
            when = NotifyWhen.AFTER_FINALLY
    )
    public String finallyNotification(String accountId, boolean fail) {
        if (fail) {
            throw new IllegalArgumentException("forced fail for " + accountId);
        }
        return "done-" + accountId;
    }
}
