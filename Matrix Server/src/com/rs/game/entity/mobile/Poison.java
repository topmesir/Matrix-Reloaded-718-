package com.rs.game.entity.mobile;

import java.io.Serializable;

import com.rs.game.entity.mobile.Hit.HitLook;
import com.rs.game.entity.mobile.player.Player;
import com.rs.game.entity.mobile.player.content.ContentType;
import com.rs.utilities.Utilities;

public final class Poison implements Serializable {

	private static final long serialVersionUID = -6324477860776313690L;

	private transient MobileEntity entity;
	private transient MobileEntity damageSource;
	private int poisonDamage;
	private int poisonCount;

	public void setEntity(MobileEntity entity) {
		this.entity = entity;
	}

	public MobileEntity getEntity() {
		return entity;
	}

	public void makePoisoned(MobileEntity source, int startDamage) {
		this.damageSource = source;
		if (poisonDamage > startDamage)
			return;
		if (entity instanceof Player) {
			Player player = ((Player) entity);
			if (player.getPoisonImmune() > Utilities.currentTimeMillis())
				return;
			if (poisonDamage == 0)
				player.getPackets().sendGameMessage("You are poisoned.");
		}
		poisonDamage = startDamage;
		refresh();
	}

	public void processPoison() {
		if (!entity.isDead() && isPoisoned()) {
			if (poisonCount > 0) {
				poisonCount--;
				return;
			}
			boolean heal = false;
			if (entity instanceof Player) {
				Player player = ((Player) entity);
				// inter opened we dont poison while inter opened like at rs
				if (player.getInterfaceManager().containsScreenInter())
					return;
				if (player.getContent().get(ContentType.AURAS).hasPoisonPurge())
					heal = true;
			}
			entity.applyHit(new Hit(damageSource, poisonDamage, heal ? HitLook.HEALED_DAMAGE : HitLook.POISON_DAMAGE));
			poisonDamage -= 2;
			if (isPoisoned()) {
				poisonCount = 30;
				return;
			}
			reset();
		}
	}

	public void reset() {
		poisonDamage = 0;
		poisonCount = 0;
		refresh();
	}

	public void refresh() {
		if (entity instanceof Player) {
			Player player = ((Player) entity);
			player.getPackets().sendConfig(102, isPoisoned() ? 1 : 0);
		}
	}

	public boolean isPoisoned() {
		return poisonDamage >= 1;
	}
}
