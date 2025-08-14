package my.lexonix.wordgen.gateway.discord;

import my.lexonix.wordgen.server.Player;
import my.lexonix.wordgen.utility.Logger;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BattleListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Logger.write("[WordsListener] Использована слэш-команда " + event.getName());
        Player p = DiscordBot.getPlayer(event.getUser());
        switch (event.getName()) {

        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] buttonId = event.getComponentId().split("_");
        Player p = DiscordBot.getPlayer(event.getUser());
        if (buttonId[0].equals(p.getPlayerID())) {
            Logger.write("[WordsListener] Пользователем " + p.getPlayerID() +
                    " использована кнопка " + event.getComponentId());
            switch (buttonId[1]) {
                case "battlesend": {
                    String word = buttonId[2];
                    break;
                }  // TODO
            }
        }
    }
}
