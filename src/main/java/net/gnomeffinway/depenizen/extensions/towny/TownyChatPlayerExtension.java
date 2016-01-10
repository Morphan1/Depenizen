package net.gnomeffinway.depenizen.extensions.towny;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.channels.ChannelsHolder;
import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import net.gnomeffinway.depenizen.extensions.dObjectExtension;
import net.gnomeffinway.depenizen.support.Support;
import net.gnomeffinway.depenizen.support.plugins.TownyChatSupport;

import java.util.Map;

public class TownyChatPlayerExtension extends dObjectExtension {

    public static boolean describes(dObject pl) {
        return pl instanceof dPlayer;
    }

    public static TownyChatPlayerExtension getFrom(dObject object) {
        if (!describes(object)) {
            return null;
        }
        return new TownyChatPlayerExtension((dPlayer) object);
    }

    private TownyChatPlayerExtension(dPlayer player) {
        try {
            this.resident = TownyUniverse.getDataSource().getResident(player.getName());
            this.player = player;
        }
        catch (NotRegisteredException e) {
            dB.echoError("Referenced player is not registered with towny.");
        }
    }

    dPlayer player;
    Resident resident;
    Chat plugin = Support.getPlugin(TownyChatSupport.class);
    ChannelsHolder holder = plugin.getChannelsHandler();

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        if (attribute.startsWith("townychat")) {
            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <p@player.townychat.channels>
            // @returns dList(Element)
            // @description
            // Returns a list of all channels the player is in.
            // @plugin Depenizen, Towny
            // -->
            if (attribute.startsWith("channel")) {
                dList chans = new dList();
                for (Channel c : holder.getAllChannels().values()) {
                        chans.add(new Element(c.getName()).identify());
                }
                return chans.getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <p@player.townychat.muted_in[<channel name>]>
            // @returns Element(Boolean)
            // @description
            // Returns whether the player is muted in the specified channel.
            // @plugin Depenizen, Towny
            // -->
            else if (attribute.startsWith("muted_in") && attribute.hasContext(1)) {
                Channel c = holder.getChannel(attribute.getAttribute(1));
                if (c == null) {
                    return null;
                }
                return new Element(c.isMuted(player.getName())).getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <p@player.townychat.has_permission[<channel name>]>
            // @returns Element(Boolean)
            // @description
            // Returns whether the player has permissions to join the specified channel.
            // @plugin Depenizen, Towny
            // -->
            else if (attribute.startsWith("has_permission") && attribute.hasContext(1)) {
                Channel c = holder.getChannel(attribute.getContext(1));
                if (c == null) {
                    return null;
                }
                String perm = c.getPermission();
                if (perm == null || !plugin.getTowny().isPermissions()) {
                    return Element.TRUE.getAttribute(attribute.fulfill(1));
                }
                return new Element(TownyUniverse.getPermissionSource().has(player.getPlayerEntity(), perm))
                        .getAttribute(attribute.fulfill(1));
            }

            return null;
        }
        return null;
    }
}
