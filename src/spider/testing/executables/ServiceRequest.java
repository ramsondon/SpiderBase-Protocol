package spider.testing.executables;

import spider.prototype.services.Controller;
import spider.prototype.services.communication.addresses.TCPAddress;
import spider.prototype.services.communication.mappinglayer.TCPMapper;
import spider.prototype.services.modules.IDataRequester;
import spider.prototype.services.yellowpage.ServiceDescription;

public class ServiceRequest implements IDataRequester {

	@Override
	public void onDataResponse(ServiceDescription serviceDesc, int serviceId,
			byte[] response) {
		// TODO Auto-generated method stub
		
	}
}
