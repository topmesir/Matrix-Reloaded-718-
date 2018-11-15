package com.rs.networking.decoders.world.handlers.option.impl.object;

import com.rs.game.WorldTile;
import com.rs.game.entity.GameObject;
import com.rs.game.entity.mobile.player.Player;
import com.rs.networking.decoders.world.handlers.option.ObjectOptionHandler;
import com.rs.networking.decoders.world.handlers.option.OptionType;
import com.rs.networking.decoders.world.handlers.option.OptionType.ObjectOptionType;

public class ExampleObjectOption extends ObjectOptionHandler {

	@Override
	public void execute(ObjectOptionType type, Player player, GameObject object) {
		//use for multiple ids in handled objects for this handler
		switch(object.getId()) {
		case 69696969:
			//use for constraining method to same tile as inputted tile
			GameObject obj1 = getHandledObjects()[0];
			if(object.getWorldTile() == obj1.getWorldTile())
				System.out.println("");
			
			break;
		case 69696970:
			
			//use for constraining method to same tile as inputted tile
			GameObject obj2 = getHandledObjects()[1];
			if(object.getWorldTile() == obj2.getWorldTile())
				System.out.println("");
			
			break;
		}
	}

	@Override
	public OptionType getType() {
		return OptionType.OBJECT;
	}

	@Override
	public GameObject[] getHandledObjects() {
		// Can use multiple object ids in handlers, aswell as diff coords for same ids
		//                             id        x         y         z
		return toObjArr(new GameObject(69696969, 69696961, 69696961, 69696961),
				new GameObject(69696970, 69696962, 69696961, 69696961));
	}

}
