package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

import static Bots.Main.*;

public class CommandQueue extends BaseCommand {
    private static final HashMap<Long, Integer> queuePages = new HashMap<>();

    private void HandleButtonEvent(ButtonInteractionEvent event) {
        final GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(Objects.requireNonNull(event.getGuild()));
        final AudioTrack track = manager.audioPlayer.getPlayingTrack();
        final BlockingQueue<AudioTrack> Queue = manager.scheduler.queue;
        int newPageNumber = 1;
        if (Objects.equals(event.getButton().getId(), "forward")) {
            newPageNumber = queuePages.getOrDefault(event.getGuild().getIdLong(), 1) + 1;
        } else if (Objects.equals(event.getButton().getId(), "backward")) {
            newPageNumber = queuePages.getOrDefault(event.getGuild().getIdLong(), 1) - 1;
        }
        int maxPage = (Queue.size() + 4) / 5;
        newPageNumber = Math.floorMod(newPageNumber - 1, maxPage) + 1; //wrap around (1-indexed)
        queuePages.put(event.getGuild().getIdLong(), newPageNumber);
        long queueTimeLength = 0;
        for (AudioTrack queueTrack : Queue) {
            if (queueTrack.getInfo().length < 432000000) {
                queueTimeLength = queueTimeLength + queueTrack.getInfo().length;
            }
        }
        EmbedBuilder eb = new EmbedBuilder();
        for (int j = 5 * newPageNumber - 5; j < 5 * newPageNumber && j < Queue.size(); j++) {
            AudioTrackInfo trackInfo = Objects.requireNonNull(getTrackFromQueue(event.getGuild(), j)).getInfo();
            eb.appendDescription(j + 1 + ". [" + trackInfo.title + "](" + trackInfo.uri + ")\n");
        }
        eb.setTitle("__**Now playing:**__\n" + track.getInfo().title, track.getInfo().uri);
        eb.setFooter(Queue.size() + " songs queued | Page " + newPageNumber + "/" + maxPage + " | Length: " + toTimestamp(queueTimeLength));
        eb.setColor(botColour);
        if (PlayerManager.getInstance().getThumbURL(track) != null)
            eb.setThumbnail(PlayerManager.getInstance().getThumbURL(track));
        event.getInteraction().editMessageEmbeds(eb.build()).queue();
    }

    @Override
    public void Init() {
        registerButtonInteraction(new String[]{"forward", "backward"}, this::HandleButtonEvent);
    }

    @Override
    public void execute(MessageEvent event) {
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("Im not in a vc."));
            return;
        }
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.queue);
        if (queue.isEmpty()) {
            event.replyEmbeds(createQuickError("The queue is empty."));
            return;
        }
        EmbedBuilder embed = new EmbedBuilder();
        AudioTrack track = audioPlayer.getPlayingTrack();
        String title = track.getInfo().title;
        if (track.getInfo().title == null) title = "Unknown title";
        embed.setTitle("__**Now playing:**__\n" + title, track.getInfo().uri);
        int queueLength = queue.size();
        long queueTimeLength = 0;
        for (AudioTrack audioTrack : queue) {
            if (audioTrack.getInfo().length > 432000000) {
                continue; // will be slightly inaccurate due to tracks with unknown duration
            }
            queueTimeLength = queueTimeLength + audioTrack.getInfo().length;
        }
        String[] args = event.getArgs();
        int pageNumber = 1;
        if (args.length >= 2) {
            if (args[1].equalsIgnoreCase("clear")) {
                event.replyEmbeds(createQuickError("Did you mean to use **clearqueue**?"));
                return;
            }
            if (!args[1].matches("^\\d+$")) {
                event.replyEmbeds(createQuickError("The page must be a positive integer."));
                return;
            }
            pageNumber = Math.max(Integer.parseInt(args[1]), 1); //page 0 is a bad idea
        }
        queuePages.put(event.getGuild().getIdLong(), pageNumber);
        for (int i = 5 * pageNumber - 5; i < 5 * pageNumber && i < queueLength; i++) {
            AudioTrackInfo trackInfo = queue.get(i).getInfo();
            embed.appendDescription(i + 1 + ". [" + trackInfo.title + "](" + trackInfo.uri + ")\n");
        }
        embed.setFooter(queueLength + " songs queued | Page " + pageNumber + "/" + ((queueLength + 4) / 5) + " | Length: " + toTimestamp(queueTimeLength));
        embed.setColor(botColour);
        if (PlayerManager.getInstance().getThumbURL(track) != null)
            embed.setThumbnail(PlayerManager.getInstance().getThumbURL(track));
        if (!event.isSlash()) { //Incredibly hacky fix because @9382 doesn't want to implement all the backend just for this
            ((MessageReceivedEvent) event.getCoreEvent()).getMessage().replyEmbeds(embed.build()).queue(
                    message -> message.editMessageComponents().setActionRow(Button.secondary("backward", "◀"), Button.secondary("forward", "▶")).queue()
            );
        } else {
            ((SlashCommandInteractionEvent) event.getCoreEvent()).replyEmbeds(embed.build()).queue(
                    message -> message.editOriginalComponents().setActionRow(Button.secondary("backward", "◀"), Button.secondary("forward", "▶")).queue()
            );
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"queue", "q"};
    }

    @Override
    public Category getCategory() {
        return Category.Music;
    }

    @Override
    public String getOptions() {
        return "[Page]";
    }

    @Override
    public String getDescription() {
        return "Shows you the current queue.";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.INTEGER, "page-number", "Page number of the queue");
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}
