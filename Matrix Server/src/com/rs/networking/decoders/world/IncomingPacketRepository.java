package com.rs.networking.decoders.world;

import java.util.HashMap;
import java.util.Map;
import com.rs.networking.decoders.world.impl.DebugPacket;
import com.rs.utilities.Utilities;

public class IncomingPacketRepository {

	private static final String BASE_DIR = IncomingPacketRepository.class.getPackage().toString().replace("package ", "");

	public static Map<Integer, IncomingPacket> PACKETS = new HashMap<>(); 

	private static final IncomingPacket DEBUG_PACKET = new DebugPacket();

	@SuppressWarnings("unchecked")
	public static HashMap<Integer, IncomingPacket> loadPackets() {
		HashMap<Integer, IncomingPacket> packets = new HashMap<>();
		try {
			Class<IncomingPacket>[] packetArray = (Class<IncomingPacket>[]) Utilities.getAllClasses(BASE_DIR + ".impl");
			for(Class<IncomingPacket> packet : packetArray) {
				if(packet.getCanonicalName() == null)
					continue;
				Class<IncomingPacket> packetClass = (Class<IncomingPacket>) Class.forName(packet.getCanonicalName());
					IncomingPacket incomingPacket = (IncomingPacket) packetClass.newInstance();
					if(incomingPacket.packetIds() == null)
						continue;
					if(incomingPacket.packetIds().length > 1) {
						for(int id : incomingPacket.packetIds()) {
							if(id != -1)
								packets.put(id, incomingPacket);
						}
						continue;
					}
					if(incomingPacket.packetIds()[0] != -1)
						packets.put(incomingPacket.packetIds()[0], incomingPacket);
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return packets;
	}

	public static IncomingPacket getIncomingPacket(int packetId) {
		IncomingPacket packet = PACKETS.get(packetId);
		if(packet != null)
			return packet;
		return DEBUG_PACKET;
	}

}