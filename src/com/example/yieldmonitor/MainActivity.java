package com.example.yieldmonitor;

import java.util.ArrayList;
//import java.util.List;
import java.lang.String;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.GoogleMap.*;

import static java.lang.Math.*;

public class MainActivity extends FragmentActivity {
	
	static final double EARTH_RADIUS = 6371009;
	static TextView AreaTextView;
	
	GoogleMap googleMap;
    ArrayList<LatLng> points; //Vertices of the polygon to be plotted
    ArrayList<Polyline> polylines; //Lines of the polygon (required for clearing)
    Marker iMarker, fMarker, inMarker; //Initial and final markers
    boolean longClickClear;
    PolylineOptions previewPolylineOptions;
    Polyline previewPolyline;
    LatLng overlayLocation = new LatLng(40.432923, -86.918481);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        points = new ArrayList<LatLng>();
        polylines = new ArrayList<Polyline>();
        GroundOverlayOptions overlay = new GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.yield_overlay))
        		.position(overlayLocation, 860f, 650f).transparency(0.5f);

        //Getting reference to the SupportMapFragment of activity_main.xml
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap = fm.getMap(); //Getting GoogleMap object from the fragment    
        googleMap.addGroundOverlay(overlay);        
        googleMap.setMyLocationEnabled(true); //Enabling MyLocation Layer of Google Map
        
        AreaTextView = (TextView) findViewById(R.id.AreaView);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(overlayLocation, 13));

        googleMap.setOnMapClickListener(new OnMapClickListener() { //Setting OnClick event listener for the Google Map
        	

            @Override
            public void onMapClick(LatLng point) {
            	if (points.size() == 0) {
            		// Instantiating the class MarkerOptions to plot marker on the map
            		MarkerOptions iMarkerOptions = new MarkerOptions(); MarkerOptions fMarkerOptions = new MarkerOptions();
            		
            		Toast.makeText(getApplicationContext(), "Press and hold data marker to drag.", Toast.LENGTH_SHORT).show();

            		// Setting latitude and longitude of the marker position
            		iMarkerOptions.position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker));
        			fMarkerOptions.position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker));

            		// Setting title of the infowindow of the marker
            		iMarkerOptions.title("Position"); fMarkerOptions.title("Position");

            		// Setting the content of the infowindow of the marker
            		iMarkerOptions.snippet("Latitude:"+point.latitude+","+"Longitude:"+point.longitude); 
            		fMarkerOptions.snippet("Latitude:"+point.latitude+","+"Longitude:"+point.longitude);

            		// Adding the tapped point to the ArrayList
            		points.add(point);

            		// Adding the marker to the map
            		iMarker = googleMap.addMarker(iMarkerOptions); fMarker = googleMap.addMarker(fMarkerOptions);
            		iMarker.setDraggable(false); fMarker.setDraggable(true);
            		longClickClear = false;
            	}
            	else if (points.size() > 0) {
            		if (inMarker != null)
            			inMarker.remove();
            		MarkerOptions inMarkerOptions = new MarkerOptions();
            		inMarkerOptions.position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_marker));
            		boolean inside = containsLocation(point, points, true);
            		Toast.makeText(getApplicationContext(), String.format("%s", Boolean.toString(inside)), Toast.LENGTH_SHORT).show();
            		inMarker = googleMap.addMarker(inMarkerOptions);
            	}
            }
        });
        
        googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                if (longClickClear == true) {
                	//Clearing the markers and polylines in the google map
                	iMarker.remove(); fMarker.remove();
                	for(Polyline line: polylines) {
                		line.remove();
                	}
                	polylines.clear();
                	Toast.makeText(getApplicationContext(), "Polygon cleared. Tap to create another marker.", Toast.LENGTH_SHORT).show();

                	// Empty the array list
                	points.clear();
                }
            }
        });
        
        googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				if (marker.equals(iMarker)) { //Initial marker clicked
					points.add(marker.getPosition()); //Current position of movable marker added to list 
					//Polyline added to map and to ArrayList polylines
					polylines.add(googleMap.addPolyline(new PolylineOptions().color(Color.BLUE).width(3).addAll(points)));
					Toast.makeText(getApplicationContext(), "Long press to clear polygon.", Toast.LENGTH_SHORT).show();
			    	longClickClear = true;
				}
				return true;
			}
        });
        
        googleMap.setOnMarkerDragListener(new OnMarkerDragListener() {
        	@Override
        	public void onMarkerDragEnd(Marker marker) {
        		previewPolyline.remove();
            	LatLng curr = marker.getPosition(); //Gets current marker position
            	points.add(curr);
            	//Polyline added to map and to ArrayList polylines
            	polylines.add(googleMap.addPolyline(new PolylineOptions().color(Color.RED).width(3).addAll(points)));
        	}

			@Override
			public void onMarkerDrag(Marker marker) {
				LatLng curr;
				if (previewPolyline == null) {
					curr = marker.getPosition();
					previewPolylineOptions = new PolylineOptions().color(Color.RED).width(3).add(points.get(points.size() - 1), curr);
					previewPolyline = googleMap.addPolyline(previewPolylineOptions);
				}
				else {
					previewPolyline.remove();
					curr = marker.getPosition();
					previewPolylineOptions = new PolylineOptions().color(Color.RED).width(3).add(points.get(points.size() - 1), curr);
					previewPolyline = googleMap.addPolyline(previewPolylineOptions);
				}
			}

			@Override
			public void onMarkerDragStart(Marker marker) {
			}
        });
        
        googleMap.setOnMyLocationButtonClickListener(new OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
            	Toast.makeText(getApplicationContext(), "Tap to create marker.", Toast.LENGTH_SHORT).show();;
                return false;
            }
        
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /*Inflate the menu; this adds items to the action bar if it is present.*/
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case R.id.action_undo:
    	  if (polylines.size() > 0) {
    		  polylines.remove(polylines.size() - 1).remove(); //Removes element from array and corresponding polyline from map
    		  points.remove(points.size() - 1);
    		  fMarker.setPosition(points.get(points.size() - 1));
    		  AreaTextView.setText("");
    	  }
    	  else if (polylines.size() == 0) {
    		  if (points.size() > 0) {
    			  points.remove(points.size() - 1);
        		  iMarker.remove(); fMarker.remove();
        		  AreaTextView.setText("");
    		  }
    		  else{
    			  AreaTextView.setText("");
    		  }
    	  }
    	  break;
    	  
      case R.id.action_dial_pad:
    	  double area = computeArea(points);
    	  if (area == -1) {
    		  Toast.makeText(this, "Complete polygon to compute area.", Toast.LENGTH_SHORT).show();
    	  }
    	  else {
    		  Toast.makeText(this, String.format("Area: %.2e sq meters", area), Toast.LENGTH_LONG).show();
    		  AreaTextView.setText( String.format("%.2e sq meters", area));
    	  }
    	  break;
    	  
    	  
      case R.id.action_settings: // action with ID action_settings was selected
        Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT)
            .show();
        break;
        
      default:
        break;
      }

      return true;
    }
    
    /**
     * Returns the area of a closed path on Earth.
     * @param path A closed path.
     * @return The path's area in square meters.
     */
    public static double computeArea(ArrayList<LatLng> path) {
    	if (!path.get(0).equals(path.get(path.size() - 1))) { //path not closed
    		return -1;
    	}
        return abs(computeSignedArea(path));
    }

    /**
     * Returns the signed area of a closed path on Earth. The sign of the area may be used to
     * determine the orientation of the path.
     * "inside" is the surface that does not contain the South Pole.
     * @param path A closed path.
     * @return The loop's area in square meters.
     */
    public static double computeSignedArea(ArrayList<LatLng> path) {
        return computeSignedArea(path, EARTH_RADIUS);
    }

    /**
     * Returns the signed area of a closed path on a sphere of given radius.
     * The computed area uses the same units as the radius squared.
     * Used by SphericalUtilTest.
     */
    static double computeSignedArea(ArrayList<LatLng> path, double radius) {
        int size = path.size();
        if (size < 3) { return 0; }
        double total = 0;
        LatLng prev = path.get(size - 1);
        double prevTanLat = tan((PI / 2 - toRadians(prev.latitude)) / 2);
        double prevLng = toRadians(prev.longitude);
        // For each edge, accumulate the signed area of the triangle formed by the North Pole
        // and that edge ("polar triangle").
        for (LatLng point : path) {
            double tanLat = tan((PI / 2 - toRadians(point.latitude)) / 2);
            double lng = toRadians(point.longitude);
            total += polarTriangleArea(tanLat, lng, prevTanLat, prevLng);
            prevTanLat = tanLat;
            prevLng = lng;
        }
        return total * (radius * radius);
    }

    /**
     * Returns the signed area of a triangle which has North Pole as a vertex.
     * Formula derived from "Area of a spherical triangle given two edges and the included angle"
     * as per "Spherical Trigonometry" by Todhunter, page 71, section 103, point 2.
     * See http://books.google.com/books?id=3uBHAAAAIAAJ&pg=PA71
     * The arguments named "tan" are tan((pi/2 - latitude)/2).
     */
    private static double polarTriangleArea(double tan1, double lng1, double tan2, double lng2) {
        double deltaLng = lng1 - lng2;
        double t = tan1 * tan2;
        return 2 * atan2(t * sin(deltaLng), 1 + t * cos(deltaLng));
    }
    
    /**
     * Computes whether the given point lies inside the specified polygon.
     * The polygon is always cosidered closed, regardless of whether the last point equals
     * the first or not.
     * Inside is defined as not containing the South Pole -- the South Pole is always outside.
     * The polygon is formed of great circle segments if geodesic is true, and of rhumb
     * (loxodromic) segments otherwise.
     */
    public static boolean containsLocation(LatLng point, ArrayList<LatLng> polygon, boolean geodesic) {
        final int size = polygon.size();
        if (size == 0) {
            return false;
        }
        double lat3 = toRadians(point.latitude);
        double lng3 = toRadians(point.longitude);
        LatLng prev = polygon.get(size - 1);
        double lat1 = toRadians(prev.latitude);
        double lng1 = toRadians(prev.longitude);
        int nIntersect = 0;
        for (LatLng point2 : polygon) {
            double dLng3 = wrap(lng3 - lng1, -PI, PI);
            // Special case: point equal to vertex is inside.
            if (lat3 == lat1 && dLng3 == 0) {
                return true;
            }
            double lat2 = toRadians(point2.latitude);
            double lng2 = toRadians(point2.longitude);
            // Offset longitudes by -lng1.
            if (intersects(lat1, lat2, wrap(lng2 - lng1, -PI, PI), lat3, dLng3, geodesic)) {
                ++nIntersect;
            }
            lat1 = lat2;
            lng1 = lng2;
        }
        return (nIntersect & 1) != 0;
    }
    
    /**
     * Wraps the given value into the inclusive-exclusive interval between min and max.
     * @param n   The value to wrap.
     * @param min The minimum.
     * @param max The maximum.
     */
    static double wrap(double n, double min, double max) {
        return (n >= min && n < max) ? n : (mod(n - min, max - min) + min);
    }
    
    /**
     * Returns the non-negative remainder of x / m.
     * @param x The operand.
     * @param m The modulus.
     */
    static double mod(double x, double m) {
        return ((x % m) + m) % m;
    }
    
    /**
     * Computes whether the vertical segment (lat3, lng3) to South Pole intersects the segment
     * (lat1, lng1) to (lat2, lng2).
     * Longitudes are offset by -lng1; the implicit lng1 becomes 0.
     */
    private static boolean intersects(double lat1, double lat2, double lng2,
                                      double lat3, double lng3, boolean geodesic) {
        // Both ends on the same side of lng3.
        if ((lng3 >= 0 && lng3 >= lng2) || (lng3 < 0 && lng3 < lng2)) {
            return false;
        }
        // Point is South Pole.
        if (lat3 <= -PI/2) {
            return false;
        }
        // Any segment end is a pole.
        if (lat1 <= -PI/2 || lat2 <= -PI/2 || lat1 >= PI/2 || lat2 >= PI/2) {
            return false;
        }
        if (lng2 <= -PI) {
            return false;
        }
        double linearLat = (lat1 * (lng2 - lng3) + lat2 * lng3) / lng2;
        // Northern hemisphere and point under lat-lng line.
        if (lat1 >= 0 && lat2 >= 0 && lat3 < linearLat) {
            return false;
        }
        // Southern hemisphere and point above lat-lng line.
        if (lat1 <= 0 && lat2 <= 0 && lat3 >= linearLat) {
            return true;
        }
        // North Pole.
        if (lat3 >= PI/2) {
            return true;
        }
        // Compare lat3 with latitude on the GC/Rhumb segment corresponding to lng3.
        // Compare through a strictly-increasing function (tan() or mercator()) as convenient.
        return geodesic ?
            tan(lat3) >= tanLatGC(lat1, lat2, lng2, lng3) :
            mercator(lat3) >= mercatorLatRhumb(lat1, lat2, lng2, lng3);
    }
    
    /**
     * Returns mercator Y corresponding to latitude.
     * See http://en.wikipedia.org/wiki/Mercator_projection .
     */
    static double mercator(double lat) {
        return log(tan(lat * 0.5 + PI/4));
    }
    
    /**
     * Returns tan(latitude-at-lng3) on the great circle (lat1, lng1) to (lat2, lng2). lng1==0.
     * See http://williams.best.vwh.net/avform.htm .
     */
    private static double tanLatGC(double lat1, double lat2, double lng2, double lng3) {
        return (tan(lat1) * sin(lng2 - lng3) + tan(lat2) * sin(lng3)) / sin(lng2);
    }
    
    /**
     * Returns mercator(latitude-at-lng3) on the Rhumb line (lat1, lng1) to (lat2, lng2). lng1==0.
     */        
    private static double mercatorLatRhumb(double lat1, double lat2, double lng2, double lng3) {
        return (mercator(lat1) * (lng2 - lng3) + mercator(lat2) * lng3) / lng2;
    }

}

