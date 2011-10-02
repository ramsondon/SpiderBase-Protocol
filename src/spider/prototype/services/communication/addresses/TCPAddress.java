package spider.prototype.services.communication.addresses;

import spider.prototype.services.communication.NetworkType;
import spider.prototype.utils.BinaryTransformer;

/***
 * The Address implementation for TCP/IP
 * 
 * @author Thomas
 * 
 */
public class TCPAddress extends Address {

	/***
	 * The number of bytes for the tcp/ip address
	 */
	public static final int CountOfBytes = 4;

	/***
	 * The address as byte array
	 */
	private byte[] addressAsBytes;

	/***
	 * The address as string
	 */
	private String addressAsString;

	/***
	 * 
	 * @param address
	 *            The address as byte array without the NetworkTypeFlag byte
	 */
	public TCPAddress(byte[] address) {
		super(NetworkType.TCP, CountOfBytes);

		this.addressAsBytes = address;
		this.addressAsString = convertToString(address);
	}

	/***
	 * 
	 * @param address
	 *            The address as string
	 */
	public TCPAddress(String address) {
		super(NetworkType.TCP, CountOfBytes);

		this.addressAsBytes = convertToByteArray(address);
		this.addressAsString = address;
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
			addressAsString += BinaryTransformer.toInt(address[i]) + ".";
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

		// parse tcp address = xxx.xxx.xxx.xxx
		byte[] byteAddress = new byte[this.getCountOfBytes()];
		String[] numbers = address.split("\\.");
		for (int i = 0; i < numbers.length; i++) {
			byteAddress[i] = BinaryTransformer.toByte(Integer
					.valueOf(numbers[i]));
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
}
