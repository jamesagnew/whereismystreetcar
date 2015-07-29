package ca.wimsc.server.svc;

import java.io.IOException;

public class ServiceFactory {

    private static ServiceFactory ourInstance;
    private IActivityMonitorService myActivityMonitorService;
    private INextbusFetcherService myNextbusFetherService;
	private ITwitterService myTwitterService;

    public static synchronized ServiceFactory getInstance() throws IOException {
        if (ourInstance == null) {
            ourInstance = new ServiceFactory();
        }
        return ourInstance;
    }
    
    private ServiceFactory() throws IOException {
        
        myActivityMonitorService = new ActivityMonitorServiceImpl();
        myNextbusFetherService = new NextbusFetcherServiceImpl();
        
        // Little circular dependency here.. this needs to be cleaned up
        ((ActivityMonitorServiceImpl) myActivityMonitorService).setNextbusFetcherService(myNextbusFetherService);
        ((NextbusFetcherServiceImpl) myNextbusFetherService).setActivityMonitorService(myActivityMonitorService);
        
        myTwitterService = new TwitterService();
    }

    public IActivityMonitorService getActivityMonitorService() {
        return myActivityMonitorService;
    }

    public INextbusFetcherService getNextbusFetcherService() {
        return myNextbusFetherService;
    }

	public ITwitterService getTwitterService() {
		return myTwitterService;
	}
    
}
