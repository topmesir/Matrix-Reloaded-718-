package com.rs.engine.thread;

import com.rs.game.entity.mobile.MobileEntity;

/**
 * Holds an Action for Entity Synchronization.
 * 
 * @author Abysa/Dido#4821
 * @param <T>
 */
@FunctionalInterface
public interface MobileSyncAction<T extends MobileEntity> {

	public void execute(T e);

}
