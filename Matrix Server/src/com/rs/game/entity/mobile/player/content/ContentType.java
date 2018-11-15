package com.rs.game.entity.mobile.player.content;

import java.util.function.Supplier;

import com.rs.game.entity.mobile.player.content.type.Appearence;
import com.rs.game.entity.mobile.player.content.type.AuraManager;
import com.rs.game.entity.mobile.player.content.type.ChargesManager;
import com.rs.game.entity.mobile.player.content.type.CombatDefinitions;
import com.rs.game.entity.mobile.player.content.type.EmotesManager;
import com.rs.game.entity.mobile.player.content.type.FriendsList;
import com.rs.game.entity.mobile.player.content.type.MusicsManager;
import com.rs.game.entity.mobile.player.content.type.Prayer;
import com.rs.game.entity.mobile.player.content.type.PriceCheckManager;
import com.rs.game.entity.mobile.player.content.type.Trade;
import com.rs.game.entity.mobile.player.content.type.action.ActionManager;
import com.rs.game.entity.mobile.player.content.type.container.Bank;
import com.rs.game.entity.mobile.player.content.type.container.Equipment;
import com.rs.game.entity.mobile.player.content.type.container.Inventory;
import com.rs.game.entity.mobile.player.content.type.hint.HintIconsManager;
import com.rs.game.entity.mobile.player.content.type.skills.Skills;

/**
 * @author Abysa/Dido#4821
 * Dec 1, 2017 | 10:24:37 PM
 */
public class ContentType<T extends AbstractContent> {
	
	public final static ContentType<Prayer> PRAYER = new ContentType<>(Prayer::new); 
	public final static ContentType<PriceCheckManager> PRICE_CHECK = new ContentType<>(PriceCheckManager::new);
	public final static ContentType<Skills> SKILLS = new ContentType<>(Skills::new);
	public final static ContentType<AuraManager> AURAS = new ContentType<>(AuraManager::new);
	public final static ContentType<ChargesManager> CHARGES = new ContentType<>(ChargesManager::new);
	public final static ContentType<Appearence> APPEARANCE = new ContentType<>(Appearence::new);
	public final static ContentType<Trade> TRADE = new ContentType<>(Trade::new);
	public final static ContentType<EmotesManager> EMOTES = new ContentType<>(EmotesManager::new);
	public final static ContentType<HintIconsManager> HINTICON = new ContentType<>(HintIconsManager::new);
	public final static ContentType<CombatDefinitions> COMBATDEF = new ContentType<>(CombatDefinitions::new);
	public final static ContentType<FriendsList> FRIENDSLIST = new ContentType<>(FriendsList::new);
	public final static ContentType<MusicsManager> MUSIC = new ContentType<>(MusicsManager::new);
	public final static ContentType<ActionManager> ACTION = new ContentType<>(ActionManager::new);
	public final static ContentType<Bank> BANK = new ContentType<>(Bank::new);
	public final static ContentType<Equipment> EQUIPMENT = new ContentType<>(Equipment::new);
	public final static ContentType<Inventory> INVENTORY = new ContentType<>(Inventory::new);
	
	public static ContentType<? extends AbstractContent>[] values() {
		return new ContentType<?>[] {
			PRAYER,  
			PRICE_CHECK,
			SKILLS,
			AURAS,
			CHARGES,
			APPEARANCE,
			TRADE,
			EMOTES,
			HINTICON,
			COMBATDEF,
			FRIENDSLIST,
			MUSIC,
			ACTION,
			EQUIPMENT
		};		
	}

	private final Supplier<T> supplier;
	
	private ContentType(Supplier<T> supplier) {
		this.supplier = supplier;
	}

	public T supply() {
		return (T) supplier.get();
	}
}