/**
 * 
 */
package ca.wimsc.client.common.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.wimsc.client.common.util.GeocoderUtil;

/**
 * @author James
 * 
 */
public class RoutePathTest {

	@Test
	public void testMarmarshallAndUnmarshall() {

		RoutePath routePath = new RoutePath();
		routePath.setDirection1Tag("DIR1");
		routePath.setDirection2Tag("DIR2");
		routePath.setLastUpdatedTimestamp(new Date());

		routePath.setRoutePathElements(new ArrayList<RoutePathElement>());
		routePath.getRoutePathElements().add(new RoutePathElement());
		routePath.getRoutePathElements().get(0).setClosestStopTagDirection1(0, "stop1a");
		routePath.getRoutePathElements().get(0).setClosestStopTagDirection1(1, "stop1b");
		routePath.getRoutePathElements().get(0).setNewPathEntry(true);
		routePath.getRoutePathElements().get(0).setSpeedInKmhDirection1(10);
		routePath.getRoutePathElements().get(0).setSpeedInKmhDirection2(20);
		routePath.getRoutePathElements().get(0).setLatitude(GeocoderUtil.recodeLatOrLon(43.64462));
		routePath.getRoutePathElements().get(0).setLongitude(GeocoderUtil.recodeLatOrLon(-79.41618));
		routePath.getRoutePathElements().add(new RoutePathElement());
		routePath.getRoutePathElements().get(1).setClosestStopTagDirection1(0, "stop2a");
		routePath.getRoutePathElements().get(1).setClosestStopTagDirection1(1, "stop2b");
		routePath.getRoutePathElements().get(1).setNewPathEntry(true);
		routePath.getRoutePathElements().get(1).setSpeedInKmhDirection1(11);
		routePath.getRoutePathElements().get(1).setSpeedInKmhDirection2(21);
		routePath.getRoutePathElements().get(1).setLatitude(GeocoderUtil.recodeLatOrLon(43.64131));
		routePath.getRoutePathElements().get(1).setLongitude(GeocoderUtil.recodeLatOrLon(79.41489));

		String marshalled = routePath.marshall();

		RoutePath actual = new RoutePath(marshalled);
		recodeRoutePathElements(actual.getRoutePathElements());
		Assert.assertEquals(routePath, actual);

		actual = new RoutePath("1,gj4ydejx,2,DIR1,DIR2,1,stop1a,stop1b,2ljn2,-4q7si,a,k,1,stop2a,stop2b,-97,9gfhf,b,l,");
		recodeRoutePathElements(actual.getRoutePathElements());
		Assert.assertEquals(routePath, actual);

		actual = new RoutePath("2,gj4ydejx,2,DIR1,DIR2,1,stop1a,stop1b,2ljn2,-4q7si,a,k,222,1,stop2a,stop2b,-97,9gfhf,b,l,222,");
		recodeRoutePathElements(actual.getRoutePathElements());
		Assert.assertEquals(routePath, actual);
		
		String str = "1,gj54ka1n,2,501_eastbound,501_westbound,1,lake39th_e,lake39th_w,2lfmz,-4qhfr,-1,-1,,lake39th_e,lake39th_w,7,1c,3,-1,,lake37th_e,lake37th_w,1i,6n,9,i,,lakelong_e,lakelong_w,2p,bs,b,d,,lake31st_e,lake30th_w,2d,ab,e,d,,lake28th_e,lake29th_w,1g,7f,e,d,,lake27th_e,lake26th_w,1r,7f,e,g,,lake23rd_e,lake22nd_w,28,a3,e,g,,lakekipl_e,lakekipl_w,2m,bs,f,g,,lake15th_e,lake15th_w,1p,7p,f,g,,lake13th_e,lake13th_w,1h,6t,f,f,,lake10th_e,lake10th_w,1x,8p,f,f,,lake7th_e,lakeisli_w,24,9i,f,f,,lake5th_e,lake5th_w,1e,6e,g,h,,lake3rd_e,lake3rd_w,1d,68,g,f,,lakefirs_e,lakefirs_w,1c,62,g,j,,lakeroya_e,lakeroya_w,3v,f4,g,j,,lakelake_e,lakelake_w,98,7z,g,j,,lakemile_e,lakesymo_w,3s,c,h,j,,lakenorr_e,lakehill_w,7b,u,h,-1,,lakesumm_e,lakehill_w,3g,i,h,j,,lakemimi_e,lakemimi_w,47,1c,g,i,,lakesupe_e,lakesupe_w,3g,1v,f,h,,lakeburl_e,lakeburl_w,6j,3o,f,g,,lakeloui_e,lakeloui_w,4l,2a,f,j,,lakelegi_e,lakelegi_w,4f,8l,f,f,,lakepark_e,lakepark_w,6p,5s,f,f,,lakeopp_e,lakeat2_w,84,4c,e,f,";
		actual = new RoutePath(str);
		
		
	}


	/**
	 * @param theRoutePathElements
	 */
	private void recodeRoutePathElements(List<RoutePathElement> theRoutePathElements) {
		for (RoutePathElement next : theRoutePathElements) {
			next.setLatitude(GeocoderUtil.recodeLatOrLon(next.getLatitude()));
			next.setLongitude(GeocoderUtil.recodeLatOrLon(next.getLongitude()));
		}

	}

}
