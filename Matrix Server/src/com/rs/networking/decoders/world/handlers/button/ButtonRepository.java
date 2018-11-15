package com.rs.networking.decoders.world.handlers.button;

import java.util.HashMap;
import java.util.Map;

import com.rs.utilities.Utilities;

/**
 * @author Abysa/Dido#4821
 * 11/14/18
 */
public class ButtonRepository {
	
	private static final String BASE_DIR = ButtonRepository.class.getPackage().toString().replace("package ", "");

	public static Map<Integer, InterfaceButtonHandler> BUTTONS = new HashMap<>(); 

	@SuppressWarnings("unchecked")
	public static HashMap<Integer, InterfaceButtonHandler> loadButtons() {
		HashMap<Integer, InterfaceButtonHandler> buttons = new HashMap<>();
		try {
			Class<InterfaceButtonHandler>[] buttonArray = (Class<InterfaceButtonHandler>[]) Utilities.getAllClasses(BASE_DIR + ".impl");
			for(Class<InterfaceButtonHandler> button : buttonArray) {
				if(button.getCanonicalName() == null)
					continue;
				Class<InterfaceButtonHandler> packetClass = (Class<InterfaceButtonHandler>) Class.forName(button.getCanonicalName());
				InterfaceButtonHandler buttonInstance = (InterfaceButtonHandler) packetClass.newInstance();
					if(buttonInstance.interfaceIds() == null)
						continue;
					if(buttonInstance.interfaceIds().length > 1) {
						for(int id : buttonInstance.interfaceIds()) {
							if(id != -1)
								buttons.put(id, buttonInstance);
						}
						continue;
					}
					if(buttonInstance.interfaceIds()[0] != -1)
						buttons.put(buttonInstance.interfaceIds()[0], buttonInstance);
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return buttons;
	}

	public static InterfaceButtonHandler getButtonHandler(int interfaceId) {
		InterfaceButtonHandler button = BUTTONS.get(interfaceId);
		if(button != null)
			return button;
		return null;
	}


}
