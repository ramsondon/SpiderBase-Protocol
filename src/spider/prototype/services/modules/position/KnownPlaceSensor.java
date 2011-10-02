package spider.prototype.services.modules.position;

import spider.prototype.services.communication.addresses.Address;
import spider.prototype.services.modules.AbstractModule;
import spider.prototype.services.yellowpage.ServiceDescription;
import spider.prototype.utils.BinaryTransformer;

public class KnownPlaceSensor extends AbstractModule {

	private int placeId;
	
	public KnownPlaceSensor(int serviceId, int placeId) {
		super(ServiceDescription.KnownPlace, serviceId, false);
		
		this.placeId = placeId;
	}

	public int getPlaceId() {
		return placeId;
	}
	
	@Override
	public byte[] getValue() {
		return BinaryTransformer.toByta(placeId);
	}

	@Override
	public void observe(Address observer, int intervalInMilliseconds,
			byte[] alertConditions) {
		// ignore		
	}

	@Override
	public void use(byte[] parameters) {
		// ignore
	}

}
