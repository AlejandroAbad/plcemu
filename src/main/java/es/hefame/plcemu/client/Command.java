package es.hefame.plcemu.client;

public class Command {

	protected String name;
	protected String[] parms;
	
	public Command(String raw) {
		
		if (raw == null) {
			name = "null";
			parms = new String[0];
		}
		
		String[] chunks = raw.split("\\s");
		if (chunks.length > 0) {
			name = chunks[0];
			parms = new String[chunks.length - 1];
			for (int i = 1 ; i < chunks.length ; i++) {
				parms[i-1] = chunks[i];
			}
		}
	}
	
	public String getName() {
		return this.name;
	}
	
	public int noParms() {
		return this.parms.length;
	}
	
	public String asS(int i) {
		if (this.parms.length > i) {
			return this.parms[i];
		}
		return "null";
	}

	public int asI(int i) {
		if (this.parms.length > i) {
			try {
				return Integer.parseInt(this.parms[i]);
			} catch (NumberFormatException e) {}
		}
		return -1;
	}

}
