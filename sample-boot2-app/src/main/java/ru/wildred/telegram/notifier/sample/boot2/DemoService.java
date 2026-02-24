package ru.wildred.telegram.notifier.sample.boot2;

import org.springframework.stereotype.Service;
import ru.wildred.telegram.notifier.core.annotation.NotifyWhen;
import ru.wildred.telegram.notifier.core.annotation.TelegramNotify;
import ru.wildred.telegram.notifier.core.dispatch.ParseMode;

@Service
public class DemoService {
    @TelegramNotify(
            message = "'[BEFORE] method=' + #methodName + ', order=' + #orderId + ', amount=' + #amount",
            condition = "#amount > 0",
            when = NotifyWhen.BEFORE,
            parseMode = ParseMode.MARKDOWN
    )
    public String beforeNotification(String orderId, long amount) {
        return "accepted-" + orderId;
    }

    @TelegramNotify(
            message = "'[SUCCESS] order=' + #orderId + ', result=' + #result",
            condition = "#result.startsWith('ok')",
            when = NotifyWhen.AFTER_SUCCESS,
            parseMode = ParseMode.HTML
    )
    public String successNotification(String orderId) {
        return "ok-" + orderId;
    }

    @TelegramNotify(
            message = "'[FAILURE] order=' + #orderId + ', ex=' + #ex.message",
            when = NotifyWhen.AFTER_FAILURE
    )
    public String failureNotification(String orderId) {
        throw new IllegalStateException("payment failed for " + orderId);
    }

    @TelegramNotify(
            message = "'[FINALLY] order=' + #orderId + ', result=' + #result + ', ex=' + (#ex != null ? #ex.message : 'none')",
            when = NotifyWhen.AFTER_FINALLY
    )
    public String finallyNotification(String orderId, boolean fail) {
        if (fail) {
            throw new IllegalArgumentException("forced fail for " + orderId);
        }
        return "done-" + orderId;
    }
}
