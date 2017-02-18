package top.quantic.sentry.job;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageList;
import top.quantic.sentry.discord.core.ClientRegistry;
import top.quantic.sentry.event.ChannelPurgeEvent;
import top.quantic.sentry.service.util.Result;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static top.quantic.sentry.discord.util.DiscordUtil.*;
import static top.quantic.sentry.service.util.DateUtil.parseTimeDate;
import static top.quantic.sentry.service.util.MiscUtil.inflect;

public class ScheduledModerator implements Job {

    private static final Logger log = LoggerFactory.getLogger(ScheduledModerator.class);

    @Autowired
    private ClientRegistry clientRegistry;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        String action = jobDataMap.getString("action");
        String bot = jobDataMap.getString("bot");
        String channel = jobDataMap.getString("channel");

        IDiscordClient client;

        if (action == null) {
            throw new JobExecutionException("Parameter 'action' must not be null, add it to the data map!");
        }
        if (bot == null) {
            client = clientRegistry.getClients().entrySet().stream()
                .filter(entry -> entry.getKey().isPrimary())
                .findFirst()
                .map(Map.Entry::getValue).orElse(null);
        } else {
            client = clientRegistry.getClients().entrySet().stream()
                .filter(entry -> bot.equals(entry.getKey().getId())
                    || bot.equals(entry.getKey().getName())
                    || (entry.getValue().isReady() && bot.equals(entry.getValue().getOurUser().getID())))
                .findFirst()
                .map(Map.Entry::getValue).orElse(null);
        }
        if (client == null) {
            throw new JobExecutionException("Parameter 'bot' is missing or invalid, it must map to a registered Bot");
        }
        if (channel == null) {
            throw new JobExecutionException("Parameter 'channel' must not be null, add it to the data map!");
        }

        if ("purge".equals(action)) {
            IChannel target = client.getChannelByID(channel);
            if (target == null) {
                throw new JobExecutionException("Could not find channel with ID " + channel);
            }

            String beforeTimex = jobDataMap.getString("before");
            String afterTimex = jobDataMap.getString("after");
            ZonedDateTime thisMinute = ZonedDateTime.now().truncatedTo(ChronoUnit.MINUTES);
            ZonedDateTime before = beforeTimex == null ? thisMinute.minusDays(13) : parseTimeDate(beforeTimex);
            ZonedDateTime after = afterTimex == null ? thisMinute.minusDays(14) : parseTimeDate(afterTimex);
            if (before == null) {
                throw new JobExecutionException("Could not parse a date from " + beforeTimex);
            }
            if (after == null) {
                throw new JobExecutionException("Could not parse a date from " + afterTimex);
            }

            log.info("Preparing to purge channel {} from {} to {}", humanize(target),
                after.toOffsetDateTime().toString(), before.toOffsetDateTime().toString());

            MessageList messages = target.getMessages();
            int capacity = messages.getCacheCapacity();
            messages.setCacheCapacity(MessageList.UNLIMITED_CAPACITY);

            // collect all offending messages
            List<IMessage> toDelete = new ArrayList<>();
            int i = 0;
            while (true) {
                try {
                    IMessage msg = messages.get(i++);
                    if (msg.getTimestamp().isAfter(before.toLocalDateTime())) {
                        continue;
                    }
                    if (msg.getTimestamp().isBefore(after.toLocalDateTime())) {
                        log.debug("Search interrupted after hitting date constraint");
                        break;
                    }
                    toDelete.add(msg);
                } catch (ArrayIndexOutOfBoundsException e) {
                    // we reached the end apparently
                    log.warn("Could not retrieve messages to delete: {}", e.getMessage());
                    break;
                }
            }

            if (toDelete.size() > 1 && (before.isBefore(thisMinute.minusWeeks(2)) || after.isBefore(thisMinute.minusWeeks(2)))) {
                throw new JobExecutionException("Cannot bulk delete messages before 2 weeks ago");
            }

            log.info("Deleting {} after searching through {}", inflect(toDelete.size(), "message"), inflect(i, "message"));

            // bulk delete requires at least 2 messages
            CompletableFuture<Result<Integer>> future;
            if (toDelete.size() == 1) {
                future = CompletableFuture.supplyAsync(() -> deleteMessage(toDelete.get(0)).get());
            } else {
                future = CompletableFuture.supplyAsync(() -> deleteInBatch(target, toDelete));
            }
            future.whenComplete((result, error) -> {
                if (result != null) {
                    if (!result.isSuccessful()) {
                        log.warn("Error: {}", result.getMessage());
                    } else {
                        if (!result.getContent().equals(toDelete.size())) {
                            log.warn("Some messages could not be deleted: {} out of {}", result.getContent(), toDelete.size());
                        }
                        log.info("{}", (toDelete.size() == 0 ? "No messages were deleted" : "Deleted " + inflect(toDelete.size(), "message")));
                        publisher.publishEvent(new ChannelPurgeEvent(target, toDelete));
                    }
                }
                if (error != null) {
                    log.warn("Error while deleting messages");
                }
            }).thenRun(() -> messages.setCacheCapacity(capacity));
        } else {
            log.warn("Unknown action: {}", action);
        }
    }
}