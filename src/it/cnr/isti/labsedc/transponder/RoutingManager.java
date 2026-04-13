package it.cnr.isti.labsedc.transponder;

public class RoutingManager extends Thread {

	private CHANNELMODE transmissionMode;
	private LoRaSender senderLora;
	private AT_4G_SMSSender mobile4g;
	private SatelliteSender sat;

	public RoutingManager(CHANNELMODE transmissionMode) {
		this.transmissionMode = transmissionMode;
	}
	
	public RoutingManager(LoRaSender senderLora, AT_4G_SMSSender mobile4g, SatelliteSender sat, CHANNELMODE transmissionMode) {
		this.senderLora = senderLora;
		this.mobile4g = mobile4g;
		this.sat = sat;
		this.transmissionMode = transmissionMode;
	}

	public void run() {
		switch (transmissionMode) {
		case LORA_ONLY: {
			senderLora.start();
		}
		case UMTS_LGE_4G_ONLY: {
			mobile4g.start();
		}
		case SAT_ONLY: {
			sat.start();
		}
		case MULTI_CASCADE: {
			
		}
		case MULTI_SIMULTANEOUS: {
			senderLora.start();
			mobile4g.start();
			sat.start();
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + transmissionMode);
		}
	}
}
