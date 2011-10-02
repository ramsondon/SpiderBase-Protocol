package spider.prototype.services.modules.position;

import java.util.ArrayList;

import spider.prototype.services.Controller;
import spider.prototype.services.communication.addresses.Address;
import spider.prototype.services.modules.AbstractModule;
import spider.prototype.services.yellowpage.ServiceDescription;
import spider.prototype.utils.BinaryTransformer;
import spider.prototype.utils.PacketBuilder;

public abstract class GPSModule extends AbstractModule {

	private static final byte divider = new String(";").getBytes()[0];

	public GPSModule(int serviceId) {
		super(ServiceDescription.GPSPosition, serviceId, false);
	}

	@Override
	public void use(byte[] parameters) {
		// ignore
	}

	@Override
	public void observe(final Address observer,
			final int intervalInMilliseconds, byte[] alertConditions) {

		// ignore alert conditions

		// start a timer and send each intervalInMilliseconds the value of the
		// sensor
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						Thread.sleep(intervalInMilliseconds);

						byte[] response = PacketBuilder.buildDataResponse(
								getServiceDescription(), getServiceId(),
								getValue());

						if (!Controller.getInstance().getCommunicationService()
								.sendAt(observer, response)) {
							// TODO [Protokollierung]

							// quit observation
							break;
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static LatLon parseData(byte[] data) {

		LatLon ll = new LatLon();

		int index = 0;
		ArrayList<Byte> latitude = new ArrayList<Byte>();
		ArrayList<Byte> longitude = new ArrayList<Byte>();
		while (index < data.length && data[index] != divider) {
			latitude.add(data[index++]);
		}

		// ignore divider
		index++;

		while (index < data.length) {
			longitude.add(data[index++]);
		}

		byte[] latArr = new byte[latitude.size()];
		byte[] lonArr = new byte[longitude.size()];

		int i = 0;
		for (Byte b : latitude) {
			latArr[i++] = b;
		}
		i = 0;
		for (Byte b : longitude) {
			lonArr[i++] = b;
		}

		ll.Lat = BinaryTransformer.toDouble(latArr);
		ll.Lon = BinaryTransformer.toDouble(lonArr);

		return ll;
	}

	public static byte[] transformData(LatLon data) {

		byte[] lat = BinaryTransformer.toByta(data.Lat);
		byte[] lon = BinaryTransformer.toByta(data.Lon);

		byte[] transformed = new byte[lat.length + 1 + lon.length];

		// add lat
		for (int i = 0; i < lat.length; i++) {
			transformed[i] = lat[i];
		}

		transformed[lat.length] = divider;

		// add lon
		for (int i = lat.length + 1; i < (lat.length + 1 + lon.length); i++) {
			transformed[i] = lon[i - lat.length - 1];
		}

		return transformed;
	}
}
