/**
 * 
 */
package com.rs.game.entity.mobile.player.content.type.skills;

/**
 * @author Abysa/Dido#4821
 * Dec 2, 2017 | 4:54:58 PM
 */
public class SkillContainer {
	
	private int level;
	private double experience;
	
	public SkillContainer(int level, int experience) {
		this.setLevel(level);
		this.setExperience(experience);
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double getExperience() {
		return experience;
	}

	public void setExperience(double experience) {
		this.experience = experience;
	}
	
	public void addExperience(double experience) {
		this.experience += experience;
	}
}
