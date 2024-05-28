package io.github.racoondog.nbcracker;

import io.github.racoondog.nbcracker.commands.FindCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class NetherBedrockCrackerMod implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> FindCommand.register(dispatcher));
	}
}
