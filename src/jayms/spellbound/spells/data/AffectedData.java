package jayms.spellbound.spells.data;

import java.util.List;
import java.util.UUID;

import org.bukkit.entity.LivingEntity;

import jayms.spellbound.spells.Spell;

public class AffectedData extends CommonData {

	public List<LivingEntity> affected;
	
	public AffectedData(UUID user, Spell parent) {
		super(user, parent);
	}

}
