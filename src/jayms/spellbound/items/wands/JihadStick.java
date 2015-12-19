package jayms.spellbound.items.wands;

import jayms.spellbound.spells.SpellType;

public class JihadStick extends Wand {

	public JihadStick() {
		super("JihadStick");
		setManaPercentage(SpellType.OFFENSE, 25);
		setHealthPercentage(SpellType.OFFENSE, 20);
		setHealthPercentage(SpellType.DEFENSE, 10);
		setKnockbackPercentage(SpellType.DEFENSE, 20);
		setCooldownPercentage(SpellType.UTILITY, 17);
		setBackfirePercentage(SpellType.UTILITY, 5);
	}

}
