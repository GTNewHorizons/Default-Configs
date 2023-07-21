package net.blay09.mods.defaultkeys;

import java.io.File;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.util.ChatComponentTranslation;

public class CommandDefaultOptions extends CommandBase {

    @Override
    public String getCommandName() {
        return "defaultconfigs";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/defaultconfigs (saveAll|saveKeys|saveOptions|createUpdateFile)";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
        if (args[0].equals("createUpdateFile")) {
            try {
                new File(Minecraft.getMinecraft().mcDataDir, "modpack-update").createNewFile();
            } catch (Exception e) {
                sender.addChatMessage(
                    new ChatComponentTranslation(
                        "defaultconfigs.cmd.createUpdateFile.failure",
                        e.getLocalizedMessage()));
                DefaultKeys.logger.error("Could not create modpack-update file", e);
                return;
            }
            sender.addChatMessage(new ChatComponentTranslation("defaultconfigs.cmd.createUpdateFile.success"));
            return;
        }
        boolean saveOptions = args[0].equals("saveAll") || args[0].equals("saveOptions");
        boolean saveKeys = args[0].equals("saveAll") || args[0].equals("saveKeys");
        if (saveKeys) {
            if (DefaultKeys.instance.saveDefaultMappings()) {
                sender.addChatMessage(new ChatComponentTranslation("defaultconfigs.cmd.saveKeys.success"));
                DefaultKeys.instance.reloadDefaultMappings();
            } else {
                sender.addChatMessage(new ChatComponentTranslation("defaultconfigs.cmd.saveKeys.failure"));
            }
        }
        if (saveOptions) {
            if (DefaultKeys.instance.saveDefaultOptions() && DefaultKeys.instance.saveDefaultOptionsOptiFine()) {
                sender.addChatMessage(new ChatComponentTranslation("defaultconfigs.cmd.saveOptions.success"));
            } else {
                sender.addChatMessage(new ChatComponentTranslation("defaultconfigs.cmd.saveOptions.failure"));
            }
        }
        if (!saveOptions && !saveKeys) {
            throw new WrongUsageException(getCommandUsage(sender));
        }
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length < 2) {
            return getListOfStringsMatchingLastWord(args, "saveAll", "saveKeys", "saveOptions", "createUpdateFile");
        }
        return super.addTabCompletionOptions(sender, args);
    }
}
