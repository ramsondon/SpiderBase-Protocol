package spider.prototype.services.yellowpage;

import spider.prototype.services.communication.addresses.Address;

public class YellowPage {

	public static final int NO_SERVICE_ID = -1;
	
	private ServiceDescription serviceDescription;
	private Address address;
	private int serviceId;
	
	/**
	 * Constructor
	 * 
	 * @param serviceDescription
	 * @param address
	 */
	public YellowPage(ServiceDescription serviceDescription, Address address, int serviceId) {
		
		this.serviceDescription = serviceDescription;
		this.address = address;
		this.serviceId = serviceId;
	}

	public ServiceDescription getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(ServiceDescription serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Integer getServiceId() {
		return this.serviceId;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(serviceDescription.name());
		sb.append(" - ");
		sb.append(address.toString());
		
		return sb.toString();
	}
}
