/**
 * 
 */
package ca.wimsc.client.common.util;

import org.junit.Test;

import junit.framework.Assert;

/**
 * @author James
 *
 */
public class GeocoderUtilTest {

	@Test
	public void testEncodeAndDecode() {
		
		
		double point = 43.63894;
		int value = GeocoderUtil.encodeLatOrLon(point);
		String encoded = Integer.toString(value, Character.MAX_RADIX);
		
		double actual = GeocoderUtil.decodeLatOrLonDeltaFromString(encoded, 0.0);
		Assert.assertEquals(point, actual);
		
	}
	
	@Test
	public void testGetAngleFromPointAToPointBInDegrees() {
		double lat1 = 43.64462;
		double lon1 = -79.41618;
		double lat2 = 43.64131;
		double lon2 = 79.41489;
		int angle = GeocoderUtil.getAngleFromPointAToPointBInDegrees(lat1, lon1, lat2, lon2);
		Assert.assertEquals(111, angle);

		lat2 = 43.64462;
		lon2 = -79.41618;
		lat1 = 43.64131;
		lon1 = 79.41489;
		angle = GeocoderUtil.getAngleFromPointAToPointBInDegrees(lat1, lon1, lat2, lon2);
		Assert.assertEquals(249, angle);

		lat1 = 43.64131;
		lon1 = 79.41489;
		lat2 = 43.74131;
		lon2 = 79.41489;
		angle = GeocoderUtil.getAngleFromPointAToPointBInDegrees(lat1, lon1, lat2, lon2);
		Assert.assertEquals(0, angle);

		lat1 = 43.74131;
		lon1 = 79.41489;
		lat2 = 43.64131;
		lon2 = 79.41489;
		angle = GeocoderUtil.getAngleFromPointAToPointBInDegrees(lat1, lon1, lat2, lon2);
		Assert.assertEquals(180, angle);
		
	}
	
}
