package spider.prototype.services.modules;

import spider.prototype.services.yellowpage.ServiceDescription;

public interface IDataRequester {

	public void onDataResponse(ServiceDescription serviceDesc, int serviceId, byte[] response);
}
