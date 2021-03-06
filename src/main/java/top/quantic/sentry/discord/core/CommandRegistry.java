package top.quantic.sentry.discord.core;

import org.springframework.stereotype.Component;
import sx.blah.discord.api.IDiscordClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CommandRegistry {

    private Map<IDiscordClient, List<Command>> commandsMap = new ConcurrentHashMap<>();

    public List<Command> getCommands(IDiscordClient client) {
        return commandsMap.computeIfAbsent(client, k -> new ArrayList<>());
    }

    public void addAll(IDiscordClient client, Collection<Command> commands) {
        getCommands(client).addAll(commands);
    }

    public List<Command> remove(IDiscordClient client) {
        return commandsMap.remove(client);
    }
}
