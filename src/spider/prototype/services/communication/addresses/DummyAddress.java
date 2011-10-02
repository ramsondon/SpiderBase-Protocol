package spider.prototype.services.communication.addresses;

import spider.prototype.services.communication.NetworkType;

public class DummyAddress extends Address {

	private String addressName;
	
	public DummyAddress(NetworkType nt, int countOfBytes, String addressName) {
		super(nt, countOfBytes);
		
		this.addressName = addressName;
	}

	@Override
	public String getAddressAsString() {
		return addressName;
	}

	@Override
	protected byte[] getAddressOnlyAsBytes() {
		return new byte[] {};
	}

}
