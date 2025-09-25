package it.cnr.isti.labsedc.transponder;

public class RoutingManager extends Thread {

	private CHANNELMODE transmissionMode;

	public RoutingManager(CHANNELMODE transmissionMode) {
		this.transmissionMode = transmissionMode;
	}
	
	public void run() {
		switch (transmissionMode) {
		case LORA_ONLY: {
					
		}
		case UMTS_LGE_4G_ONLY: {
			
		}
		case SAT_ONLY: {
			
		}
		case MULTI_CASCADE: {
			
		}
		case MULTI_SIMULTANEOUS: {
			
		}
		
		default:
			throw new IllegalArgumentException("Unexpected value: " + transmissionMode);
		}
	}
}
