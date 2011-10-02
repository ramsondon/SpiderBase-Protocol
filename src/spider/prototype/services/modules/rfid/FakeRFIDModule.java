package spider.prototype.services.modules.rfid;

import spider.prototype.utils.BinaryTransformer;

public class FakeRFIDModule extends RFIDModule {

	public FakeRFIDModule(int serviceId) {
		super(serviceId);

	}

	@Override
	public byte[] getValue() {
		
		return BinaryTransformer.toByta("1,2,3,4,5,6,7,8,9,10,11,12,13,14,15");
	}

}
