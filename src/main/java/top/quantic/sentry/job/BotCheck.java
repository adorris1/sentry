package top.quantic.sentry.job;

import com.codahale.metrics.MetricRegistry;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.StatusType;
import top.quantic.sentry.discord.core.ClientRegistry;
import top.quantic.sentry.domain.Bot;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class BotCheck implements Job {

    private static final Logger log = LoggerFactory.getLogger(BotCheck.class);

    @Autowired
    private ClientRegistry clientRegistry;

    @Autowired
    private MetricRegistry metricRegistry;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        for (Map.Entry<Bot, IDiscordClient> entry : clientRegistry.getClients().entrySet()) {
            IDiscordClient client = entry.getValue();
            if (client.isReady()) {
                String botTag = "bot:" + entry.getKey().getName();
                for (IShard shard : client.getShards()) {
                    String shardTag = "shard:" + shard.getInfo()[0];
                    long millis = shard.getResponseTime();
                    log.debug("[{}:{}] Discord WS response time: {} ms", entry.getKey().getName(), shard.getInfo()[0], millis);
                    metricRegistry.timer("discord.ws.response[" + botTag + "," + shardTag + "]")
                        .update(millis, TimeUnit.MILLISECONDS);
                }
                for (IGuild guild : client.getGuilds()) {
                    String guildTag = "guild:" + guild.getID();
                    long online = guild.getUsers().stream()
                        .filter(user -> user.getPresence().getStatus() == StatusType.ONLINE)
                        .count();
                    long other = guild.getUsers().size() - online;
                    String onlineMetric = "discord.ws.users[" + botTag + "," + guildTag + "," + "status:online]";
                    String otherMetric = "discord.ws.users[" + botTag + "," + guildTag + "," + "status:other]";
                    metricRegistry.histogram(onlineMetric).update(online);
                    metricRegistry.histogram(otherMetric).update(other);
                }
            }
        }
    }
}