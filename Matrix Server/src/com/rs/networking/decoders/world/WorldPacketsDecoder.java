package com.rs.networking.decoders.world;

import com.rs.Settings;
import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.Session;
import com.rs.networking.decoders.Decoder;
import com.rs.networking.io.InputStream;
import com.rs.utilities.Utilities;

/**
 * @author Abysa/Dido#4821
 * 11/14/18
 */
public final class WorldPacketsDecoder extends Decoder {

	private static byte[] PACKET_SIZES = new byte[256];
	
	public final static int ACTION_BUTTON1_PACKET = 14;
	public final static int ACTION_BUTTON2_PACKET = 67;
	public final static int ACTION_BUTTON3_PACKET = 5;
	public final static int ACTION_BUTTON4_PACKET = 55;
	public final static int ACTION_BUTTON5_PACKET = 68;
	public final static int ACTION_BUTTON6_PACKET = 90;
	public final static int ACTION_BUTTON7_PACKET = 6;
	public final static int ACTION_BUTTON8_PACKET = 32;
	public final static int ACTION_BUTTON9_PACKET = 27;
	public final static int ACTION_BUTTON10_PACKET = 96;

	public static final int[] logicPacketIds = new int[] {
			8, 58, 57, 46, 17, 42, 20,
			50, 66, 31, 101, 34, 26, 76,
			59, 40, 23, 80, 37
	};

	
	static {
		PACKET_SIZES = loadPacketSizes();
	}

	public static byte[] loadPacketSizes() {
		byte[] PACKET_SIZES = new byte[256];
		for (int id = 0; id < 256; id++)
			PACKET_SIZES[id] = -4;
		PACKET_SIZES[64] = 8;
		PACKET_SIZES[18] = 8;
		PACKET_SIZES[25] = 8;
		PACKET_SIZES[41] = -1;
		
		PACKET_SIZES[14] = 3;
		PACKET_SIZES[46] = 3;
		PACKET_SIZES[87] = 6;
		PACKET_SIZES[47] = 9;
		PACKET_SIZES[57] = 3;
		PACKET_SIZES[67] = 3;
		PACKET_SIZES[91] = 8;
		PACKET_SIZES[24] = 7;
		PACKET_SIZES[73] = 16;
		PACKET_SIZES[40] = 11;
		PACKET_SIZES[36] = -1;
		PACKET_SIZES[74] = -1;
		PACKET_SIZES[31] = 3;
		PACKET_SIZES[54] = 6;
		PACKET_SIZES[12] = -1;
		PACKET_SIZES[23] = 1;
		PACKET_SIZES[9] = 3;
		PACKET_SIZES[17] = -1;
		PACKET_SIZES[44] = -1;
		PACKET_SIZES[88] = -1;
		PACKET_SIZES[42] = 17;
		PACKET_SIZES[49] = 3;
		PACKET_SIZES[21] = 15;
		PACKET_SIZES[59] = -1;
		PACKET_SIZES[37] = -1;
		PACKET_SIZES[6] = 8;
		PACKET_SIZES[55] = 7;
		PACKET_SIZES[69] = 9;
		PACKET_SIZES[26] = 16;
		PACKET_SIZES[39] = 12;
		PACKET_SIZES[71] = 4;
		PACKET_SIZES[22] = 2;
		PACKET_SIZES[32] = -1;
		PACKET_SIZES[79] = -1;
		PACKET_SIZES[89] = 4;
		PACKET_SIZES[90] = -1;
		PACKET_SIZES[15] = 4;
		PACKET_SIZES[72] = -2;
		PACKET_SIZES[20] = 8;
		PACKET_SIZES[92] = 3;
		PACKET_SIZES[82] = 3;
		PACKET_SIZES[28] = 3;
		PACKET_SIZES[81] = 8;
		PACKET_SIZES[7] = -1;
		PACKET_SIZES[4] = 8;
		PACKET_SIZES[60] = -1;
		PACKET_SIZES[13] = 2;
		PACKET_SIZES[52] = 8;
		PACKET_SIZES[65] = 11;
		PACKET_SIZES[85] = 2;
		PACKET_SIZES[86] = 7;
		PACKET_SIZES[78] = -1;
		PACKET_SIZES[83] = -1;
		PACKET_SIZES[27] = 7;
		PACKET_SIZES[2] = 9;
		PACKET_SIZES[93] = 1;
		PACKET_SIZES[70] = -1;
		PACKET_SIZES[1] = -1;
		PACKET_SIZES[8] = -1;
		PACKET_SIZES[11] = 9;
		PACKET_SIZES[0] = 9;
		PACKET_SIZES[51] = -1;
		PACKET_SIZES[5] = 4;
		PACKET_SIZES[45] = 7;
		PACKET_SIZES[75] = 4;
		PACKET_SIZES[53] = 3;
		PACKET_SIZES[33] = 0;
		PACKET_SIZES[50] = 3;
		PACKET_SIZES[76] = 9;
		PACKET_SIZES[80] = -1;
		PACKET_SIZES[77] = 3;
		PACKET_SIZES[68] = -1;
		PACKET_SIZES[43] = 3;
		PACKET_SIZES[30] = -1;
		PACKET_SIZES[19] = 3;
		PACKET_SIZES[16] = 0;
		PACKET_SIZES[34] = 4;
		PACKET_SIZES[48] = 0;
		PACKET_SIZES[56] = 0;
		PACKET_SIZES[58] = 2;
		PACKET_SIZES[10] = 8;
		PACKET_SIZES[35] = 7;
		PACKET_SIZES[84] = 6;
		PACKET_SIZES[66] = 3;
		PACKET_SIZES[61] = 8;
		PACKET_SIZES[29] = -1;
		PACKET_SIZES[62] = 3;
		PACKET_SIZES[3] = 4;
		PACKET_SIZES[63] = 4;
		PACKET_SIZES[73] = 16;
		PACKET_SIZES[38] = -1;
		return PACKET_SIZES;
	}

