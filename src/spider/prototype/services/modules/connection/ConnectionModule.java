package spider.prototype.services.modules.connection;

import spider.prototype.services.communication.addresses.Address;
import spider.prototype.services.communication.addresses.TCPAddress;
import spider.prototype.services.modules.AbstractModule;
import spider.prototype.services.modules.IDataRequester;
import spider.prototype.services.modules.IServiceObserver;
import spider.prototype.services.yellowpage.ServiceDescription;

public abstract class ConnectionModule extends AbstractModule implements IDataRequester,
		IServiceObserver {
	
	private String server = "http://127.0.0.1/spider/SaveData.php";

	public ConnectionModule(int serviceId) {
		super(ServiceDescription.ServerConnection, serviceId, true);
		
	}

	@Override
	public byte[] getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void observe(Address observer, int intervalInMilliseconds,
			byte[] alertConditions) {
		// TODO Auto-generated method stub

	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getServer() {
		return server;
	}

}
