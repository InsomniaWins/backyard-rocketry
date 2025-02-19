package wins.insomnia.backyardrocketry.network;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import org.joml.Vector3d;
import org.joml.Vector3f;
import wins.insomnia.backyardrocketry.controller.ClientController;
import wins.insomnia.backyardrocketry.controller.ServerController;
import wins.insomnia.backyardrocketry.item.BlockItem;
import wins.insomnia.backyardrocketry.item.Item;
import wins.insomnia.backyardrocketry.item.ItemStack;
import wins.insomnia.backyardrocketry.network.entity.PacketRemoveEntity;
import wins.insomnia.backyardrocketry.network.entity.PacketUpdateEntityTransform;
import wins.insomnia.backyardrocketry.network.entity.player.*;
import wins.insomnia.backyardrocketry.network.world.PacketLoadChunk;
import wins.insomnia.backyardrocketry.network.world.PacketUpdateBlock;
import wins.insomnia.backyardrocketry.util.Transform;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public abstract class Packet {

	public enum SenderType {
		SERVER,
		CLIENT
	}

	public static final List<Class<?>> CLASS_REGISTRATION_LIST = Arrays.asList(
			TestPacket.class,
			PacketLoadChunk.class,
			PacketPlayerTransform.class,
			PacketPlayerJump.class,
			PacketPlayerBreakBlock.class,
			PacketPlayerPlaceBlock.class,
			PacketPlayerPunchEntity.class,
			PacketUpdateBlock.class,
			PacketPlayerMovementInputs.class,
			PacketDropItem.class,
			PacketUpdateEntityTransform.class,
			PacketRemoveEntity.class,
			String.class,
			Transform.class,
			Vector3f.class,
			Vector3d.class,
			boolean[].class,
			byte[].class,
			byte[][].class
	);



	public static void registerClasses(Kryo kryo) {

		for (Class<?> packetClass : CLASS_REGISTRATION_LIST) {

			kryo.register(packetClass);

		}

	}



	public void sendToServer(boolean reliable) {
		ClientController.send(this, reliable);
	}

	public void sendToClient(int id, boolean reliable) {
		ServerController.send(id, this, reliable);
	}





	// called on main thread
	public abstract void received(SenderType senderType, Connection connection);



}
