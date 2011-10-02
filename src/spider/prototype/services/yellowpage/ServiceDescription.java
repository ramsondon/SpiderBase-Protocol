package spider.prototype.services.yellowpage;

public enum ServiceDescription {

	YellowPageService, ServerConnection, Temperature, GSMPosition, GPSPosition, KnownPlace, RFIDNumber, Invalid;

	public static byte convert(ServiceDescription desc) {

		switch (desc) {
		case YellowPageService:
			return 0x10;
		case ServerConnection:
			return 0x01;
		case Temperature:
			return 0x02;
		case GSMPosition:
			return 0x03;
		case GPSPosition:
			return 0x04;
		case KnownPlace:
			return 0x05;
		case RFIDNumber:
			return 0x06;
		default:
			return 0x00;
		}

	}

	public static ServiceDescription convert(byte desc) {

		if (desc == convert(YellowPageService)) {
			return YellowPageService;
		} else if (desc == convert(ServerConnection)) {
			return ServerConnection;
		} else if (desc == convert(Temperature)) {
			return Temperature;
		} else if (desc == convert(GSMPosition)) {
			return GSMPosition;
		} else if (desc == convert(GPSPosition)) {
			return GPSPosition;
		} else if(desc == convert(KnownPlace)) {
			return KnownPlace;
		} else if(desc == convert(RFIDNumber)) {
			return RFIDNumber;
		}

		return Invalid;
	}
}
