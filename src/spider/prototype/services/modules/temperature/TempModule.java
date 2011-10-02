package spider.prototype.services.modules.temperature;

import java.lang.reflect.Constructor;

import spider.prototype.services.Controller;
import spider.prototype.services.communication.addresses.Address;
import spider.prototype.services.modules.AbstractModule;
import spider.prototype.services.yellowpage.ServiceDescription;
import spider.prototype.utils.BinaryTransformer;
import spider.prototype.utils.PacketBuilder;

public abstract class TempModule extends AbstractModule {

	
	public TempModule(int serviceId) {
		
		super(ServiceDescription.Temperature, serviceId, false);
	}

	@Override
	public void observe(final Address observer, final int intervalInMilliseconds,
			final byte[] alertConditions) {
		


		// start a timer and send each intervalInMilliseconds the value of the
		// sensor
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(intervalInMilliseconds);

						// alert condition
						if (BinaryTransformer.toInt(getValue()) <= TempModule.bytaToMm(alertConditions).Min  
								|| BinaryTransformer.toInt(getValue()) >= TempModule.bytaToMm(alertConditions).Max) {
							
							// build response & send
							byte[] response = PacketBuilder.buildDataResponse(
									getServiceDescription(), getServiceId(),
									getValue());
	
							if (!Controller.getInstance().getCommunicationService()
									.sendAt(observer, response)) {
								// TODO [Protokollierung]
	
								// quit observation
								break;
							}
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	
		
	}

	@Override
	public void use(byte[] parameters) {
		// TODO Auto-generated method stub
		
	}
	
	public static MinMax bytaToMm(byte[] input) {
		
		byte[] lowby = new byte[4];
		byte[] highby = new byte[4];
		for (int i = 0; i < 4; i++) {
			lowby[i] = input[i];
		}
		for (int i = 0; i < 4; i++) {
			highby[i] = input[i + 4];
		}
		
		return new MinMax(BinaryTransformer.toInt(lowby), BinaryTransformer.toInt(highby));
	}
	
	public static byte[] mmToByta(MinMax input) {
		
		byte[] lowby = BinaryTransformer.toByta(input.Min);
		byte[] highby = BinaryTransformer.toByta(input.Max);
		
		byte[] result = new byte[8];
		
		for (int i = 0; i < 4; i++) {
			result[i] = lowby[i];
		}
		for (int i = 0; i < 4; i++) {
			result[i + 4] = highby[i];
		}
		
		return result;
	}

}
