package jayms.spellbound.spells;

import java.util.Arrays;

public enum SpellType {
	
	OFFENSE("Offense", "OFF", '4'), DEFENSE("Defense", "DEF", '1'), UTILITY("Utility", "UTI", '2');
	
	public static String[] SHORTENED = new String[3];
	
	static {
		SpellType[] values = values();
		for (int i = 0; i < values.length; i++) {
			SHORTENED[i] = values[i].getShortened();
		}
	}
	
	private String rep;
	private String sho;
	private char code;
	
	private SpellType(String rep, String sho, char code) {
		this.rep = rep;
		this.sho = sho;
		this.code = code;
	}
	
	public String getShortened() {
		return sho;
	}
	
	public char getCode() {
		return code;
	}
	
	public String ampersandCode() {
		return "&" + code;
	}
	
	@Override
	public String toString() {
		return rep;
	}
	
	public static SpellType getFromShortened(String s) {
		SpellType[] values = values();
		for (int i = 0; i < values.length; i++) {
			if (values[i].getShortened().equals(s)) {
				return values[i];
			}
		}
		return null;
	}
	
	public static boolean isShortenedName(String s) {
		return Arrays.asList(SHORTENED).contains(s);
	}
}
