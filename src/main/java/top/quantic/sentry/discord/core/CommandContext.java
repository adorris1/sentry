package top.quantic.sentry.discord.core;

import joptsimple.OptionSet;
import sx.blah.discord.handle.obj.IMessage;

public class CommandContext {

    private IMessage message;
    private String prefix;
    private Command command;
    private String[] args;
    private OptionSet optionSet;

    public IMessage getMessage() {
        return message;
    }

    public void setMessage(IMessage message) {
        this.message = message;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public String[] getArgs() {
        return args;
    }

    public void setArgs(String[] args) {
        this.args = args;
    }

    public OptionSet getOptionSet() {
        return optionSet;
    }

    public void setOptionSet(OptionSet optionSet) {
        this.optionSet = optionSet;
    }

    public String getContentAfterPrefix() {
        if (message == null || prefix == null) {
            return null;
        }
        String content = message.getContent();
        if (!content.contains(prefix)) {
            return content;
        }
        return content.substring(prefix.length());
    }
}
