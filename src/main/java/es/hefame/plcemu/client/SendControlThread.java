package es.hefame.plcemu.client;
import es.hefame.plcemu.util.Log;

public class SendControlThread extends Thread{

	PlcClientThread parent = null;
	long delay = 10000;
	
	public SendControlThread(PlcClientThread parent, long delay) {
		this.parent = parent;
		this.delay = delay;
	}
	
	
	@Override
	public void run() {
		Log.i("SendControlThread STARTED");
		while(this.parent.isOnline()) {
			
			try {
				this.parent.sendControlMessage("9999999", 88);
				Thread.sleep(delay);
			} catch (Exception e) {

			}
		}
		Log.i("SendControlThread STOPPED");
		
	}

}
