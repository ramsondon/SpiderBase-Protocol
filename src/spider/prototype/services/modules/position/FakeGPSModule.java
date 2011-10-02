package spider.prototype.services.modules.position;


public class FakeGPSModule extends GPSModule {

	public FakeGPSModule(int serviceId) {
		super(serviceId);
	}

	@Override
	public byte[] getValue() {

		LatLon ll = new LatLon();
		ll.Lat = 47;
		ll.Lon = 9;

		return GPSModule.transformData(ll);
	}

}