	private Player player;

	public WorldPacketsDecoder(Session session, Player player) {
		super(session);
		this.player = player;
	}

	@Override
	public void decode(InputStream stream) {
		while (stream.getRemaining() > 0 && session.getChannel().isConnected() && !player.hasFinished()) {
			int packetId = stream.readUnsignedByte();
			if (packetId >= PACKET_SIZES.length) {
				if (Settings.DEBUG)
					System.out.println("PacketId " + packetId + " has fake packet id.");
				break;
			}
			int length = PACKET_SIZES[packetId];
			if (length == -1)
				length = stream.readUnsignedByte();
			else if (length == -2)
				length = stream.readUnsignedShort();
			else if (length == -3)
				length = stream.readInt();
			else if (length == -4) {
				length = stream.getRemaining();
				if (Settings.DEBUG)
					System.out.println("Invalid size for PacketId " + packetId + ". Size guessed to be " + length);
			}
			if (length > stream.getRemaining()) {
				length = stream.getRemaining();
				if (Settings.DEBUG)
					System.out.println("PacketId " + packetId + " has fake size. - expected size " + length);
				

			}
			 
			int startOffset = stream.getOffset();
			requestProcess(false, packetId, length, stream);
			stream.setOffset(startOffset + length);
		}
	}
	
	/**
	 * @param packetId
	 * @param stream
	 * @param packetSize
	 */
	public void requestProcess(boolean processLogic, int packetId, int packetSize, InputStream stream) {
		player.setPacketsDecoderPing(Utilities.currentTimeMillis());
		if (isLogicPacket(packetId) && !processLogic) {
			player.addLogicPacketToQueue(new LogicPacket(packetId, packetSize, stream));
		} else {
			IncomingPacketRepository.getIncomingPacket(packetId).processPacket(player, stream, packetId);
		}
	}
	
	public static boolean isLogicPacket(int packetId) {
		for (int i : logicPacketIds) {
			if (i == packetId)
				return true;
		}
		return false;
	}

	public Player getPlayer() {
		return player;
	}

}