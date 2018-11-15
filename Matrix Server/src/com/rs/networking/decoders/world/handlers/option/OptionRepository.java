package com.rs.networking.decoders.world.handlers.option;

import java.util.HashMap;
import java.util.Map;

import com.rs.utilities.Utilities;

/**
 * @author Abysa/Dido#4821
 * 11/14/18
 */
public class OptionRepository {
	private static final String BASE_DIR = OptionRepository.class.getPackage().toString().replace("package ", "");

	public static Map<OptionType, HashMap<Integer, OptionHandler>> OPTIONS = new HashMap<>(); 

	@SuppressWarnings("unchecked")
	public static Map<OptionType, HashMap<Integer, OptionHandler>> loadOptions() {
		Map<OptionType, HashMap<Integer, OptionHandler>> options = new HashMap<>();
		try {
			Class<OptionHandler>[] optionArray = (Class<OptionHandler>[]) Utilities.getAllClasses(BASE_DIR + ".impl");
			for(Class<OptionHandler> option : optionArray) {
				if(option.getCanonicalName() == null)
					continue;
				Class<OptionHandler> packetClass = (Class<OptionHandler>) Class.forName(option.getCanonicalName());
				OptionHandler optionInstance = (OptionHandler) packetClass.newInstance();
				if(optionInstance.getIds() == null)
					continue;
				for(int id : optionInstance.getIds()) {
					if(id == -1)
						continue;
					if(options.get(optionInstance.getType()) == null)
						options.put(optionInstance.getType(), new HashMap<Integer, OptionHandler>());
					if(options.get(optionInstance.getType()).get(id) == null)
					options.get(optionInstance.getType()).put(id, optionInstance);
					else options.get(optionInstance.getType()).put(id, optionInstance);
				}
			}
		} catch(Throwable e) {
			e.printStackTrace();
		}
		return options;
	}

	public static OptionHandler getOptionHandler(OptionType type, int id) {
		OptionHandler button = OPTIONS.get(type).get(id);
		if(button != null)
			return button;
		return null;
	}

}
