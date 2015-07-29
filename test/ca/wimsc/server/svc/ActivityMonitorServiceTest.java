package ca.wimsc.server.svc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ca.wimsc.client.common.model.Route;
import ca.wimsc.client.common.model.RouteList;
import ca.wimsc.client.common.model.Stop;
import ca.wimsc.client.common.model.StopList;
import ca.wimsc.client.common.model.StreetcarLocation;
import ca.wimsc.client.common.model.StreetcarLocationList;
import ca.wimsc.client.common.rpc.FailureException;
import ca.wimsc.server.jpa.PersistedIncident;
import ca.wimsc.server.jpa.PersistedRoute;
import ca.wimsc.server.jpa.PersistedVehicle;
import ca.wimsc.server.util.VehiclePositionAnalyzer;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class ActivityMonitorServiceTest {

	private static final String DIRECTION_2 = "DIRECTION_2";
	private static final String DIRECTION_1 = "DIRECTION_1";
	private static final String VEHICLE_1 = "VEHICLE_1";
	private static final String ROUTE_TAG_1 = "ROUTE_1";
	private ActivityMonitorServiceImpl mySvc;
	private INextbusFetcherService myFetcherSvc;
	private EntityManager myEntityManager;
	private SimpleDateFormat myTimeFormat = new SimpleDateFormat("HH:mm");
	private SimpleDateFormat myDateTimeFormat = new SimpleDateFormat("MMM-dd HH:mm");
	private final LocalServiceTestHelper myTestHelper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	private static EntityManagerFactory ourEmf;
	private StreetcarLocation myVehicle1Location;
	private StreetcarLocation myVehicle2Location;
	private ArrayList<StreetcarLocation> myStreetcarLocations;
	private Stop myStopQueenDovercourt;
	private Stop myStopQueenPeter;
	private Stop myStopQueenShaw;
	private Stop myStopQueenPeter2;


	@Before
	public void setUp() throws IOException, FailureException {

		myTestHelper.setEnvAppId("env.app.id");
		myTestHelper.setUp();

		myFetcherSvc = mock(INextbusFetcherService.class);

		if (ourEmf == null) {
			ourEmf = Persistence.createEntityManagerFactory("transactions-optional");
		}

		EntityManager em = ourEmf.createEntityManager();
		em.createQuery("DELETE FROM ca.wimsc.server.jpa.PersistedRoute").executeUpdate();
		em.createQuery("DELETE FROM ca.wimsc.server.jpa.PersistedIncident").executeUpdate();
		em.close();

		mySvc = new ActivityMonitorServiceImpl(myFetcherSvc, ourEmf);

		RouteList routeList = new RouteList();
		routeList.setList(new ArrayList<Route>());
		Route route = new Route();
		route.setTag(ROUTE_TAG_1);
		routeList.getList().add(route);
		when(myFetcherSvc.loadRouteList()).thenReturn(routeList);

		HashMap<String, StopList> hashMap = new HashMap<String, StopList>();
		StopList stopListDirection1 = new StopList();
//		stopListDirection1.setStops(new ArrayList<Stop>());

		myStopQueenDovercourt = new Stop();
		myStopQueenDovercourt.setLatitude(43.64331d);
		myStopQueenDovercourt.setLatitude(-79.42223d);
		myStopQueenDovercourt.setStopTag("queen_and_dovercourt");
		myStopQueenDovercourt.setTitle("Dovercourt At Queen");
		stopListDirection1.getStops().add(myStopQueenDovercourt);

		myStopQueenShaw = new Stop();
		myStopQueenShaw.setLatitude(43.64463d);
		myStopQueenShaw.setLatitude(-79.41617d);
		myStopQueenShaw.setStopTag("queen_and_shaw");
		myStopQueenShaw.setTitle("Shaw At Queen");
		stopListDirection1.getStops().add(myStopQueenShaw);

		myStopQueenPeter = new Stop();
		myStopQueenPeter.setLatitude(43.64911d);
		myStopQueenPeter.setLatitude(-79.39309d);
		myStopQueenPeter.setStopTag("queen_and_pater");
		myStopQueenPeter.setTitle("Queen And Peter");
		stopListDirection1.getStops().add(myStopQueenPeter);

		// Stop stop = new Stop();
		// stop.setLatitude(43.64331d);
		// stop.setLatitude(-79.42223d);
		// stop.setStopTag("queen_and_dovercourt");
		// stop.setTitle("Dovercourt At Queen");
		// stopList.getStops().add(stop);

		hashMap.put(DIRECTION_1, stopListDirection1);

		StopList stopListDirection2 = new StopList();
//		stopListDirection2.setStops(new ArrayList<Stop>());
		hashMap.put(DIRECTION_2, stopListDirection2);

		myStopQueenPeter2 = new Stop();
		myStopQueenPeter2.setLatitude(43.64911d);
		myStopQueenPeter2.setLatitude(-79.39309d);
		myStopQueenPeter2.setStopTag("queen_and_pater_d2");
		myStopQueenPeter2.setTitle("Queen And Peter");
		stopListDirection2.getStops().add(myStopQueenPeter2);
		
//		when(myFetcherSvc.loadDirectionTagToStopList(ROUTE_TAG_1)).thenReturn(hashMap);

		StreetcarLocationList streetcarLocationList1 = new StreetcarLocationList();
		myStreetcarLocations = new ArrayList<StreetcarLocation>();

		myVehicle1Location = new StreetcarLocation();
		myVehicle1Location.setDirectionTag(DIRECTION_1);
		myVehicle1Location.setVehicleTag(VEHICLE_1);
		myStreetcarLocations.add(myVehicle1Location);

		myVehicle2Location = new StreetcarLocation();
		myVehicle2Location.setDirectionTag(DIRECTION_2);
		myVehicle2Location.setVehicleTag("VEHICLE_2");
		myStreetcarLocations.add(myVehicle2Location);

		streetcarLocationList1.setLocations(myStreetcarLocations);
		when(myFetcherSvc.loadStreetcarLocations(ROUTE_TAG_1, false, false)).thenReturn(streetcarLocationList1);

	}


	@Test
	public void testPurge() throws ParseException, FailureException {
		EntityManager em = ourEmf.createEntityManager();

		PersistedIncident incident1 = new PersistedIncident();
		incident1.setKey(1L);
		incident1.setStartDate(myDateTimeFormat.parse("Jan-01 00:00"));
		em.merge(incident1);
		em.close();

		em = ourEmf.createEntityManager();

		PersistedIncident incident2 = new PersistedIncident();
		incident2.setKey(2L);
		incident2.setStartDate(myDateTimeFormat.parse("Jan-07 00:00"));
		em.merge(incident2);
		em.close();

		em = ourEmf.createEntityManager();
		Assert.assertEquals(2, em.createQuery("SELECT FROM " + PersistedIncident.class.getName()).getResultList().size());
		em.close();

		mySvc.setHardcodeTime(myDateTimeFormat.parse("Jan-08 00:01").getTime());
		mySvc.purgeIncidents();

		em = ourEmf.createEntityManager();
		Assert.assertEquals(1, em.createQuery("SELECT FROM " + PersistedIncident.class.getName()).getResultList().size());
		em.close();

		mySvc.setHardcodeTime(myDateTimeFormat.parse("Jan-15 00:01").getTime());
		mySvc.purgeIncidents();

		em = ourEmf.createEntityManager();
		Assert.assertEquals(0, em.createQuery("SELECT FROM " + PersistedIncident.class.getName()).getResultList().size());
		em.close();
	}


	@Test
	public void testDetectNotMovingIncident() throws FailureException, ParseException {

		// Eastbound on Queen /Dovercourt
		myVehicle1Location.setLatitude(43.64331d);
		myVehicle1Location.setLatitude(-79.42223d);

		myVehicle2Location.setLatitude(41.64701d);
		myVehicle2Location.setLatitude(-79.40412d);

		mySvc.setHardcodeTime(myTimeFormat.parse("11:45").getTime());
		mySvc.updateVehicles();

		// Eastbound on Queen just before Shaw
		myVehicle1Location.setLatitude(43.64463d);
		myVehicle1Location.setLatitude(-79.41617d);

		myVehicle2Location.setLatitude(41.64701d);
		myVehicle2Location.setLatitude(-79.40412d);

		mySvc.setHardcodeTime(myTimeFormat.parse("11:50").getTime());
		mySvc.updateVehicles();

		// Eastbound on Queen just before Bellwood
		myVehicle1Location.setLatitude(43.64561d);
		myVehicle1Location.setLatitude(-79.41139d);

		myVehicle2Location.setLatitude(42.64701d);
		myVehicle2Location.setLatitude(-79.40412d);

		mySvc.setHardcodeTime(myTimeFormat.parse("11:55").getTime());
		mySvc.updateVehicles();

		// Eastbound on Queen just before Bathurst
		myVehicle1Location.setLatitude(43.64701d);
		myVehicle1Location.setLatitude(-79.40412d);

		myVehicle2Location.setLatitude(43.64701d);
		myVehicle2Location.setLatitude(-79.40412d);

		mySvc.setHardcodeTime(myTimeFormat.parse("12:00").getTime());
		mySvc.updateVehicles();

		// Eastbound on Queen just after Bathurst
		myVehicle1Location.setLatitude(43.64716d);
		myVehicle1Location.setLatitude(-79.40369d);

		myVehicle2Location.setLatitude(44.64701d);
		myVehicle2Location.setLatitude(-79.40412d);

		mySvc.setHardcodeTime(myTimeFormat.parse("12:05").getTime());
		mySvc.updateVehicles();

		myEntityManager = ourEmf.createEntityManager();
		PersistedVehicle vehicle1 = myEntityManager.find(PersistedRoute.class, ROUTE_TAG_1).getVehicle(VEHICLE_1);

		Assert.assertNull(vehicle1.getMostRecentIncidentId());
		Assert.assertFalse(vehicle1.isMostRecentIncidentActive());

		// Eastbound on Queen just after Bathurst
		myVehicle1Location.setLatitude(43.64716d);
		myVehicle1Location.setLatitude(-79.40369d);

		myVehicle2Location.setLatitude(45.64701d);
		myVehicle2Location.setLatitude(-79.40412d);

		mySvc.setHardcodeTime(myTimeFormat.parse("12:10").getTime());
		mySvc.updateVehicles();

		myEntityManager = ourEmf.createEntityManager();
		vehicle1 = myEntityManager.find(PersistedRoute.class, ROUTE_TAG_1).getVehicle(VEHICLE_1);

		Assert.assertNotNull(vehicle1.getMostRecentIncidentId());
		Assert.assertTrue(vehicle1.isMostRecentIncidentActive());

		// Eastbound on Queen at University
		myVehicle1Location.setLatitude(43.65080d);
		myVehicle1Location.setLatitude(-79.38654d);

		myVehicle2Location.setLatitude(46.64701d);
		myVehicle2Location.setLatitude(-79.40412d);

		mySvc.setHardcodeTime(myTimeFormat.parse("12:15").getTime());
		mySvc.updateVehicles();

		myEntityManager = ourEmf.createEntityManager();
		vehicle1 = myEntityManager.find(PersistedRoute.class, ROUTE_TAG_1).getVehicle(VEHICLE_1);
		Assert.assertFalse(vehicle1.isMostRecentIncidentActive());

	}


	/**
	 * If a vehicle disappears from a route, and there is an open incident for it, the incident should auto close
	 */
	@Test
	public void testNotMovingIncidentClearsIfVehicleDisappears() throws ParseException, FailureException {

		// Eastbound on Queen just before Shaw
		myVehicle1Location.setLatitude(43.64463d);
		myVehicle1Location.setLatitude(-79.41617d);

		myVehicle2Location.setLatitude(41.64701d);
		myVehicle2Location.setLatitude(-79.40412d);

		mySvc.setHardcodeTime(myTimeFormat.parse("11:50").getTime());
		mySvc.updateVehicles();

		// Eastbound on Queen just before Bellwood
		myVehicle1Location.setLatitude(43.64561d);
		myVehicle1Location.setLatitude(-79.41139d);

		myVehicle2Location.setLatitude(42.64701d);
		myVehicle2Location.setLatitude(-79.40412d);

		mySvc.setHardcodeTime(myTimeFormat.parse("11:55").getTime());
		mySvc.updateVehicles();

		// Eastbound on Queen just before Bathurst
		myVehicle1Location.setLatitude(43.64701d);
		myVehicle1Location.setLatitude(-79.40412d);

		myVehicle2Location.setLatitude(43.64701d);
		myVehicle2Location.setLatitude(-79.40412d);

		mySvc.setHardcodeTime(myTimeFormat.parse("12:00").getTime());
		mySvc.updateVehicles();

		// Eastbound on Queen just after Bathurst
		myVehicle1Location.setLatitude(43.64716d);
		myVehicle1Location.setLatitude(-79.40369d);

		myVehicle2Location.setLatitude(44.64701d);
		myVehicle2Location.setLatitude(-79.40412d);

		mySvc.setHardcodeTime(myTimeFormat.parse("12:05").getTime());
		mySvc.updateVehicles();

		myEntityManager = ourEmf.createEntityManager();
		PersistedVehicle vehicle1 = myEntityManager.find(PersistedRoute.class, ROUTE_TAG_1).getVehicle(VEHICLE_1);

		Assert.assertNull(vehicle1.getMostRecentIncidentId());
		Assert.assertFalse(vehicle1.isMostRecentIncidentActive());

		// Eastbound on Queen just after Bathurst
		myVehicle1Location.setLatitude(43.64716d);
		myVehicle1Location.setLatitude(-79.40369d);

		myVehicle2Location.setLatitude(45.64701d);
		myVehicle2Location.setLatitude(-79.40412d);

		mySvc.setHardcodeTime(myTimeFormat.parse("12:10").getTime());
		mySvc.updateVehicles();

		myEntityManager = ourEmf.createEntityManager();
		vehicle1 = myEntityManager.find(PersistedRoute.class, ROUTE_TAG_1).getVehicle(VEHICLE_1);

		Long incidentId = vehicle1.getMostRecentIncidentId();
		Assert.assertNotNull(incidentId);
		Assert.assertTrue(vehicle1.isMostRecentIncidentActive());

		PersistedIncident incident = myEntityManager.find(PersistedIncident.class, incidentId);
		Assert.assertNull(incident.getEndDate());

		// Now, make vehicle 1 disappear!
		myStreetcarLocations.remove(myVehicle1Location);
		mySvc.setHardcodeTime(myTimeFormat.parse("12:15").getTime());
		mySvc.updateVehicles();

		myEntityManager.close();
		myEntityManager = ourEmf.createEntityManager();

		incident = myEntityManager.find(PersistedIncident.class, incidentId);
		Assert.assertNotNull(incident.getEndDate());

	}


	@SuppressWarnings("unchecked")
	@Test
	public void testDetectDiversion() throws ParseException, FailureException {
		myStreetcarLocations.remove(myVehicle2Location);

		// Eastbound on Queen /Dovercourt
		myVehicle1Location.setDirectionTag(DIRECTION_1);
		myVehicle1Location.setClosestStopTag(myStopQueenDovercourt.getStopTag());
		myVehicle1Location.setLatitude(43.64331d);
		myVehicle1Location.setLongitude(-79.42223d);

		mySvc.setHardcodeTime(myTimeFormat.parse("12:00").getTime());
		mySvc.updateVehicles();

		// Eastbound on King at Spadina
		myVehicle1Location.setDirectionTag(VehiclePositionAnalyzer.NULL_DIR);
		myVehicle1Location.setClosestStopTag("xxx");
		myVehicle1Location.setLatitude(43.64523d);
		myVehicle1Location.setLongitude(-79.39485d);

		mySvc.setHardcodeTime(myTimeFormat.parse("12:05").getTime());
		mySvc.updateVehicles();

		// Eastbound on Queen at Peter
		myVehicle1Location.setDirectionTag(DIRECTION_1);
		myVehicle1Location.setClosestStopTag(myStopQueenPeter.getStopTag());
		myVehicle1Location.setLatitude(43.64911d);
		myVehicle1Location.setLongitude(-79.39309d);

		mySvc.setHardcodeTime(myTimeFormat.parse("12:10").getTime());
		mySvc.updateVehicles();

		// Check that a diversion incident has been created
		EntityManager em = ourEmf.createEntityManager();
		Query query = em.createQuery("SELECT FROM " + PersistedIncident.class.getName());
		List<PersistedIncident> resultList = query.getResultList();
		PersistedIncident incident = resultList.get(0);
		Assert.assertEquals(myStopQueenDovercourt.getStopTag(), incident.getBeginNearestStopTag());
		Assert.assertEquals(myStopQueenPeter.getStopTag(), incident.getEndNearestStopTag());
		Assert.assertEquals(myTimeFormat.parse("12:00"), incident.getStartDate());
		Assert.assertEquals(null, incident.getEndDate());
		em.close();

		/*
		 * Now, have a second vehicle pass through closer to the diversion point, to make sure the bounds tighten
		 */
		myStreetcarLocations.remove(myVehicle1Location);
		myStreetcarLocations.add(myVehicle2Location);
		myVehicle2Location.setDirectionTag(DIRECTION_1);

		// Eastbound on Queen just before Shaw
		myVehicle2Location.setLatitude(myStopQueenShaw.getLatitude());
		myVehicle2Location.setLatitude(myStopQueenShaw.getLongitude());
		myVehicle2Location.setDirectionTag(DIRECTION_1);
		myVehicle2Location.setClosestStopTag(myStopQueenShaw.getStopTag());
		mySvc.setHardcodeTime(myTimeFormat.parse("12:15").getTime());
		mySvc.updateVehicles();

		// Eastbound on King at Spadina
		myVehicle2Location.setDirectionTag(VehiclePositionAnalyzer.NULL_DIR);
		myVehicle2Location.setClosestStopTag("xxx");
		myVehicle2Location.setLatitude(43.64523d);
		myVehicle2Location.setLongitude(-79.39485d);

		mySvc.setHardcodeTime(myTimeFormat.parse("12:20").getTime());
		mySvc.updateVehicles();

		// Eastbound on Queen at Peter
		myVehicle2Location.setDirectionTag(DIRECTION_1);
		myVehicle2Location.setClosestStopTag(myStopQueenPeter.getStopTag());
		myVehicle2Location.setLatitude(43.64911d);
		myVehicle2Location.setLongitude(-79.39309d);

		mySvc.setHardcodeTime(myTimeFormat.parse("12:25").getTime());
		mySvc.updateVehicles();

		em = ourEmf.createEntityManager();
		query = em.createQuery("SELECT FROM " + PersistedIncident.class.getName());
		resultList = query.getResultList();
		Assert.assertEquals(1, resultList.size());
		incident = resultList.get(0);
		Assert.assertEquals(myStopQueenShaw.getStopTag(), incident.getBeginNearestStopTag());
		Assert.assertEquals(myStopQueenPeter.getStopTag(), incident.getEndNearestStopTag());
		Assert.assertEquals(myTimeFormat.parse("12:00"), incident.getStartDate());
		Assert.assertEquals(null, incident.getEndDate());
		em.close();

	}

}
