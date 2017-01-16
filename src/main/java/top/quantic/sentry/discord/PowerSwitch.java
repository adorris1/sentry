package top.quantic.sentry.discord;

import org.ocpsoft.prettytime.shade.edu.emory.mathcs.backport.java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import top.quantic.sentry.discord.command.Command;
import top.quantic.sentry.discord.command.CommandBuilder;
import top.quantic.sentry.discord.command.CommandContext;
import top.quantic.sentry.discord.module.CommandSupplier;

import java.util.List;

import static top.quantic.sentry.discord.util.DiscordUtil.answer;
import static top.quantic.sentry.discord.util.DiscordUtil.deleteMessage;
import static top.quantic.sentry.discord.util.DiscordUtil.ourBotHash;

@Component
public class PowerSwitch implements CommandSupplier {

    private static final Logger log = LoggerFactory.getLogger(PowerSwitch.class);

    @Override
    public List<Command> getCommands() {
        return Collections.singletonList(logout());
    }

    private Command logout() {
        return CommandBuilder.of("logout")
            .describedAs("Disconnect from Discord")
            .in("Power")
            .nonParsed()
            .secured()
            .onExecute(context -> {
                IMessage message = context.getMessage();
                String[] args = context.getArgs();
                if (args.length >= 1) {
                    String hash = ourBotHash(message.getClient());
                    if (args[1].equals(hash)) {
                        deleteMessage(message);
                        doLogout(context);
                    }
                } else {
                    deleteMessage(message);
                    answer(message, ":wave:");
                    doLogout(context);
                }
            }).build();
    }

    private void doLogout(CommandContext context) {
        IDiscordClient client = context.getMessage().getClient();
        log.info("[{}] Logging out via command", client.getOurUser().getName());
        if (client.isLoggedIn()) {
            try {
                client.logout();
            } catch (DiscordException e) {
                log.warn("Could not logout bot", e);
            }
        }
    }
}