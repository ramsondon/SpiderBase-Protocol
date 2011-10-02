package spider.prototype.services.modules;

import spider.prototype.services.communication.addresses.Address;
import spider.prototype.services.yellowpage.ServiceDescription;

public abstract class AbstractModule {

	private int serviceId;
	private ServiceDescription serviceDesc;
	private boolean usable;

	public AbstractModule(ServiceDescription serviceDesc, int serviceId, boolean usable) {
		this.serviceDesc = serviceDesc;
		this.usable = usable;
		this.serviceId = serviceId;
	}

	public boolean isUsable() {
		return usable;
	}
	
	public ServiceDescription getServiceDescription() {
		return serviceDesc;
	}
	
	public int getServiceId() {
		return serviceId;
	}

	public abstract byte[] getValue();
	public abstract void use(byte[] parameters);
	public abstract void observe(Address observer, int intervalInMilliseconds, byte[] alertConditions);
}
