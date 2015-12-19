package jayms.spellbound.spells.collision;

public enum CollisionResult {
	
	SUCCESS(1), DESTROYED(0);

	private int result;
	
	private CollisionResult(int result) {
		this.result = result;
	}
	
	public int getResult() {
		return result;
	}
}
