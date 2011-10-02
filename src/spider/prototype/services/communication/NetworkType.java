package spider.prototype.services.communication;

/***
 * 
 * TCP = 0x10, Bluetooth = 0x20, ZigBee = 0x30
 * 
 * @author Thomas
 * 
 */
public enum NetworkType {

	TCP, Bluetooth, ZigBee;

	/***
	 * 
	 * @param NetworkTypeFlag
	 *            A byte that represents the NetworkType
	 * @return The NetworkType for the given NetworkTypeFlag
	 */
	public static NetworkType convert(byte NetworkTypeFlag) {

		if (NetworkTypeFlag == 0x10) {
			return NetworkType.TCP;
		} else if (NetworkTypeFlag == 0x20) {
			return NetworkType.Bluetooth;
		} else if (NetworkTypeFlag == 0x30) {
			return NetworkType.ZigBee;
		}

		throw new IllegalArgumentException(
				"Given address doesn't contain any known NetworkType tag (like tp for TCP). Address = "
						+ NetworkTypeFlag);
	}

	/***
	 * 
	 * @param nt The NetworkType to convert
	 * @return The byte that represents the NetworkType
	 */
	public static byte convert(NetworkType nt) {
		switch (nt) {
		case TCP:
			return 0x10;
		case Bluetooth:
			return 0x20;
		case ZigBee:
			return 0x30;
		default:
			throw new IllegalArgumentException(
					"Given NetworkType is not supported. NetworkType = " + nt);
		}
	}
}
