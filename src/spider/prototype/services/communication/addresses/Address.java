package spider.prototype.services.communication.addresses;

import spider.prototype.services.communication.NetworkType;

/***
 * 
 * An abstract class to represent a network address
 * 
 * @author Thomas
 * 
 */
public abstract class Address {

	/***
	 * The network type of the address
	 */
	private NetworkType nt;

	/***
	 * The length of the address (without the NetworkTypeFlag byte)
	 */
	private int countOfBytes;

	/***
	 * Initializes the address
	 * 
	 * @param nt
	 *            The network type of the address
	 * @param countOfBytes
	 *            The length of the address (without the NetworkTypeFlag byte)
	 */
	public Address(NetworkType nt, int countOfBytes) {
		this.nt = nt;
		this.countOfBytes = countOfBytes;
	}

	/***
	 * 
	 * @return The network type of the address
	 */
	public NetworkType getNetworkType() {
		return nt;
	}

	/***
	 * 
	 * @return The length of the address (without the NetworkTypeFlag byte)
	 */
	public int getCountOfBytes() {
		return countOfBytes;
	}

	/***
	 * 
	 * @return The length of the address with the NetworkTypeFlag byte
	 */
	public int getOffset() {
		return countOfBytes + 1;
	}

	/***
	 * 
	 * @return The address with the NetworkTypeFlag byte as byte array
	 */
	public byte[] getAddressAsBytes() {

		byte[] addressWithNetworkType = new byte[1 + countOfBytes];

		// add the network type to the address
		addressWithNetworkType[0] = NetworkType.convert(nt);
		byte[] address = getAddressOnlyAsBytes();
		for (int i = 1; i <= countOfBytes; i++) {
			addressWithNetworkType[i] = address[i - 1];
		}

		return addressWithNetworkType;
	}

	/***
	 * 
	 * @return The address as string
	 */
	public abstract String getAddressAsString();

	/***
	 * 
	 * @return The address as byte array without the NetworkTypeFlag byte
	 */
	protected abstract byte[] getAddressOnlyAsBytes();

	/***
	 * 
	 * @param dataContainingAddress
	 *            The data which contains the network type and the address
	 * @return An instance of an address implementation for the correct network
	 *         type (offset = 0)
	 */
	public static Address createAddress(byte[] dataContainingAddress) {
		return createAddress(dataContainingAddress, 0);
	}
	
	/***
	 * 
	 * @param dataContainingAddress
	 *            The data which contains the network type and the address
	 *          @param offset The offset for the given byte array
	 * @return An instance of an address implementation for the correct network
	 *         type
	 */
	public static Address createAddress(byte[] dataContainingAddress, int offset) {

		// parse the network type
		switch (NetworkType.convert(dataContainingAddress[offset])) {
		case TCP:

			// build tcp address
			byte[] tcpaddress = new byte[TCPAddress.CountOfBytes];
			for (int i = offset + 1, j = 0; j < TCPAddress.CountOfBytes; i++, j++) {
				tcpaddress[j] = dataContainingAddress[i];
			}

			return new TCPAddress(tcpaddress);
		case Bluetooth:
			
			// build bluetooth address
			byte[] btaddress = new byte[BluetoothAddress.CountOfBytes];
			for (int i = offset + 1, j = 0; j < BluetoothAddress.CountOfBytes; i++, j++) {
				btaddress[j] = dataContainingAddress[i];
			}

			return new BluetoothAddress(btaddress);
			
		case ZigBee:
		default:
			throw new IllegalArgumentException(
					"The given data containing the NetworkType and the source address isn't supported.");
		}
	}

	@Override
	public String toString() {
		return getAddressAsString();
	}
}
