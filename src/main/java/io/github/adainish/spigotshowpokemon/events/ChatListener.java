package io.github.adainish.spigotshowpokemon.events;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Nature;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.pokemon.species.gender.Gender;
import com.pixelmonmod.pixelmon.api.pokemon.stats.*;
import com.pixelmonmod.pixelmon.api.pokemon.stats.extraStats.LakeTrioStats;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.attacks.specialAttacks.basic.HiddenPower;
import com.pixelmonmod.pixelmon.enums.EnumRibbonType;
import io.github.adainish.spigotshowpokemon.config.Config;
import io.github.adainish.spigotshowpokemon.util.StringTransformerUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatListener implements Listener {


    public int getPartySlot(String msg) {

        String newMSG = msg.replace("pokemon", "")
                .replace("poke", "")
                .replace("p", "")
                .replace("{", "")
                .replace("}", "");

        return Integer.parseInt(newMSG);
    }
    @EventHandler
    public void onChatEvent(AsyncPlayerChatEvent event) {
        Map<String, BaseComponent[]> placeholders = new HashMap<>();
        if (event.getMessage().contains("{pokemon")) {
            event.setCancelled(true);

            int start = event.getMessage().indexOf("{");
            int last = event.getMessage().indexOf("}");

            String pokemonMSG = event.getMessage().substring(start, last);
            Pokemon pokemon = null;
            PlayerPartyStorage storage = StorageProxy.getParty(event.getPlayer().getUniqueId());
            int selectedSlot = getPartySlot(pokemonMSG);
            selectedSlot -= 1;
            if (selectedSlot < 0)
                return;
            if (selectedSlot > 5)
                return;
            if (storage != null) {
                try {
                    pokemon = storage.get(selectedSlot);
                    if (pokemon == null)
                        return;

                    String key = "{pokemon" + selectedSlot + "}";
                    placeholders.put(key, buildPokemonStats(pokemon));
                } catch (Exception partyLength) {
                    partyLength.printStackTrace();
                }
            }
            if (pokemon == null)
                return;

            String newMessage = Config.getConfig().get().node("Messages", "Display")
                    .getString()
                    .replace("%p%", event.getPlayer().getName())
                    .replace("%pokemon%", pokemon.getSpecies().getName());
            TextComponent component = new TextComponent(StringTransformerUtil.formattedString(newMessage));
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, placeholders.values().stream().findFirst().get()));
            for (Player p : event.getRecipients()) {
                p.spigot().sendMessage(component);
            }
        }
    }

    public BaseComponent[] buildPokemonStats(Pokemon pokemon) {

        String heldItem;
        String customTexture;
        String displayName = pokemon.getSpecies().getName();
        String pokerus = pokemon.getPokerus() != null ? (pokemon.getPokerus().canInfect() ? "&d[PKRS] " : "&7&m[PKRS] ") : "";
        boolean isTrio = false;
        PermanentStats stats = pokemon.getStats();
        Gender gender = pokemon.getGender();
        Nature nature = pokemon.getMintNature() != null ? pokemon.getMintNature() : pokemon.getBaseNature();
        String natureColor = pokemon.getMintNature() != null ? "&3" : "";
        EVStore eVsStore = null;
        IVStore ivStore = null;
        boolean wasHyperTrained = false;
        EnumRibbonType displayRibbon = pokemon.getDisplayedRibbon();
        String[] ht = new String[]{"", "", "", "", "", ""};

        String formName = pokemon.getForm().getName();

        Species species = pokemon.getSpecies();
        if (PixelmonSpecies.MESPRIT.getValueUnsafe().equals(species) || PixelmonSpecies.AZELF.getValueUnsafe().equals(species) || PixelmonSpecies.UXIE.getValueUnsafe().equals(species)) {
            isTrio = true;
        }

        int ivSum = 0;
        int evSum = 0;
        Moveset moveset = pokemon.getMoveset();

        heldItem = !pokemon.getHeldItem().isEmpty() ? pokemon.getHeldItem().getDisplayName().getUnformattedComponentText() : "Nothing";

        if (stats != null) {
            eVsStore = pokemon.getEVs();
            ivStore = pokemon.getIVs();

            ivSum = ivStore.getStat(BattleStatsType.HP) + ivStore.getStat(BattleStatsType.ATTACK) + ivStore.getStat(BattleStatsType.DEFENSE) + ivStore.getStat(BattleStatsType.SPECIAL_ATTACK) + ivStore.getStat(BattleStatsType.SPECIAL_DEFENSE) + ivStore.getStat(BattleStatsType.SPEED);
            evSum = eVsStore.getStat(BattleStatsType.HP) + eVsStore.getStat(BattleStatsType.ATTACK) + eVsStore.getStat(BattleStatsType.DEFENSE) + eVsStore.getStat(BattleStatsType.SPECIAL_ATTACK) + eVsStore.getStat(BattleStatsType.SPECIAL_DEFENSE) + eVsStore.getStat(BattleStatsType.SPEED);

            BattleStatsType[] stat = new BattleStatsType[]{BattleStatsType.HP, BattleStatsType.ATTACK, BattleStatsType.DEFENSE, BattleStatsType.SPECIAL_ATTACK, BattleStatsType.SPECIAL_DEFENSE, BattleStatsType.SPEED};

            for (int i = 0; i < stat.length; ++i) {
                if (ivStore.isHyperTrained(stat[i])) {
                    ht[i] = "&3";
                    wasHyperTrained = true;
                }
            }
        }

        String pokeGender;
        if (gender.toString().equals("Female")) pokeGender = "&d" + gender.toString() + " \u2640";
        else if (gender.toString().equals("Male")) pokeGender = "&b" + gender.toString() + " \u2642";
        else pokeGender = "&8Genderless \u26A5";

        ArrayList<String> moves = new ArrayList<>();
        moves.add((moveset.get(0) == null) ? "&bNone" : "&b" + moveset.get(0).getActualMove().getAttackName());
        moves.add((moveset.get(1) == null) ? "&bNone" : "&b" + moveset.get(1).getActualMove().getAttackName());
        moves.add((moveset.get(2) == null) ? "&bNone" : "&b" + moveset.get(2).getActualMove().getAttackName());
        moves.add((moveset.get(3) == null) ? "&bNone" : "&b" + moveset.get(3).getActualMove().getAttackName());


        DecimalFormat df = new DecimalFormat("#0.##");
        int numEnchants = 0;
        try {
            if (pokemon.getExtraStats() != null && pokemon.getExtraStats() instanceof LakeTrioStats) {
                LakeTrioStats extra = (LakeTrioStats) pokemon.getExtraStats();
                numEnchants = PixelmonConfigProxy.getGeneral().getLakeTrioMaxEnchants() - extra.numEnchanted;
            }
        } catch (Exception extra) {
            // empty catch block
        }

        ChatColor nameColor = ChatColor.DARK_AQUA;
        String pokeName = "&3" + displayName;

        if (pokemon.isShiny() && !pokemon.isEgg()) {
            nameColor = ChatColor.GOLD;
            pokeName = "&6" + displayName;
        }
        if (PixelmonSpecies.isLegendary(pokemon.getSpecies())) {
            nameColor = ChatColor.DARK_PURPLE;
            pokeName = "&d" + displayName;
        }
        if (PixelmonSpecies.isLegendary(pokemon.getSpecies())) {
            nameColor = ChatColor.DARK_GREEN;
            pokeName = "&2" + displayName;
        }

        String pokeStats = pokerus + pokeName + " &7| &eLvl " + pokemon.getPokemonLevel() + " " + ((pokemon.isShiny()) ? "&7(&6Shiny&7)&r " : "") + "\n&r" +
                (pokemon.hasFlag("untradeable") ? "&4Untradeable" + "\n&r" : "") +
                (pokemon.hasFlag("unbreedable") ? "&4Unbreedable" + "\n&r" : "") +

                (displayRibbon != EnumRibbonType.NONE ? "&7Ribbon: &e" + displayRibbon.name() + "\n&r" : "") +

                (pokemon.hasGigantamaxFactor() ? "&cGigantamax Potential" + "\n&r" : "") +
                (pokemon.getDynamaxLevel() > 0 ? "&7Dynamax Level: &d" + pokemon.getDynamaxLevel() + "\n&r" : "") +

                (!formName.isEmpty() ? "&7Form: &e" + formName + "\n&r" : "") +
                (isTrio ? "&7Ruby Enchant: &e" + (numEnchants != 0 ? numEnchants + " Available" : "None Available") + "\n&r" : "") +
                (!pokemon.getHeldItem().isEmpty() ? "&7Held Item: &e" + heldItem + "\n&r" : "") +
                "&7Ability: &e" + pokemon.getAbility().getName() + ((pokemon.getForm().getAbilities().hasHiddenAbilities() && pokemon.getAbility().equals(pokemon.getForm().getAbilities().getHiddenAbilities()[0])) ? " &7(&6HA&7)&r" : "") + "\n&r" +
                "&7Nature: &e" + natureColor + nature.name() + " &7(&a+" + nature.getIncreasedStat() + " &7| &c-" + nature.getDecreasedStat() + "&7)" + "\n&r" +
                "&7Gender: " + pokeGender + "\n&r" +
                "&7Size: &e" + pokemon.getGrowth().name() + "\n&r" +
                "&7Happiness: &e" + pokemon.getFriendship() + "\n&r" +
                "&7Hidden Power: &e" + HiddenPower.getHiddenPowerType(pokemon.getOwnerPlayer(), pokemon, pokemon.getStats().getIVs(), "").getLocalizedName() + "\n&r" +
                "&7Caught Ball: &e" + pokemon.getBall().getLocalizedName() + "\n\n&r" +

                "&7IVs: &e" + ivSum + "&7/&e186 &7(&a" + df.format((int) (((double) ivSum / 186) * 100)) + "%&7) \n"
                + "&cHP: " + ht[0] + ivStore.getStat(BattleStatsType.HP) + " &7/ " + "&6Atk: " + ht[1] + ivStore.getStat(BattleStatsType.ATTACK) + " &7/ " + "&eDef: " + ht[2] + ivStore.getStat(BattleStatsType.DEFENSE) + "\n"
                + "&9SpA: " + ht[3] + ivStore.getStat(BattleStatsType.SPECIAL_ATTACK) + " &7/ " + "&aSpD: " + ht[4] + ivStore.getStat(BattleStatsType.SPECIAL_DEFENSE) + " &7/ " + "&dSpe: " + ht[5] + ivStore.getStat(BattleStatsType.SPEED) + "\n" +

                "&7EVs: &e" + evSum + "&7/&e510 &7(&a" + df.format((int) (((double) evSum / 510) * 100)) + "%&7) \n"
                + "&cHP: " + eVsStore.getStat(BattleStatsType.HP) + " &7/ " + "&6Atk: " + eVsStore.getStat(BattleStatsType.ATTACK) + " &7/ " + "&eDef: " + eVsStore.getStat(BattleStatsType.DEFENSE) + "\n"
                + "&9SpA: " + eVsStore.getStat(BattleStatsType.SPECIAL_ATTACK) + " &7/ " + "&aSpD: " + eVsStore.getStat(BattleStatsType.SPECIAL_DEFENSE) + " &7/ " + "&dSpe: " + eVsStore.getStat(BattleStatsType.SPEED) + "\n\n" +

                "&7Moves:\n" + moves.get(0) + " &7- " + moves.get(1) + "\n" + moves.get(2) + " &7- " + moves.get(3);
        if (pokemon.isEgg()) {
            return new ComponentBuilder("Pokemon Egg").color(nameColor).create();
        }
        else
            return new ComponentBuilder(StringTransformerUtil.formattedString(pokeStats)).color(nameColor).create();
    }
}
