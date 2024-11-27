package me.ByteVexer.tablist_logger.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Tablist_loggerClient implements ClientModInitializer {
    private static final Path PLAYER_LIST_FILE = Path.of("player_list.txt");
    private boolean running = false;
    public static final Logger LOGGER = LoggerFactory.getLogger("tablist_logger");

    @Override
    public void onInitializeClient() {
        LOGGER.info("Tablist_Logger has started up!");
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> startUpdatingPlayerList());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> stopUpdatingPlayerList());
    }

    private void startUpdatingPlayerList() {
        running = true;
        new Thread(() -> {
            while (running) {
                try {
                    updatePlayerListFile();
                    Thread.sleep(1000); // Update every second
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void stopUpdatingPlayerList() {
        running = false;
    }

    private void updatePlayerListFile() throws IOException {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.getNetworkHandler() != null) {
            List<String> playerNames = client.getNetworkHandler().getPlayerList()
                    .stream()
                    .map(player -> player.getProfile().getName())
                    .toList();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(PLAYER_LIST_FILE.toFile()))) {
                for (String name : playerNames) {
                    writer.write(name);
                    writer.newLine();
                }
            }
        }
    }
}

