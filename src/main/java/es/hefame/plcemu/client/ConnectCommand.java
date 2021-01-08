package es.hefame.plcemu.client;

public class ConnectCommand extends Command {

	public ConnectCommand(String raw) {
		super(raw);
	}

	public String getHost() {
		if (this.parms.length > 0) {
			return this.parms[0];
		}
		return "localhost";
	}

	public int getPort() {
		if (this.parms.length > 0) {
			try {
				return Integer.parseInt(this.parms[1]);
			} catch (NumberFormatException e) {}
		}
		return 3001;
	}

}
