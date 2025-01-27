package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.json.simple.JSONArray;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Bots.CommandEvent.localise;
import static Bots.Main.*;

public class CommandBlockChannel extends BaseCommand {
    Pattern pattern = Pattern.compile("^<#(\\d+)>$"); //To support rawtext #channel additions (slash commands auto convert to just ID which is really nice)

    @Override
    public void execute(CommandEvent event) {
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {
            event.replyEmbeds(createQuickError(localise("Main.noPermission")));
            return;
        }
        String[] args = event.getArgs();
        if (args.length == 1 || !args[1].equalsIgnoreCase("list")) {
            if (args.length < 3) {
                event.replyEmbeds(createQuickError(String.format(localise("CommandBlockChannel.invalidUsage"), "`blockchannel <remove/add>", "`", "`blockchannel list`")));
                return;
            }
        }

        JSONArray blockedChannels = (JSONArray) event.getConfig().get("BlockedChannels");
        if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
            String targetChannel = args[2];

            Matcher matcher = pattern.matcher(args[2]);
            if (matcher.matches()) {
                targetChannel = matcher.group(1);
            }
            for (GuildChannel guildChannel : event.getGuild().getChannels()) {
                if (guildChannel.getId().equals(targetChannel)) {
                    if (args[1].equalsIgnoreCase("add")) {
                        if (blockedChannels.contains(guildChannel.getId())) {
                            event.replyEmbeds(createQuickError(localise("CommandBlockChannel.alreadyBlocked")));
                            return;
                        }
                        blockedChannels.add(targetChannel);
                        event.replyEmbeds(createQuickEmbed(" ", "✅ " + String.format(localise("CommandBlockChannel.added"), "<#" + guildChannel.getIdLong() + ">")));
                    } else if (args[1].equalsIgnoreCase("remove")) {
                        if (!blockedChannels.contains(guildChannel.getId())) {
                            event.replyEmbeds(createQuickError(localise("CommandBlockChannel.notBlocked")));
                            return;
                        }
                        blockedChannels.remove(targetChannel);
                        event.replyEmbeds(createQuickEmbed(" ", "✅ " + String.format(localise("CommandBlockChannel.removed"), "<#" + guildChannel.getIdLong() + ">")));
                    }
                    return;
                }
            }
            event.replyEmbeds(createQuickError(localise("CommandBlockChannel.notFound")));
        } else if (args[1].equalsIgnoreCase("list")) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(botColour);
            eb.setTitle(String.format(localise("CommandBlockChannel.channelList"), event.getGuild().getName()) + ":");
            if (blockedChannels.isEmpty()) {
                eb.appendDescription("**" + localise("CommandBlockChannel.noChannels") + "**");
                event.replyEmbeds(eb.build());
                return;
            }
            for (Object blockedChannel : blockedChannels) {
                eb.appendDescription("* <#" + blockedChannel + ">\n");
            }
            event.replyEmbeds(eb.build());
        } else {
            event.replyEmbeds(createQuickError(String.format(localise("CommandBlockChannel.invalidUsage"), "`blockchannel <remove/add>", "`", "`blockchannel list`")));
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"blockchannel"};
    }

    @Override
    public Category getCategory() {
        return Category.Admin;
    }

    @Override
    public String getDescription() {
        return "Prevents certain audio-related commands from being used in specified channels (eg: play)";
    }

    @Override
    public String getOptions() {
        return "list | <add/remove> <channel>";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addSubcommands(
                new SubcommandData("list", "List the blocked channels"),
                new SubcommandData("add", "Add a channel to the blocklist").addOptions(
                        new OptionData(OptionType.CHANNEL, "channel-id", "The channel for the command", true)
                ),
                new SubcommandData("remove", "Remove a channel from the blocklist").addOptions(
                        new OptionData(OptionType.CHANNEL, "channel-id", "The channel for the command", true)
                )
        );
    }

    @Override
    public long getRatelimit() {
        return 1000;
    }
}
