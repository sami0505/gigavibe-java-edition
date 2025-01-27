package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import Bots.CommandStateChecker.Check;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import static Bots.CommandEvent.localise;
import static Bots.Main.*;

public class CommandPitch extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DJ, Check.IS_IN_SAME_VC, Check.IS_PLAYING};
    }

    @Override
    public void execute(CommandEvent event) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        TimescalePcmAudioFilter timescale = (TimescalePcmAudioFilter) musicManager.filters.get(AudioFilters.Timescale); // this needs to be redefined many times due to how it works.

        if (event.getArgs().length == 1) {
            timescale.setPitch(1);
            event.replyEmbeds(createQuickEmbed("✅ **" + localise("Main.success") + "**", localise("CommandPitch.defaulted")));
            return;
        }

        float value = Float.parseFloat(String.format("%.3f %n", Float.parseFloat(event.getArgs()[1])));

        if (!(value <= 5.0 && value >= 0.25)) {
            event.replyEmbeds(createQuickError(localise("CommandPitch.range")));
            return;
        }

        timescale.setPitch(value);
        event.replyEmbeds(createQuickEmbed("✅ **" + localise("Main.success") + "**", String.format(localise("CommandPitch.changed"), value)));
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.NUMBER, "pitch", "The pitch of the song, supports decimals.", false);
    }

    @Override
    public String getOptions() {
        return "<Number>";
    }

    @Override
    public Category getCategory() {
        return Category.DJ;
    }

    @Override
    public String[] getNames() {
        return new String[]{"pitch"};
    }

    @Override
    public String getDescription() {
        return "Changes the pitch of the song.";
    }

    @Override
    public long getRatelimit() {
        return 2000;
    }
}
