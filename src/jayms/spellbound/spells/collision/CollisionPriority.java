package jayms.spellbound.spells.collision;

public enum CollisionPriority {

	LOWEST(1), LOW(2), NORMAL(3), HIGH(4), HIGHEST(5);
	
	private int prior;
	
	private CollisionPriority(int prior) {
		this.prior = prior;
	}
	
	public int getPriority() {
		return prior;
	}
}
