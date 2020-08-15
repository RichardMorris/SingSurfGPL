package org.singsurf.singsurf.definitions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ProjectComponents {
	String name;
	List<String> inputs = new ArrayList<>();
	List<String> ingredients = new ArrayList<>();

	Map<String, Map<String, String>> inputOptions = new HashMap<>();

	public ProjectComponents(String name) {
		this.name = name;
	}

	public boolean addIngredient(String key) {
		return ingredients.add(key);
	}

	public boolean addInput(String key) {
		inputOptions.put(key, new HashMap<>());
		return inputs.add(key);
	}

	public String getName() {
		return name;
	}

	public List<String> getInputs() {
		return inputs;
	}

	public List<String> getIngredients() {
		return ingredients;
	}

	public boolean isEmpty() {
		return inputs.isEmpty() && ingredients.isEmpty();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<projectComponents name=\"" + this.name + "\">\n");
		for (String in : inputs) {
			sb.append("  <input name=\"" + in + "\" >\n");
			Map<String, String> map = inputOptions.get(in);
			for (Entry<String, String> ent : map.entrySet()) {
				sb.append("    <inputOpt name=\"" + ent.getKey() + "\" value=\"" + ent.getValue() + "\" />\n");
			}
			sb.append("  </input>\n");
		}
		for (String in : ingredients) {
			sb.append("  <ingredient name=\"" + in + "\" />\n");
		}
		sb.append("</projectComponents>\n");
		return sb.toString();
	}

	public String addInputOption(String ingr, String key, String value) {
		Map<String, String> map = inputOptions.get(ingr);
		return map.put(key, value);
	}
	
	public Map<String,String> getInputOptions(String name) {
		return inputOptions.get(name);
	}
}