package ca.wimsc.client.common.util;

public class Vehicles {

    public static String getVehicleLink(String theVehicleTag) {
        if (theVehicleTag == null) {
            return null;
        }
        
        if (theVehicleTag.startsWith("40") || theVehicleTag.startsWith("41")) {
            return "<a href='http://en.wikipedia.org/wiki/Canadian_Light_Rail_Vehicle' class='vehicleLink'>CLRV</a>";
        }

        if (theVehicleTag.startsWith("42")) {
            return "<a href='http://en.wikipedia.org/wiki/Articulated_Light_Rail_Vehicle' class='vehicleLink'>ALRV</a>";
        }
        
        if (theVehicleTag.startsWith("45")) {
            return "<a href='http://en.wikipedia.org/wiki/PCC_streetcar' class='vehicleLink'>PCC</a>";
        }

        return "Bus";
    }
    
    public static VehicleTypeEnum getVehicleType(String theVehicleTag) {
    	if (theVehicleTag == null) {
            return VehicleTypeEnum.BUS;
        }
        
        if (theVehicleTag.startsWith("40") || theVehicleTag.startsWith("41")) {
            return VehicleTypeEnum.STREETCAR;
        }

        if (theVehicleTag.startsWith("42")) {
            return VehicleTypeEnum.STREETCAR;
        }
        
        if (theVehicleTag.startsWith("45")) {
            return VehicleTypeEnum.STREETCAR;
        }

        return VehicleTypeEnum.BUS;
    }
    
    public enum VehicleTypeEnum 
    {
        STREETCAR, BUS
    }
    
}
