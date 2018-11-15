package com.rs.game.entity.mobile.player.dialogue.impl;

import com.rs.game.entity.mobile.Graphics;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.Rank;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.game.entity.mobile.player.content.type.skills.SkillType;
import com.rs.game.entity.mobile.player.content.type.skills.Skills;
import com.rs.game.entity.mobile.player.dialogue.Dialogue;
import com.rs.game.world.World;

public final class LevelUp extends Dialogue {

	/*
	 * public static final int[] SKILL_ICON = { 100000000, 400000000, 200000000,
	 * 450000000, 250000000, 500000000, 300000000, 1100000000, 1250000000,
	 * 1300000000, 1050000000, 1200000000, 800000000, 1000000000, 900000000,
	 * 650000000, 600000000, 700000000, 1400000000, 1450000000, 850000000,
	 * 1500000000, 1600000000, 1650000000, 1700000000 };
	 */

	public static final int[] SKILL_LEVEL_UP_MUSIC_EFFECTS = { 37, 37, 37, 37,
		37, -1, 37, -1, 39, -1, -1, -1, -1, -1, 53, -1, -1, -1, -1, -1, -1,
		-1, -1, 300, 417 };

	private int skill;

	@Override
	public void start() {
		skill = (Integer) parameters[0];
		SkillType skillT = SkillType.getTypeById(skill);
		int level = player.getContent().get(ContentType.SKILLS).getLevelForXp(skillT);
		player.getTemporaryAttributtes().put("leveledUp", skill);
		player.getTemporaryAttributtes().put("leveledUp[" + skill + "]",
				Boolean.TRUE);
		player.setNextGraphics(new Graphics(199));
		if (level == 99 || level == 120)
			player.setNextGraphics(new Graphics(1765));
		player.getInterfaceManager().sendChatBoxInterface(740);
		String name = Skills.SKILL_NAME[skill];
		player.getPackets().sendIComponentText(
				740,
				0,
				"Congratulations, you have just advanced a"
						+ (name.startsWith("A") ? "n" : "") + " " + name
						+ " level!");
		player.getPackets().sendIComponentText(740, 1,
				"You have now reached level " + level + ".");
		player.getPackets().sendGameMessage(
				"You've just advanced a" + (name.startsWith("A") ? "n" : "")
				+ " " + name + " level! You have reached level "
				+ level + ".");
		player.getPackets().sendConfigByFile(4757, getIconValue(skillT));
		switchFlash(player, skillT, true);
		int musicEffect = SKILL_LEVEL_UP_MUSIC_EFFECTS[skill];
		if (musicEffect != -1)
			player.getPackets().sendMusicEffect(musicEffect);
		if (player.getRank() != Rank.ADMIN && (level == 99 || level == 120) && skill > 6 && skill != 23) {
			sendNews(player, skill, level);
		}
	}
	
	public static void sendNews(Player player, int skill, int level) {
		boolean reachedAll = true;
		for (SkillType skillT : SkillType.values()) {
			if (player.getContent().get(ContentType.SKILLS).getLevelForXp(skillT) < 99) {
				reachedAll = false;
				break;
			}
		}
		if(!reachedAll) {
			World.sendWorldMessage("<img=6><col=ff8c38>News: "+player.getDisplayName()+" has achieved "+level+" "+Skills.SKILL_NAME[skill]+".", false);
			return;
		}
		if(player.getContent().get(ContentType.SKILLS).getLevelForXp(SkillType.DUNGEONEERING) == 120) {
			World.sendWorldMessage("<img=7><col=ff0000>News: "+player.getDisplayName()+" has been awarded the Completionist Cape!", false);
			return;
		}
		World.sendWorldMessage("<img=7><col=ff0000>News: "+player.getDisplayName()+" has just achieved at least level 99 in all skills!", false);
	}

	public static int getIconValue(SkillType skill) {
		if (skill == SkillType.ATTACK)
			return 1;
		if (skill == SkillType.STRENGTH)
			return 2;
		if (skill == SkillType.RANGE)
			return 3;
		if (skill == SkillType.MAGIC)
			return 4;
		if (skill == SkillType.DEFENCE)
			return 5;
		if (skill == SkillType.HITPOINTS)
			return 6;
		if (skill == SkillType.PRAYER)
			return 7;
		if (skill == SkillType.AGILITY)
			return 8;
		if (skill == SkillType.HERBLORE)
			return 9;
		if (skill == SkillType.THIEVING)
			return 10;
		if (skill == SkillType.CRAFTING)
			return 11;
		if (skill == SkillType.RUNECRAFTING)
			return 12;
		if (skill == SkillType.MINING)
			return 13;
		if (skill == SkillType.SMITHING)
			return 14;
		if (skill == SkillType.FISHING)
			return 15;
		if (skill == SkillType.COOKING)
			return 16;
		if (skill == SkillType.FIREMAKING)
			return 17;
		if (skill == SkillType.WOODCUTTING)
			return 18;
		if (skill == SkillType.FLETCHING)
			return 19;
		if (skill == SkillType.SLAYER)
			return 20;
		if (skill == SkillType.FARMING)
			return 21;
		if (skill == SkillType.CONSTRUCTION)
			return 22;
		if (skill == SkillType.SLAYER)
			return 23;
		if (skill == SkillType.SUMMONING)
			return 24;
		return 25;
	}

	public static void switchFlash(Player player, SkillType skill, boolean on) {
		int id;
		if (skill == SkillType.ATTACK)
			id = 4732;
		else if (skill == SkillType.STRENGTH)
			id = 4733;
		else if (skill == SkillType.DEFENCE)
			id = 4734;
		else if (skill == SkillType.RANGE)
			id = 4735;
		else if (skill == SkillType.PRAYER)
			id = 4736;
		else if (skill == SkillType.MAGIC)
			id = 4737;
		else if (skill == SkillType.HITPOINTS)
			id = 4738;
		else if (skill == SkillType.AGILITY)
			id = 4739;
		else if (skill == SkillType.HERBLORE)
			id = 4740;
		else if (skill == SkillType.THIEVING)
			id = 4741;
		else if (skill == SkillType.CRAFTING)
			id = 4742;
		else if (skill == SkillType.FLETCHING)
			id = 4743;
		else if (skill == SkillType.MINING)
			id = 4744;
		else if (skill == SkillType.SMITHING)
			id = 4745;
		else if (skill == SkillType.FISHING)
			id = 4746;
		else if (skill == SkillType.COOKING)
			id = 4747;
		else if (skill == SkillType.FIREMAKING)
			id = 4748;
		else if (skill == SkillType.WOODCUTTING)
			id = 4749;
		else if (skill == SkillType.RUNECRAFTING)
			id = 4750;
		else if (skill == SkillType.SLAYER)
			id = 4751;
		else if (skill == SkillType.FARMING)
			id = 4752;
		else if (skill == SkillType.CONSTRUCTION)
			id = 4753;
		else if (skill == SkillType.HUNTER)
			id = 4754;
		else if (skill == SkillType.SUMMONING)
			id = 4755;
		else
			id = 7756;
		player.getPackets().sendConfigByFile(id, on ? 1 : 0);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void finish() {
		// player.getPackets().sendConfig(1179, SKILL_ICON[skill]); //removes
		// random flash
	}
}