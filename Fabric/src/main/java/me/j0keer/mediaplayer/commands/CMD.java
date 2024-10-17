package me.j0keer.mediaplayer.commands;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class CMD {
    public void sendMSG(ServerCommandSource source, boolean toOps, String... msg) {
        for (String m : msg) {
            source.sendFeedback(getSupplier(m), toOps);
        }
    }

    public void sendMSG(ServerCommandSource source, String... msg) {
        sendMSG(source, false, msg);
    }

    public Supplier<Text> getSupplier(String in) {
        return () -> Text.of(in);
    }
}
