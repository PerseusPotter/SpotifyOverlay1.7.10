package com.perseuspotter.spotifyoverlay;

import com.perseuspotter.lib.textgui.TextGui;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

import java.util.Collections;
import java.util.List;

public class SpotifyCommand extends CommandBase {
    @Override
    public String getCommandName() { return "spotify"; }
    @Override
    public List<String> getCommandAliases() { return Collections.emptyList(); }

    private final String[] params = { "move", "enabled", "refreshDelay", "prefix", "songFormat", "hideWhenNotOpen", "scrollSpeed", "freezeTime", "maxLength", "alternate", "font" };
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return joinNiceString(params);
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, params);
        if (args.length == 2) {
            if (args[0].equals("font")) return getListOfStringsFromIterableMatchingLastWord(args, TextGui.FONT_NAMES.keySet());
        }
        return null;
    }

    private interface Property {
        String getName();
        String getValue(Config c);
        void setValue(String s);
    }
    private final Config DEFAULT_CONFIG = new Config();
    private final Property[] props = {
        new Property() {
            public String getName() { return "enabled"; }
            public String getValue(Config c) { return Boolean.toString(c.enabled); }
            public void setValue(String s) { Config.getInstance().enabled = Boolean.parseBoolean(s); }
        },
        new Property() {
            public String getName() { return "refreshDelay"; }
            public String getValue(Config c) { return Integer.toString(c.refreshDelay); }
            public void setValue(String s) { Config.getInstance().refreshDelay = Integer.parseInt(s); }
        },
        new Property() {
            public String getName() { return "prefix"; }
            public String getValue(Config c) { return c.prefix; }
            public void setValue(String s) { Config.getInstance().prefix = s; }
        },
        new Property() {
            public String getName() { return "songFormat"; }
            public String getValue(Config c) { return c.songFormat; }
            public void setValue(String s) { Config.getInstance().songFormat = s; }
        },
        new Property() {
            public String getName() { return "hideWhenNotOpen"; }
            public String getValue(Config c) { return Boolean.toString(c.hideWhenNotOpen); }
            public void setValue(String s) { Config.getInstance().hideWhenNotOpen = Boolean.parseBoolean(s); }
        },
        new Property() {
            public String getName() { return "scrollSpeed"; }
            public String getValue(Config c) { return Integer.toString(c.scrollSpeed); }
            public void setValue(String s) { Config.getInstance().scrollSpeed = Integer.parseInt(s); }
        },
        new Property() {
            public String getName() { return "freezeTime"; }
            public String getValue(Config c) { return Integer.toString(c.freezeTime); }
            public void setValue(String s) { Config.getInstance().freezeTime = Integer.parseInt(s); }
        },
        new Property() {
            public String getName() { return "maxLength"; }
            public String getValue(Config c) { return Integer.toString(c.maxLength); }
            public void setValue(String s) { Config.getInstance().maxLength = Integer.parseInt(s); }
        },
        new Property() {
            public String getName() { return "alternate"; }
            public String getValue(Config c) { return Boolean.toString(c.alternate); }
            public void setValue(String s) { Config.getInstance().alternate = Boolean.parseBoolean(s); }
        },
        new Property() {
            public String getName() { return "font"; }
            public String getValue(Config c) { return c.font; }
            public void setValue(String s) {
                if (!TextGui.FONT_NAMES.containsKey(s)) throw new IllegalArgumentException("bad font name");
                Config.getInstance().font = s;
            }
        }
    };

    private void printProperty(Property prop) {
        IChatComponent comp = new ChatComponentText("")
            .appendSibling(
                new ChatComponentText("[ EDIT ]")
                    .setChatStyle(new ChatStyle()
                        .setColor(EnumChatFormatting.GREEN)
                        .setChatClickEvent(new ClickEvent(
                            ClickEvent.Action.SUGGEST_COMMAND,
                            "/" + getCommandName() + " " + prop.getName() + " " + prop.getValue(Config.getInstance())
                        ))
                    )
            )
            .appendText(" ")
            .appendSibling(
                new ChatComponentText("[ RESET ]")
                    .setChatStyle(new ChatStyle()
                        .setColor(EnumChatFormatting.RED)
                        .setChatClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/" + getCommandName() + " " + prop.getName() + " " + prop.getValue(DEFAULT_CONFIG)
                        ))
                    )
            )
            .appendText(" ")
            .appendSibling(
                new ChatComponentText(prop.getName() + ": ")
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE))
            )
            .appendSibling(
                new ChatComponentText(prop.getValue(Config.getInstance()))
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA))
            )
            .appendSibling(
                new ChatComponentText(" (Default Value: ")
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY))
            )
            .appendSibling(
                new ChatComponentText(prop.getValue(DEFAULT_CONFIG))
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.BLUE))
            )
            .appendSibling(
                new ChatComponentText(")")
                    .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY))
            );

        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(comp, 7329 + prop.hashCode());
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(
                new ChatComponentText("[ MOVE ]")
                    .setChatStyle(new ChatStyle()
                        .setColor(EnumChatFormatting.LIGHT_PURPLE)
                        .setChatClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/" + getCommandName() + " move"
                        ))
                    )
            );
            for (Property p : props) printProperty(p);
            return;
        }

        if (args[0].equals("move")) {
            SpotifyTextGui.INSTANCE.edit();
            return;
        }

        Property prop = null;
        for (Property p : props) {
            if (p.getName().matches(args[0])) {
                prop = p;
                break;
            }
        }
        if (prop == null) throw new WrongUsageException(getCommandUsage(sender));

        if (args.length == 1) {
            printProperty(prop);
            return;
        }

        try {
            String originalValue = prop.getValue(Config.getInstance());
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) sb.append(' ');
                sb.append(args[i]);
            }
            prop.setValue(sb.toString());
            String newValue = prop.getValue(Config.getInstance());
            // if (originalValue.equals(newValue)) return;

            IChatComponent comp = new ChatComponentText("")
                .appendSibling(
                    new ChatComponentText("Set ")
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE))
                )
                .appendSibling(
                    new ChatComponentText(prop.getName())
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GREEN))
                )
                .appendSibling(
                    new ChatComponentText(" to ")
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.WHITE))
                )
                .appendSibling(
                    new ChatComponentText(newValue)
                        .setChatStyle(new ChatStyle().setColor(EnumChatFormatting.AQUA))
                )
                .appendText(" ")
                .appendSibling(
                    new ChatComponentText("[ REVERT ]")
                        .setChatStyle(new ChatStyle()
                            .setColor(EnumChatFormatting.GRAY)
                            .setChatClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/" + getCommandName() + " " + prop.getName() + " " + originalValue
                            )
                        ))
                );

            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(comp);

            printProperty(prop);
        } catch (Exception e) {
            throw new CommandException("failed to parse input :" + e.getMessage());
        }
    }
}
