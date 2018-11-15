package com.rs.networking.decoders;

import com.rs.Settings;
import com.rs.engine.cache.Cache;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.world.World;
import com.rs.networking.Session;
import com.rs.networking.io.InputStream;
import com.rs.utilities.AntiFlood;
import com.rs.utilities.Encrypt;
import com.rs.utilities.IsaacKeyPair;
import com.rs.utilities.Logger;
import com.rs.utilities.SerializableFilesManager;
import com.rs.utilities.Utilities;

public final class LoginPacketsDecoder extends Decoder {

	public LoginPacketsDecoder(Session session) {
		super(session);
	}

	@Override
	public void decode(InputStream stream) {
		session.setDecoder(-1);
		int packetId = stream.readUnsignedByte();
		if (World.exiting_start != 0) {
			session.getLoginPackets().sendClientPacket(14);
			return;
		}
		int packetSize = stream.readUnsignedShort();
		if (packetSize != stream.getRemaining()) {
			session.getChannel().close();
			return;
		}
		if (stream.readInt() != Settings.MAJOR_BUILD) {
			session.getLoginPackets().sendClientPacket(6);
			return;
		}
		if (packetId == 16 || packetId == 18) {
			decodeWorldLogin(stream);
		} else {
			if (Settings.DEBUG) {
				Logger.log(this, "PacketId " + packetId);
			}
			session.getChannel().close();
		}
	}

	@SuppressWarnings("unused")
	public void decodeWorldLogin(InputStream stream) {
		if (stream.readInt() != Settings.MINOR_BUILD) {
			session.getLoginPackets().sendClientPacket(6);
			return;
		}
		boolean unknownEquals14 = stream.readUnsignedByte() == 1;
		int rsaBlockSize = stream.readUnsignedShort();
		if (rsaBlockSize > stream.getRemaining()) {
			session.getLoginPackets().sendClientPacket(10);
			return;
		}
		byte[] data = new byte[rsaBlockSize];
		stream.readBytes(data, 0, rsaBlockSize);
		InputStream rsaStream = new InputStream(Utilities.cryptRSA(data, Settings.GAME_SERVER_PRIVATE_EXPONENT, Settings.GAME_SERVER_PRIVATE_MODULUS));
		if (rsaStream.readUnsignedByte() != 10) {
			session.getLoginPackets().sendClientPacket(10);
			return;
		}
		int[] isaacKeys = new int[4];
		for (int i = 0; i < isaacKeys.length; i++)
			isaacKeys[i] = rsaStream.readInt();
		if (rsaStream.readLong() != 0L) {
			session.getLoginPackets().sendClientPacket(10);
			return;
		}
		String password = rsaStream.readString();
		if (password.length() > 30 || password.length() < 3) {
			session.getLoginPackets().sendClientPacket(3);
			return;
		}
		password = Encrypt.encryptSHA1(password);
		String unknown = Utilities.longToString(rsaStream.readLong());
		rsaStream.readLong();
		rsaStream.readLong();
		stream.decodeXTEA(isaacKeys, stream.getOffset(), stream.getLength());
		boolean stringUsername = stream.readUnsignedByte() == 1;
		String username = Utilities.formatPlayerNameForProtocol(stringUsername ? stream.readString() : Utilities.longToString(stream.readLong()));
		int displayMode = stream.readUnsignedByte();
		int screenWidth = stream.readUnsignedShort();
		int screenHeight = stream.readUnsignedShort();
		int unknown2 = stream.readUnsignedByte();
		stream.skip(24);
		String settings = stream.readString();
		int affid = stream.readInt();
		stream.skip(stream.readUnsignedByte());
		int unknown3 = stream.readInt();
		long userFlow = stream.readLong();
		boolean hasAditionalInformation = stream.readUnsignedByte() == 1;
		if (hasAditionalInformation) {
			stream.readString();
		}
		boolean hasJagtheora = stream.readUnsignedByte() == 1;
		boolean js = stream.readUnsignedByte() == 1;
		boolean hc = stream.readUnsignedByte() == 1;
		int unknown4 = stream.readByte();
		int unknown5 = stream.readInt();
		String unknown6 = stream.readString();
		boolean unknown7 = stream.readUnsignedByte() == 1;
		for (int index = 0; index < Cache.STORE.getIndexes().length; index++) {
			int crc = Cache.STORE.getIndexes()[index] == null ? -1011863738 : Cache.STORE.getIndexes()[index].getCRC();
			int receivedCRC = stream.readInt();
			if (crc != receivedCRC && index < 32) {
				session.getLoginPackets().sendClientPacket(6);
				return;
			}
		}
		if (Utilities.invalidAccountName(username)) {
			session.getLoginPackets().sendClientPacket(3);
			return;
		}
		if (World.getPlayers().size() >= Settings.PLAYERS_LIMIT - 10) {
			session.getLoginPackets().sendClientPacket(7);
			return;
		}
		if (World.containsPlayer(username)) {
			session.getLoginPackets().sendClientPacket(5);
			return;
		}
		if (AntiFlood.getSessionsIP(session.getIP()) > 3) {
			session.getLoginPackets().sendClientPacket(9);
			return;
		}
		Player player;
		if (!SerializableFilesManager.containsPlayer(username)) {
			player = new Player(password);
		} else {
			player = SerializableFilesManager.loadPlayer(username);
			if (player == null) {
				session.getLoginPackets().sendClientPacket(20);
				return;
			}
			if (!SerializableFilesManager.createBackup(username)) {
				session.getLoginPackets().sendClientPacket(20);
				return;
			}
			if (!password.equals(player.getPassword())) {
				session.getLoginPackets().sendClientPacket(3);
				return;
			}
		}
		if (player.isPermBanned() || player.getBanned() > Utilities.currentTimeMillis()) {
			session.getLoginPackets().sendClientPacket(4);
			return;
		}
		player.init(session, username, displayMode, screenWidth, screenHeight, new IsaacKeyPair(isaacKeys));
		session.getLoginPackets().sendLoginDetails(player);
		session.setDecoder(3, player);
		session.setEncoder(2, player);
		player.start();
	}
}
