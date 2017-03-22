package csi403;


// Import required java libraries
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.json.*;


// Extend HttpServlet class
public class pQueue extends HttpServlet {
	

  // Standard servlet method 
  public void init() throws ServletException
  {
      // Do any required initialization here - likely none
  }

  // Standard servlet method - we will handle a POST operation
  public void doPost(HttpServletRequest request,
                    HttpServletResponse response)
            throws ServletException, IOException
  {
      doService(request, response); 
  }

  // Standard servlet method - we will not respond to GET
  public void doGet(HttpServletRequest request,
                    HttpServletResponse response)
            throws ServletException, IOException
  {
      // Set response content type and return an error message
      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.println("{ 'message' : 'Use POST!'}");
  }

  // Our main worker method
  // Parses messages e.g. {"inList" : [5, 32, 3, 12]}
  // Returns the list sorted.   
  private void doService(HttpServletRequest request,
                    HttpServletResponse response)
            throws ServletException, IOException
  {
	  try{
      // Get received JSON data from HTTP request
      BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));
      String jsonStr = "";
      if(br != null){
          jsonStr = br.readLine();
      }
	  
      // Create JsonReader object
      StringReader strReader = new StringReader(jsonStr);
      JsonReader reader = Json.createReader(strReader);

      // Get the singular JSON object (name:value pair) in this message.    
      JsonObject obj = reader.readObject();
      // From the object get the array named "inList"
      JsonArray inArray = obj.getJsonArray("inList");
      JsonArrayBuilder outArrayBuilder = Json.createArrayBuilder();
	  
	  //variables
	  int i, j, k, temp;
	  int min = 0;
	  
	  //deals with when element in array is negative because of dequeue
	  int neg_check = 0;
	  
	  //counts number of enqueues
	  int en_count = 0;
	  
	  //counts number of jobs by priority
	  int pri_count = 0;
	  
	  //string array stores job names
	  String[] names = new String[inArray.size()];
	  
	  //int array stores each job's priority
	  int[] priors = new int[inArray.size()];
	  
	  //final priorities of queue
	  int[] statex = new int[inArray.size()];
	  
	  //store final state of jobs left in the queue
	  String[] jobs = new String[inArray.size()];
	  
	  //command strings
	  String enqueue = "enqueue";
	  String dequeue = "dequeue";
	  
	  for(i = 0; i < inArray.size(); i++)
	  {
		  //grabs each subarray in the inList array
		  JsonObject task = inArray.getJsonObject(i);
		  String cmd = task.getString("cmd");
		  
		  //if enqueue
		  if(cmd.equals(enqueue))
		  {
			  names[en_count] = task.getString("name"); 
			  priors[en_count] = task.getInt("pri");
			  en_count++;
		  }	  
		  //if dequeue
		  else if(cmd.equals(dequeue))
		  {
			  //finds the min priority in queue while ignoring negative values that simulate "popping"
			  for(j = 0; j < en_count; j++)
			  {
				  if(priors[j] >= 0 && j == 0)
				  {
					  min = priors[j];
				  }
				  else if(priors[j] >= 0 && neg_check == 1)
				  {
					  min = priors[j];
					  neg_check = 0;
				  }
				  else if(priors[j] < 0 && j == 0)
				  {
					neg_check = 1;
					continue; 
				  }
				  else if(priors[j] < 0)
				  {
					  continue;
				  }
				  
				  if(min > priors[j])
					{
						min = priors[j];
					}
			  }
			  
			  //simulates popping of element that was dequeued
			  for(j = 0; j < en_count; j++)
			  {
				  if(priors[j] == min)
				  {
					  priors[j] = -1;
					  break;
				  }
			  }
		  }
	  }
	  
	  //stores final jobs' priorities
	  for(j = 0; j < en_count; j++)
	  {
		  if(priors[j] >= 0)
		  {
			  statex[pri_count] = priors[j];
			  pri_count++;
		  }
	  }

	  //insertion sort algorithm
	  for(i = 1; i < pri_count; i++)
	  {
		  for(j = i; j > 0; j--)
		  {
			  //if the consecutive value is smaller than the previous, switch the values
			  if(statex[j] < statex[j-1])
			  {
				  temp = statex[j];
				  statex[j] = statex[j-1];
				  statex[j-1] = temp;
			  }
		  }
	  }
	
	  //maps the priority numbers left in queue to job names and stores the names
	  for(i = 0; i < pri_count; i++)
	  {
		  for(j = 0; j < en_count; j++)
		  {		
			  if(statex[i] == priors[j])
			  {
			  	  jobs[i] = names[j];
				  break;
			  }
		  }
	  }
	  
	  //prints final jobs left in queue
	  for(i = 0; i < pri_count; i++) 
	  {
          outArrayBuilder.add(jobs[i]); 
      }
      
      // Set response content type to be JSON
      response.setContentType("application/json");
      // Send back the response JSON message
      PrintWriter out = response.getWriter();
      out.println("{ \"outList\" : " + outArrayBuilder.build().toString() + "}"); 
	 }
	  
	  //catch errors
	  catch(JsonException e){
		  response.setContentType("application/json");
		  PrintWriter out = response.getWriter();
		  out.println("{ \"message\" : \"Json Exception!\"}");
	  }
	  catch(IllegalStateException e){
		  response.setContentType("application/json");
		  PrintWriter out = response.getWriter();
		  out.println("{ \"message\" : \"illegal state!\"}");
	  }
	  catch(Exception e){
		  response.setContentType("application/json");
		  PrintWriter out = response.getWriter();
		  out.println("{ \"message\" : \"exception!\"}");
	  }
  }	  
	
  // Standard Servlet method
  public void destroy()
  {
      // Do any required tear-down here, likely nothing.
  }
}

