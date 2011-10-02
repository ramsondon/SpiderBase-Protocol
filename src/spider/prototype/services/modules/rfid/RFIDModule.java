package spider.prototype.services.modules.rfid;

import spider.prototype.services.communication.addresses.Address;
import spider.prototype.services.modules.AbstractModule;
import spider.prototype.services.yellowpage.ServiceDescription;

public abstract class RFIDModule extends AbstractModule {

	public RFIDModule(int serviceId) {
		super(ServiceDescription.RFIDNumber, serviceId, false);

	}

	@Override
	public void observe(Address observer, int intervalInMilliseconds,
			byte[] alertConditions) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void use(byte[] parameters) {
		// TODO Auto-generated method stub
		
	}

}
