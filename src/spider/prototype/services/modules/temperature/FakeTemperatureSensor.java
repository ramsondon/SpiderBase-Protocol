package spider.prototype.services.modules.temperature;

import java.util.Random;

import spider.prototype.utils.BinaryTransformer;

public class FakeTemperatureSensor extends TempModule {

	public FakeTemperatureSensor(int serviceId) {
		super(serviceId);
	}
	
	private double temp = 10;

	@Override
	public byte[] getValue() {

		temp += (new Random().nextDouble() * 4 - 2);
		return BinaryTransformer.toByta(temp);
	}

}
