package my.lexonix.wordgen.gateway.discord;

import my.lexonix.wordgen.server.Player;
import my.lexonix.wordgen.utility.Logger;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BattleListener extends ListenerAdapter {
    private static final Logger log = new Logger("BattleListener");

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Player p = DiscordBot.getPlayer(event.getUser());
        switch (event.getName()) {

        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] buttonId = event.getComponentId().split("_");
        Player p = DiscordBot.getPlayer(event.getUser());
        if (buttonId[0].equals(p.getPlayerID())) {
            switch (buttonId[1]) {
                case "battlesend": {
                    String word = buttonId[2];
                    break;
                }  // TODO
            }
        }
    }
}
