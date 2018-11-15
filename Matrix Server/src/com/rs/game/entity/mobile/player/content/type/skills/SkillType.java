/**
 * 
 */
package com.rs.game.entity.mobile.player.content.type.skills;

/**
 * @author Abysa/Dido#4821
 * Dec 2, 2017 | 4:42:21 PM
 */
public enum SkillType {
	SHARED(-1),
	ATTACK(0),
	DEFENCE(1, 4),
	STRENGTH(2, 1),
	HITPOINTS(3, 5),
	RANGE(4, 2),
	PRAYER(5, 6),
	MAGIC(6, 3),
	COOKING(7, 15),
	WOODCUTTING(8, 17),
	FLETCHING(9),
	FISHING(10, 14),
	FIREMAKING(11, 16),
	CRAFTING(12, 10),
	SMITHING(13),
	MINING(14, 12),
	HERBLORE(15, 8),
	AGILITY(16, 7),
	THIEVING(17, 9),
	SLAYER(18, 19),
	FARMING(19, 20),
	RUNECRAFTING(20, 11),
	CONSTRUCTION(22, 21),
	HUNTER(21, 22),
	SUMMONING(23),
	DUNGEONEERING(24);
	
	private final int skillId;
	private final int counterId;
	
	private SkillType(int... skillId) {
		this.skillId = skillId[0];
		if(skillId.length > 1)
			this.counterId = skillId[1];
		else 
			this.counterId = skillId[0];
	}

	public int getSkillId() {
		return skillId;
	}

	public int getCounterId() {
		return counterId;
	}
	
	public static SkillType getTypeById(Integer id) {
		for(SkillType type : SkillType.values()) {
			if(id.equals(type.getSkillId()))
				return type;
		}
		return null;
	}

}
