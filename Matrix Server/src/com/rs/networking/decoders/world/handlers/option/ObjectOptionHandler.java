package com.rs.networking.decoders.world.handlers.option;

/**
 * @author Abysa/Dido#4821
 * 11/14/18
 */
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.rs.game.WorldTile;
import com.rs.game.entity.GameObject;
import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.handlers.option.OptionType.ObjectOptionType;

public abstract class ObjectOptionHandler extends OptionHandler {
	
	public abstract GameObject[] getHandledObjects();

	public abstract void execute(final ObjectOptionType type, final Player player, final GameObject object);
	
	public GameObject[] toObjArr(GameObject... tiles) {
		return tiles;
	}
	
	@Override
	public int[] getIds() {
		List<Integer> ids = new ArrayList<Integer>();
		for(GameObject obj : getHandledObjects()) {
			if(ids.contains(obj.getId()))
				ids.add(obj.getId());
		}
		Integer[] integers = ids.toArray(new Integer[ids.size()]);
		int[] primitives = ArrayUtils.toPrimitive(integers);
		return primitives;
	}

}
