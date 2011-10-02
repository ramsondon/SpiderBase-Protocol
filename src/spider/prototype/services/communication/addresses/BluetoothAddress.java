package spider.prototype.services.communication.addresses;

import java.util.HashMap;

import spider.prototype.services.communication.NetworkType;
import spider.prototype.utils.BinaryTransformer;

public class BluetoothAddress extends Address {

	/***
	 * This hashmap is ued to map mac addresses to service connection urls. Once
	 * the connection url of a bluetooth address object is set, this hashmap
	 * will be updated.
	 * 
	 * If we get a response with a bluetooth address in the package, the
	 * connection url is unknown. This hashmap solves this problem!
	 */
	private static HashMap<String, String> macToConnUrl = new HashMap<String, String>();

	/***
	 * The number of bytes for the tcp/ip address
	 */
	public static final int CountOfBytes = 6;

	/***
	 * The address as byte array
	 */
	private byte[] addressAsBytes;

	/***
	 * The address as string. Format: 00 11 22 AA BB CC, each part is 1 byte
	 */
	private String addressAsString;

	/***
	 * The connection url (service record url of the spider service)
	 */
	private String connectionURL;

	public BluetoothAddress(byte[] address) {
		super(NetworkType.Bluetooth, CountOfBytes);

		this.addressAsBytes = address;
		this.addressAsString = convertToString(address);
	}

	/***
	 * 
	 * @param address
	 *            The address as string. Format: 00:11:22:AA:BB:CC
	 */
	public BluetoothAddress(String address) {
		super(NetworkType.Bluetooth, CountOfBytes);

		this.addressAsString = address;
		this.addressAsBytes = convertToByteArray(address);
	}

	/***
	 * 
	 * @param address
	 *            The address to convert
	 * @return The string representation for the given byte array
	 */
	private String convertToString(byte[] address) {

		String addressAsString = "";
		for (int i = 0; i < this.getCountOfBytes(); i++) {
			// transform the byte into an int and then into a string
			int notHex = BinaryTransformer.toInt(address[i]);
			if (notHex <= 15) {
				addressAsString += "0";
			}
			addressAsString += Integer.toHexString(notHex).toUpperCase();
			addressAsString += ":";
		}
		addressAsString = addressAsString.substring(0,
				addressAsString.length() - 1);
		return addressAsString;
	}

	/***
	 * 
	 * @param address
	 *            The address to convert
	 * @return The byte array for the given address
	 */
	private byte[] convertToByteArray(String address) {

		byte[] byteAddress = new byte[this.getCountOfBytes()];
		String[] values = null;
		if (!address.contains(":")) {
			values = new String[6];
			values[0] = address.substring(0, 2);
			values[1] = address.substring(2, 4);
			values[2] = address.substring(4, 6);
			values[3] = address.substring(6, 8);
			values[4] = address.substring(8, 10);
			values[5] = address.substring(10, 12);
			addressAsString = values[0] + ":" + values[1] + ":" + values[2]
					+ ":" + values[3] + ":" + values[4] + ":" + values[5];
		} else {
			values = address.split("\\:");
		}
		for (int i = 0; i < values.length; i++) {

			// convert string into hex and then into int lolz and finally to
			// byte
			byteAddress[i] = BinaryTransformer.toByte(Integer.parseInt(
					values[i], 16));
		}

		return byteAddress;
	}

	@Override
	protected byte[] getAddressOnlyAsBytes() {

		return addressAsBytes;
	}

	@Override
	public String getAddressAsString() {
		return addressAsString;
	}

	public void setConnectionURL(String connectionURL) {
		this.connectionURL = connectionURL;

		macToConnUrl.put(addressAsString, connectionURL);
	}

	public String getConnectionURL() {

		if (connectionURL == null || connectionURL.length() == 0) {
			connectionURL = macToConnUrl.get(addressAsString);
		}

		return connectionURL;
	}
}
