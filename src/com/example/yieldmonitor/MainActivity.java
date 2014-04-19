package com.example.yieldmonitor;

import java.util.ArrayList;
//import java.util.HashMap;
import java.lang.String;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.GoogleMap.*;

import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;



public class MainActivity extends FragmentActivity {
	
	static final double EARTH_RADIUS = 6371009;
	private static final String TAG = "MyActivity";
	private static final String Value = "PIXEL";
	
	
	
	GoogleMap googleMap;
    ArrayList<LatLng> points; //Vertices of the polygon to be plotted
    ArrayList<Polyline> polylines; //Lines of the polygon (required for clearing)
    Marker iMarker, fMarker, inMarker; //Initial and final markers
    boolean longClickClear;
    PolylineOptions previewPolylineOptions;
    Polyline previewPolyline;
    LatLng overlayLocation = new LatLng(40.432923, -86.918481);
    LatLng image_test = new LatLng(40.42262549999998, -86.92454150000002); 
    
    TextView AreaTextView;
    TextView AverageText;
    TextView VarianceText;
    TextView Std_DevText;
    TextView ModeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        

        points = new ArrayList<LatLng>();
        polylines = new ArrayList<Polyline>();
        GroundOverlayOptions overlay = new GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.yield_overlay)).anchor(0, 0);
        overlay.position(overlayLocation, 860f, 650f);
        
        //Getting reference to the SupportMapFragment of activity_main.xml
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap = fm.getMap(); //Getting GoogleMap object from the fragment    
        googleMap.addGroundOverlay(overlay);        
        googleMap.setMyLocationEnabled(true); //Enabling MyLocation Layer of Google Map
        
        AreaTextView = (TextView)findViewById(R.id.areaValue);
        AverageText = (TextView)findViewById(R.id.average);
        Std_DevText = (TextView)findViewById(R.id.standard_deviation);
        VarianceText = (TextView)findViewById(R.id.variance);
        ModeText = (TextView)findViewById(R.id.mode);
        
        
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(image_test, 13));      
        
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

            		boolean inside = PolyUtil.containsLocation(point, points, true);

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
                	AreaTextView.setText("");
                	AverageText.setText("");
                	Std_DevText.setText("");
           	        VarianceText.setText("");
           	        //pixels.fill(coun)
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
        getMenuInflater().inflate(R.menu.tool, menu);

    	//MenuInflater inflater = getMenuInflater();
    	//inflater.inflate(R.menu.tool, menu);
    
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
    	     AverageText.setText("");
    	     Std_DevText.setText("");
    	     VarianceText.setText("");
    	     
    	 } else if (polylines.size() == 0) {
    		 if (points.size() > 0) {
    		 points.remove(points.size() - 1);
             iMarker.remove(); fMarker.remove();
             AreaTextView.setText("");
             AverageText.setText("");
             Std_DevText.setText("");
             VarianceText.setText("");
    	  }
    	}
    	break;
    	
    	
      case R.id.action_area:
    	double area = SphericalUtil.computeArea(points);
    	if (area != 0 && points.get(0).equals(points.get(points.size() - 1))) {
    		//Toast.makeText(this, String.format("Area: %.2e sq meters", area), Toast.LENGTH_LONG).show();
    		double arces;
    		arces = area*0.000247;

    		AreaTextView.setText( String.format("Area: %.2e sq meters || %.2e acres" ,area,arces));
    	} else {
    		Toast.makeText(this, "Complete polygon to compute area.", Toast.LENGTH_SHORT).show();
    	}
    	break;
    	
      case R.id.average:
    	  if (polylines.size() >= 3){
    		  double counter[] = new double[3];
    	      counter = t_Statistics();
    	      double average_2 = counter[0];
    	      
    	      AverageText.setText(String.format("Average: %.2e", average_2));

    	  }

    	  
      	  break;
      	   
      case R.id.standard_deviation:
     	  if (polylines.size() >= 3){
     		  double counter_3[] = new double[3];
     		  counter_3 = t_Statistics();
     		  double std_dev2  = counter_3[2];
     		
     		  Std_DevText.setText(String.format("Standard Deviation: %.2e ",std_dev2));
     	  }
    	  
    	  break;
    	  
    	  
      case R.id.variance:
    	  if (polylines.size() >= 3){
    		  
    		  double counter_2[] = new double[3];
    		  counter_2 = t_Statistics();
    		  double variance_2 = counter_2[1];

    		  VarianceText.setText(String.format("Variance: %.2e ",variance_2));
    		  
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
    

  //Does all of the calculations
      double[] t_Statistics(){
          int[] pixels;


          double[] counter = new double[3];
          double sum = 0;

          
          Bitmap vMap = BitmapFactory.decodeResource(getResources(), R.drawable.yield_overlay);
          int height = vMap.getHeight();
          int width = vMap.getWidth();
            
          Projection projection = googleMap.getProjection();
          Point point = projection.toScreenLocation(overlayLocation);
          

          pixels = new int[width*height]; 
          double[] p_array = new double [height * width];
          vMap.getPixels(pixels, 0, width, 0, 0, width, height);


          for (int y = 0; y < height; y++) {
              for(int x = 0; x < width; x++) {
                   point.x = x;
                   point.y = y;
                  

                   LatLng position = projection.fromScreenLocation(point);
                   Log.e(TAG, String.format("%f", sum));


                   if (PolyUtil.containsLocation(position, points, true)) {
                      int index = y * width + x;
                      int A = (pixels[index] >> 24) & 0xff;
                      int R = (pixels[index] >> 16) & 0xff;
                      int G = (pixels[index] >> 8) & 0xff;
                      int B = pixels[index] & 0xff;
                      sum += (A + R + G + B);
                      for(int i = 0; i < pixels.length; i++){
                          p_array[i] = sum;
      
                      }

                      if (sum != 0) {
                          MarkerOptions inMarkerOptions = new MarkerOptions();
                          inMarkerOptions.position(position);
                          inMarker = googleMap.addMarker(inMarkerOptions);
                         }
                      Log.e(Value, String.format("%f", R));
                      pixels[index] = 0xff000000 | (R << 16) | (G << 8)| B;
                      
                    }
                   
                }
             }
          //Mean
          double average = sum / p_array.length;

          //Variance
          double variance = 0;
          for(int i = 0; i<p_array.length; i++){
              variance += Math.pow(p_array[i]-average,2);
          }
          //Standard Deviation
          double std_dev;
          std_dev = Math.sqrt(variance);
          
          //Mode


          //Array Values
          counter[0] = average;
          counter[1] = variance;
          counter[2] = std_dev;

          
      
          return counter;
      }
      
      
  }
    



