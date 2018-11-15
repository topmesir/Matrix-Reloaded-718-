package com.rs.game.entity.mobile.player.content.type.skills;

import static com.rs.game.entity.mobile.player.content.type.skills.SkillType.*;

import java.util.HashMap;

import com.rs.game.entity.mobile.player.content.AbstractContent;

public final class Skills extends AbstractContent {

	private static final long serialVersionUID = -7086829989489745985L;
	
	public static final String[] SKILL_NAME = { "Attack", "Defence", "Strength", "Constitution", "Ranged", "Prayer",
			"Magic", "Cooking", "Woodcutting", "Fletching", "Fishing", "Firemaking", "Crafting", "Smithing", "Mining",
			"Herblore", "Agility", "Thieving", "Slayer", "Farming", "Runecrafting", "Hunter", "Construction",
			"Summoning", "Dungeoneering" };

	private HashMap<SkillType, SkillContainer> skills;
	private boolean xpDisplay, xpPopup;

	public Skills() {
		if(skills == null) {
			this.skills = new HashMap<>();
			resetSkills();
		}
	}
	
	public void sendInterfaces() {
		if (xpDisplay)
			player.getInterfaceManager().sendXPDisplay();
		if (xpPopup)
			player.getInterfaceManager().sendXPPopup();
	}

	public int getLevel(SkillType skill) {
		return getSkill(skill).getLevel();
	}

	public int getLevelForXp(SkillType skill) {
		double exp = getSkill(skill).getExperience();
		int points = 0;
		int output = 0;
		for (int lvl = 1; lvl <= (skill == DUNGEONEERING ? 120 : 99); lvl++) {
			points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0));
			output = (int) Math.floor(points / 4);
			if ((output - 1) >= exp) {
				return lvl;
			}
		}
		return skill == DUNGEONEERING ? 120 : 99;
	}
	
	public void addXp(SkillType type, double xp) {
		skills.get(type).addExperience(xp);
	}
	
	public int getLevel(int skillId) {
		return skills.get(SkillType.getTypeById(skillId)).getLevel();
	}
	
	public static int getLevelForXp(SkillType skill, int xp) {
		double exp = xp;
		int points = 0;
		int output = 0;
		for (int lvl = 1; lvl <= (skill == DUNGEONEERING ? 120 : 99); lvl++) {
			points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0));
			output = (int) Math.floor(points / 4);
			if ((output - 1) >= exp) {
				return lvl;
			}
		}
		return skill == DUNGEONEERING ? 120 : 99;
	}
	
	public int getCombatLevel() {
		int attack = getLevelForXp(SkillType.ATTACK);
		int defence = getLevelForXp(SkillType.DEFENCE);
		int strength = getLevelForXp(SkillType.STRENGTH);
		int hp = getLevelForXp(SkillType.HITPOINTS);
		int prayer = getLevelForXp(SkillType.PRAYER);
		int ranged = getLevelForXp(SkillType.RANGE);
		int magic = getLevelForXp(SkillType.MAGIC);
		int combatLevel = 3;
		combatLevel = (int) ((defence + hp + Math.floor(prayer / 2)) * 0.25) + 1;
		double melee = (attack + strength) * 0.325;
		double ranger = Math.floor(ranged * 1.5) * 0.325;
		double mage = Math.floor(magic * 1.5) * 0.325;
		if (melee >= ranger && melee >= mage) {
			combatLevel += melee;
		} else if (ranger >= melee && ranger >= mage) {
			combatLevel += ranger;
		} else if (mage >= melee && mage >= ranger) {
			combatLevel += mage;
		}
		return combatLevel;
	}
	
	public int getCombatLevelWithSummoning() {
		return getCombatLevel() + getSummoningCombatLevel();
	}
	
	public double getXp(SkillType skill) {
		return getSkill(skill).getExperience();
	}

	public int getSummoningCombatLevel() {
		return getLevelForXp(SkillType.SUMMONING) / 8;
	}
	
	public int drainLevel(SkillType type, int level) {
		skills.get(type).setLevel(skills.get(type).getLevel() - level);
		return skills.get(type).getLevel();
	}
	
	public int drainLevel(int skillId, int level) {
		SkillType type = SkillType.getTypeById(skillId);
		skills.get(type).setLevel(skills.get(type).getLevel() - level);
		return skills.get(type).getLevel();
	}
	
	public void setLevelForXp(SkillType type, int xp) {
		skills.get(type).setLevel(getLevelForXp(type, xp));
	}
	
	public void setLevelBoost(SkillType type, int level) {
		skills.get(type).setLevel(level);
	}

	public SkillContainer getSkill(SkillType type) {
		return skills.get(type);
	}

	public void restoreSkills() {
		for (SkillType type : SkillType.values()) {
			getSkill(type).setLevel(getLevelForXp(type));
			refresh(type);
		}
	}

	public void refresh(SkillType skill) {
		player.getPackets().sendSkillLevel(skill);
	}

	public void refreshLevel(SkillType skill) {
		getSkill(skill).setLevel(getLevelForXp(skill));
	}

	public void resetSkills() {
		for(SkillType skill : values()) {
			int startXp = skill == HERBLORE ? 250 : skill == HITPOINTS ? 1154 : 0;
			int startLevel = skill == HERBLORE ? 3 : skill == HITPOINTS ? 10 : 0;
			skills.put(skill, new SkillContainer(startLevel, startXp));
		}
	}
}
