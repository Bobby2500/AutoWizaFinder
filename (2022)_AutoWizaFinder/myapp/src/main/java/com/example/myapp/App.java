package com.example.myapp;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.CreateBucketConfiguration;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.S3Client;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.*;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.AnalyzeDocumentRequest;
import com.amazonaws.services.textract.model.AnalyzeDocumentResult;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.Document;
import com.amazonaws.services.textract.model.S3Object;
import com.google.gson.Gson;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;

public class App {
	
	/*Autowizafinder is essentially a script written in Java and its purpose is to automate the collection of emails 
	  from a list of LinkedIn profile URLs completely for free. The script interacts with the UI using the Java robot 
	  library, and identifies the location of button presses by taking a scan of the UI and sending it to AWS cloud 
	  services to be processed.
	*/	
	
	//Static variables referred to later on in the program. Used to identify coordinates collected/read
	static int ROBOT_DELAY = 10 * 1000; 		//Default value for delay
	static int EVENT_DELAY = 2 * 1000;			//Default value for input delay
	final static int COOKIES = 0;
	final static int SEARCH_BAR = 1;
	final static int FILL_NAME = 2;
	final static int FILL_SIGN_UP = 3;
	final static int FILL_FINISH_SIGN_UP = 10;
	final static int GET_EMAIL = 5;
	final static int LOAD_IND = 4;
	final static int VPN_TOGGLE = 6;
	final static int VPN_1 = 7;
	final static int VPN_2 = 8;
	final static int VPN_3 = 9;
	
  public static void main(String[] args) throws IOException, AWTException, UnsupportedFlavorException {
	  
	//Arrays
	Robot robot = new Robot();
    List<String> url = new ArrayList<String>();									//URLs to search through
    List<String> emails = new ArrayList<String>();								//Emails collected 
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();				//Size of screen
    int[][] cords;																//Coordinates of buttons
    int[] center = {(int)(0.5*size.getWidth()), (int)(0.5*size.getHeight())};	//Cords to center of screen
    //Counters
    int confLinks = 0;
    int foundLinks = 0;
    int lastAccount = 0;
    boolean prevSet;
    //Colors 
    Color vpnOn = new Color(217,243,249);
    Color vpnOff = new Color(236,236,236);
    Color getEmailBtnErr = new Color(0,65,130);
    Color pageLoad1 = new Color(255,255,255);
    Color pageLoad2 = new Color(238,238,238);
    Color pageLoad3 = new Color(32,33,36);
    Color notFound = new Color(255,0,0);
    Color risky = new Color(255,241,55);
    Color valid = new Color(116,220,116);
    Color getEmailBtn = new Color(0,0,0);
    //Miscellaneous
    Path currentRelativePath = Paths.get("");
    String s = currentRelativePath.toAbsolutePath().toString();
    String actEmail;
    String actPass;
    
    //Populate URL
    File file = new File(s + "/LinkedInURl.txt");
    Scanner scan = new Scanner(file);
    
    while (scan.hasNextLine()) {
    	url.add(scan.nextLine());
    }
    
    scan.close();
    
    System.out.println("Program start");    

    /*Attempt to read Settings.txt and populate settings, otherwise scan for coordinates and use default values.
      This is so that it can save on AWS Textract scans.*/
    try {
    	
    	File settingsFile = new File(s + "/Settings.txt");
    	Scanner scanSet = new Scanner(settingsFile);
    	String[] settings = new String[4];
    	
    	for (int i = 0; i < settings.length; i++) {
    		settings[i] = scanSet.nextLine();
    	}
    	
    	scanSet.close();
    	
    	//Populate cords
    	String[] tempC = settings[0].split("\\|", -1);
    	cords = new int[11][2];
    	
    	for (int i = 1; i <= cords.length; i++) {
    		cords[i-1][0] = Integer.parseInt(tempC[(2*i)-1]);
    		cords[i-1][1] = Integer.parseInt(tempC[2*i]);
    	}
    	
    	//Populate color
    	String[] tempB = settings[1].split("\\|", -1);
    	int[] color = new int[3];
    	
    	for (int i = 1; i <= color.length; i++) {
    		color[i-1] = Integer.parseInt(tempB[i]);
    	}
    	
    	getEmailBtn = new Color(color[0], color[1], color[2]);
    	
    	//Populate delay
    	String[] tempD1 = settings[2].split(":");
    	ROBOT_DELAY = Integer.parseInt(tempD1[1]);
    	
    	//Populate input delay
    	String[] tempD2 = settings[3].split(":");
    	EVENT_DELAY = Integer.parseInt(tempD2[1]);
    	
    	robot.delay(ROBOT_DELAY);
    	prevSet = true;
    	
    } catch(Exception e) {
    	
    	//Find cookies button
    	robot.delay(ROBOT_DELAY);    
        keyPress(robot, KeyEvent.VK_F12);
        robot.delay(ROBOT_DELAY);
        lClickAtCord(robot, center);
        cords = getCordsFromBlocks(getBlocksFromScreen(), "Cookies");
        
        	if (cords[0][0] == 0) {
        		System.out.println("Bad read on Cookies");
        		System.exit(0);
        	}
        
        keyPress(robot, KeyEvent.VK_F12);
        
        //Find search bar, Wiza form, and create account button
        scrollAtCord(robot, center, 5);
        String[] browserSearch = {"wiza.co/auth/signup", "Your name", "Create my account"};
        cords = addToCords(cords, getCordsFromBlocks(getBlocksFromScreen(), browserSearch));
        
        //Error handling for bad read
        for (int i = 1; i < cords.length; i++) {
        	if (cords[i][0] == 0) {
        		System.out.println("Bad read on " + browserSearch[i - 1]);
        		System.exit(0);
        	}
        }
        
        //Manual search for refresh button
        int x = 0;
        int y = 0;
        int widgetWidth = 0;
        boolean loop = true;
        boolean firstInst = true;
        boolean secInst = false;
        boolean search = false;
        while (loop) {
        	
        	robot.mouseMove(cords[SEARCH_BAR][0] - x, cords[SEARCH_BAR][1]);
        	if (robot.getPixelColor(cords[SEARCH_BAR][0] - x, cords[SEARCH_BAR][1]).equals(pageLoad1) && firstInst == true) 
        		firstInst = false;

        	if (checkColor(robot.getPixelColor(cords[SEARCH_BAR][0] - x, cords[SEARCH_BAR][1]), pageLoad2, 1) && firstInst == false)         		
        		secInst = true;
        	
        	if (robot.getPixelColor(cords[SEARCH_BAR][0] - x, cords[SEARCH_BAR][1]).equals(pageLoad1) && secInst == true)	
        		search = true;

        	if (checkColor(robot.getPixelColor(cords[SEARCH_BAR][0] - x, cords[SEARCH_BAR][1]), pageLoad2, 1) && search == true) {
        		loop = false;
        		widgetWidth = cords[SEARCH_BAR][0] - x;
        	}
        	
        	x++;
        	
        }
        
        while (!robot.getPixelColor(cords[SEARCH_BAR][0] - x, cords[SEARCH_BAR][1]).equals(pageLoad1)) 
        	x++;
        
        widgetWidth = ((cords[SEARCH_BAR][0] - x) + widgetWidth)/2 + 3;;
        
        while (!checkColor(robot.getPixelColor(widgetWidth, cords[SEARCH_BAR][1] - y), pageLoad3, 1)) 
        	y++;
        
        //Cords of refresh button
        int[] loadInd = {widgetWidth, cords[SEARCH_BAR][1] - y};
        
        cords = addToCords(cords, loadInd);
        lClickAtCord(robot, cords[SEARCH_BAR]);
        typeIn(robot, url.get(0));
        checkLoad(robot, cords[LOAD_IND], pageLoad1);
        cords = addToCords(cords, getCordsFromBlocks(getBlocksFromScreen(), "Get Email"));
        
        //Find get email button
        robot.mouseMove(cords[GET_EMAIL][0], cords[GET_EMAIL][1]);
        int counter = 0;
        x = 0;
        while (counter < 10) {
        	if (robot.getPixelColor(cords[GET_EMAIL][0] - x, cords[GET_EMAIL][1]).equals(pageLoad1)) {
        		counter++;
        	} else {
        		counter = 0;
        	}
        	x++;
        }
        
        robot.mouseMove(cords[GET_EMAIL][0] - x + 17, cords[GET_EMAIL][1]);
        getEmailBtn = robot.getPixelColor(cords[GET_EMAIL][0] - x + 17, cords[GET_EMAIL][1]);
        System.out.println(getEmailBtn);
        
        //Get VPN buttons
        altTab(robot);
        robot.delay(ROBOT_DELAY);
        String[] vpnSearch = {"Off", "Fastest", "United States", "Salt Lake City"};
        cords = addToCords(cords, getCordsFromBlocks(getBlocksFromScreen(), vpnSearch));
        
        //Error handling for bad read
        for (int i = browserSearch.length + 3; i < cords.length; i++) {
        	if (cords[i][0] == 0) {
        		System.out.println("Bad read on " + vpnSearch[i - (browserSearch.length + 3)]);
        		System.exit(0);
        	}
        }
        
        altTab(robot);
        prevSet = false;
        
    }
    

    //Beginning of script
    for(int i = 0; i < 51; i++) {
    	
    	//Load sign up page
    	lClickAtCord(robot, cords[SEARCH_BAR]);
    	typeIn(robot, "https://wiza.co/auth/signup");
        checkLoad(robot, cords[LOAD_IND], pageLoad1);
        
        //Clear Wiza cookies
        keyPress(robot, KeyEvent.VK_F12);
        robot.delay(EVENT_DELAY);
        lClickAtCord(robot, cords[COOKIES]);
        keyPress(robot, KeyEvent.VK_RIGHT);
        keyPress(robot, KeyEvent.VK_RIGHT);
        robot.keyPress(KeyEvent.VK_SHIFT);
        robot.keyPress(KeyEvent.VK_F10);
        robot.keyRelease(KeyEvent.VK_SHIFT);
        robot.keyRelease(KeyEvent.VK_F10);
        keyPress(robot, KeyEvent.VK_ENTER);
        keyPress(robot, KeyEvent.VK_ENTER);
        keyPress(robot, KeyEvent.VK_F12);
    	
        //Turn on VPN
    	altTab(robot);
    	lClickAtCord(robot, cords[VPN_1]);
    	scrollAtCord(robot, cords[VPN_1], -7);
    	scrollAtCord(robot, cords[VPN_1], 4 + (int)(i/3));
    	lClickAtCord(robot, cords[VPN_1 + (i%3)]);
    	checkX(robot, center[1], vpnOn);
    	altTab(robot);
    	
    	//Reload sign up page
    	lClickAtCord(robot, cords[SEARCH_BAR]);
        typeIn(robot, "https://wiza.co/auth/signup");
        checkLoad(robot, cords[LOAD_IND], pageLoad1);
        scrollAtCord(robot, center, 5);
    	robot.mouseWheel(20);
    	
    	//Fill in Wiza sign up form
    	EVENT_DELAY = EVENT_DELAY/5; 
    	
    	lClickAtCord(robot, center);
    	lClickAtCord(robot, cords[FILL_NAME]);
    	typeIn(robot, genRandString(5));
    	keyPress(robot, KeyEvent.VK_TAB);
    	typeIn(robot, genRandString(5));    	
    	keyPress(robot, KeyEvent.VK_TAB);
    	actEmail = genRandString(15) + "@gmail.com";
    	typeIn(robot, actEmail);
    	keyPress(robot, KeyEvent.VK_TAB);
    	actPass = genRandNum(8);
    	typeIn(robot, actPass);
    	keyPress(robot, KeyEvent.VK_TAB);
    	keyPress(robot, KeyEvent.VK_TAB);
    	typeIn(robot, genRandNum(9));
    	
    	EVENT_DELAY = EVENT_DELAY*5;
    	
    	//Click sign up button
    	lClickAtCord(robot, cords[FILL_SIGN_UP]);
    	checkLoad(robot, cords[LOAD_IND], pageLoad1);
    	
    	scrollAtCord(robot, center, 5);
    	
    	//Scan for Finish signup if settings were not populated
    	if (i == 0 && !prevSet)
    		cords = addToCords(cords, getCordsFromBlocks(getBlocksFromScreen(), "Finish signup"));
    	
    	//Click Finish signup button
    	lClickAtCord(robot, cords[FILL_FINISH_SIGN_UP]);
    	robot.delay(EVENT_DELAY/2);
    	checkLoad(robot, cords[LOAD_IND], pageLoad1);
    	
    	//Turn off VPN
    	altTab(robot);
    	lClickAtCord(robot, cords[VPN_TOGGLE]);
    	while (!checkX(robot, center[1], vpnOff, vpnOn)) {
    		lClickAtCord(robot, cords[VPN_TOGGLE]);
    	}
    	
    	altTab(robot);
    	robot.delay(EVENT_DELAY/2);
    	
    	//Start of email collection
    	BufferedWriter writer;
    	BufferedWriter csvWriter;
    	int tries = 0;
    	confLinks = 0;
    	//Searches until account runs out of credits or until all URLs are searched
    	while(confLinks < 20 && foundLinks < url.size()) { 
    		
    		//Loads URL
    		lClickAtCord(robot, cords[SEARCH_BAR]);
    		typeIn(robot, url.get(foundLinks));
    		checkLoad(robot, cords[LOAD_IND], pageLoad1);
    		
    		//Finds for GetEmail button
    		for (int y = cords[GET_EMAIL][1] - 100; y < cords[GET_EMAIL][1] + 100; y += 5) {
    			robot.mouseMove(cords[GET_EMAIL][0], y);
    			robot.delay(50);
    			if(checkColor(robot.getPixelColor(cords[GET_EMAIL][0], y), getEmailBtn, 1)) {
    				
    				tries = 0;
    				int x = 0;
    				int counter = 0;
    				int top = 0;
    				String state = "";
    				
    				//Scan up
    				while (counter < 10) {
    					y--;
    					robot.mouseMove(cords[GET_EMAIL][0], y);
    					if (robot.getPixelColor(cords[GET_EMAIL][0], y).equals(pageLoad1) || checkColor(robot.getPixelColor(cords[GET_EMAIL][0], y), getEmailBtnErr, 2)) {
    						counter++;
    					} else {
    						counter = 0;
    					}
    				}
    				
    				y += 10;
    				top = y;
    				counter = 0;
    				
    				//Scan down
    				while (counter < 10) {
    					y++;
    					robot.mouseMove(cords[GET_EMAIL][0], y);
    					if (robot.getPixelColor(cords[GET_EMAIL][0], y).equals(pageLoad1)) {
    						counter++;
    					} else {
    						counter = 0;
    					}
    				}
    				
    				//Center of button
    				y = (top + (y - 10))/2;
    				lClickAtCord(robot, cords[GET_EMAIL][0], y);
    		    	
    				//Awaits for email to load
    		    	while (!checkColor(robot.getPixelColor(cords[GET_EMAIL][0] + x, y), notFound, 1) && !checkColor(robot.getPixelColor(cords[GET_EMAIL][0] + x, y), risky, 1) && !checkColor(robot.getPixelColor(cords[GET_EMAIL][0] + x, y), valid, 1)) {    		    		
    		    		x += 5;
    		    		x = x%300;
    		    	}
    		    	
    		    	//Emails are stored with their confidence level
    		    	if (checkColor(robot.getPixelColor(cords[GET_EMAIL][0] + x, y), notFound, 1)) {
    		    		state = "(Not Found)";
    		    	} else if (checkColor(robot.getPixelColor(cords[GET_EMAIL][0] + x, y), risky, 1)) {
    		    		state = "(Risky)";
    		    	} else {
    		    		state = "(Valid)";
    		    		confLinks++;
    		    	}
    		    	
    				lClickAtCord(robot, cords[GET_EMAIL][0], y);
    				
    		    	if (state.equals("(Not Found)")) {
    		    		emails.add(state);
    		    	} else {
    		    		emails.add(returnClip() + state);
    		    	}
    		    	
    		    	foundLinks++;
    		    	y = cords[GET_EMAIL][1] + 200;
    			}
    			
    		}
  
    		tries++;
    		if (tries == 2) {
    			foundLinks++;
    			emails.add("(Not Found)");
    			tries = 0;
    		}
    		
    	}
    	
    	//Exit when all URLs are found
    	if (foundLinks == url.size()) {
    		lastAccount = i + 1;
    		i = 100;
    	}
   
    	//Writes down emails after every account used
    	writer = new BufferedWriter(new FileWriter(s + "/Results.txt", true));
    	
    	writer.write("\nAccounts Used\n-------------\n\n");
    	
    	for (int j = 0; j < lastAccount; j++) {
    		writer.write("Account  : " + actEmail + " : " + actPass + "\n");
    	}
    	
    	writer.write("___________________________________________\n\nEmails\n------\n");
    	
    	for (int j = 0; j < emails.size(); j++) {
    		writer.write("(" + (j + 1) + ") " + emails.get(j) + " : " + url.get(j) + "\n");
    	}
    	
    	writer.close();
    	
    	//Writes down emails formatted for CSV
    	csvWriter = new BufferedWriter(new FileWriter(s + "/CsvResults.txt", true));
    	
    	for (int j = 0; j < emails.size(); j++) {
    		csvWriter.write(emails.get(j) + "\n");
    	}
    	
    	csvWriter.close();
    	
    	emails.clear();
    
    }
    
    
    //Populates existing settings into Settings.txt
    FileWriter writerSet = new FileWriter(s + "/Settings.txt");
    
    writerSet.write("Cords:|");
    for (int i = 0; i < cords.length; i++) {
    	writerSet.write(cords[i][0] + "|");
    	writerSet.write(cords[i][1] + "|");
    }
    
    writerSet.write("\nGet Email Button:|" + getEmailBtn.getRed() + "|" + getEmailBtn.getGreen() + "|" + getEmailBtn.getBlue() + "|" );
    writerSet.write("\nTime to set up:" + ROBOT_DELAY);
    writerSet.write("\nInput lag:" + EVENT_DELAY);
    
    writerSet.close();

  }
  
  /**
   * This method is used to gather coordinates of a single text from Block objects generated by AWS Textract
   * @param blocks The array of Block objects
   * @param phrase The string that contain the text you are searching for
   * @return Array that contains the coordinates in (x,y) format
   */
  public static int[][] getCordsFromBlocks (List<Block> blocks, String phrase){
	  
	  int[][] arr = new int[1][2];
	  
	      for (Block block : blocks) {
	    	  
	    	  if (block.getText() != null) {
	    		  
	    		  if (block.getText().equals(phrase)) {
	    			  
	    			  XY[] xylist = convertXYClass (block.getGeometry().getPolygon().toString());
	    			  Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
	    			  
	    			  arr[0][0] = (int)(((xylist[0].X + xylist[1].X)/2)*(int)size.getWidth());
	    			  arr[0][1] = (int)(((xylist[0].Y + xylist[2].Y)/2)*(int)size.getHeight());
	    			  return arr;	    			  
	    		  }
	    	  }
	      }

	  return arr;
	  
  }

  /**
   * This method is used to gather coordinates of multiple text from Block objects generated by AWS Textract
   * @param blocks The array of Block objects
   * @param phrase The string array that contain the texts you are searching for
   * @return Array that contains the coordinates in (x,y) format
   */
  public static int[][] getCordsFromBlocks (List<Block> blocks, String[] phrase){
	  
	  int[][] arr = new int[phrase.length][2];
	  int i = 0;
	  int y = 0;
	  
	  while (i < phrase.length) {
	      
		  y = i;
		  
	      for (Block block : blocks) {
	    	  
	    	  if (block.getText() != null) {
	    		  
	    		  if (i < phrase.length && block.getText().equals(phrase[i]) && y == i) {
	    			  
	    			  XY[] xylist = convertXYClass (block.getGeometry().getPolygon().toString());
	    			  Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
	    			  arr[i][0] = (int)(((xylist[0].X + xylist[1].X)/2)*(int)size.getWidth());
	    			  arr[i][1] = (int)(((xylist[0].Y + xylist[2].Y)/2)*(int)size.getHeight());
	    			  i++;
	    			  	    			  
	    		  }
	    	  }
	      }
	      
	      if (y == i) {
	    	  i++;
	      }
	    	  
      }

	  return arr;
	  
  }
  
  /**
   * This method is used to gather a list of Block objects from the user's screen, using AWS Textract
   * @return The list of Block objects returned from AWS Textract
   */
  public static List<Block> getBlocksFromScreen() throws IOException, AWTException{
	  
	  String bucket = "temp-get-cords-from-screen";
	  String key = (int)Math.random()*10000 + "";
	  
	  Region region = Region.AP_SOUTHEAST_1;
	  S3Client s3 = S3Client.builder().region(region).build();
	  Robot robot = new Robot();
	  
	  Setup(s3, bucket, region);
	  
	  Rectangle rec = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
	  BufferedImage img = robot.createScreenCapture(rec);
	  ByteArrayOutputStream os = new ByteArrayOutputStream();
	  ImageIO.write(img, "jpeg", os);                        

	  System.out.println("Uploading object...");
	  
	  s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(),RequestBody.fromBytes(os.toByteArray()));
	  
	  System.out.println("Upload complete");
	  System.out.printf("%n");
	  
      // Call AnalyzeDocument 
	  
      EndpointConfiguration endpoint = new EndpointConfiguration(
              "https://textract.ap-southeast-1.amazonaws.com", "ap-southeast-1");
      AmazonTextract client = AmazonTextractClientBuilder.standard()
              .withEndpointConfiguration(endpoint).build();
      
              
      AnalyzeDocumentRequest request = new AnalyzeDocumentRequest()
              .withFeatureTypes("TABLES", "FORMS")
               .withDocument(new Document().
                      withS3Object(new S3Object().withName(key).withBucket(bucket)));


      AnalyzeDocumentResult result = client.analyzeDocument(request);
      
	  cleanUp(s3, bucket, key);

	  System.out.println("Closing the connection to {S3}");
	  s3.close();
	  
	  System.out.println("Connection closed");
	  System.out.println("Exiting...");
      
      return result.getBlocks();
      
  }

  
  /**
   * This method is used to left click at a given coordinate
   * @param robot Robot object that interacts with the UI
   * @param cord The integer array that contains the coordinates in (x,y) format
   */
  public static void lClickAtCord(Robot robot, int[] cord) {
	  
	  robot.delay(EVENT_DELAY/2);  
	  robot.mouseMove(cord[0], cord[1]);
	  robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
	  robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	  robot.delay(EVENT_DELAY/2);
	    
  }

  /**
   * This method is used to left click at a given coordinate
   * @param robot Robot object that interacts with the UI
   * @param x The integer that contains the x coordinate
   * @param y The integer that contains the y coordinate
   */
  public static void lClickAtCord(Robot robot, int x, int y) {
	  
	  robot.delay(EVENT_DELAY/2);  
	  robot.mouseMove(x, y);
	  robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
	  robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	  robot.delay(EVENT_DELAY/2);
	    
  }
  
  /**
   * This method is used to right click at a given coordinate
   * @param robot Robot object that interacts with the UI
   * @param cord The integer array that contains the coordinates in (x,y) format
   */
  public static void rClickAtCord (Robot robot, int[] cord) {
	  
	  robot.delay(EVENT_DELAY/2);  
	  robot.mouseMove(cord[0], cord[1]);
	  robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
	  robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
	  robot.delay(EVENT_DELAY/2);
	  
  }

  /**
   * This method is used to left click at a given coordinate
   * @param robot Robot object that interacts with the UI
   * @param x The integer that contains the x coordinate
   * @param y The integer that contains the y coordinate
   */
  public static void rClickAtCord(Robot robot, int x, int y) {
	  
	  robot.delay(EVENT_DELAY/2);  
	  robot.mouseMove(x, y);
	  robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
	  robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
	  robot.delay(EVENT_DELAY/2);
	    
  }

  /**
   * This method is used to scroll a number of indents at a given coordinate
   * @param robot Robot object that interacts with the UI
   * @param cord The integer array that contains the coordinates in (x,y) format
   * @param indent The number of indents to scroll through
   */
  public static void scrollAtCord (Robot robot, int[]cord ,int indent) {
	  
	  int dir;
	  
	  if (indent > 0) {
		  dir = 1;
	  } else {
		  dir = -1;
	  }
	  
	  robot.delay(EVENT_DELAY/2);  
	  robot.mouseMove(cord[0], cord[1]);
	  
	  for (int i = 0; i < Math.abs(indent); i++) {
		  robot.mouseWheel(dir);
		  robot.delay(100);
	  }
	  
	  robot.delay(EVENT_DELAY/2);
	 
  }
    
  /**
   * This method is used to scroll a number of indents at a given coordinate
   * @param robot Robot object that interacts with the UI
   * @param x The integer that contains the x coordinate
   * @param y The integer that contains the y coordinate
   * @param indent The number of indents to scroll through
   */  
  public static void scrollAtCord (Robot robot, int x, int y,int indent) {
	  
	  int dir;
	  
	  if (indent > 0) {
		  dir = 1;
	  } else {
		  dir = -1;
	  }
	  
	  robot.delay(EVENT_DELAY/2);  
	  robot.mouseMove(x, y);
	  
	  for (int i = 0; i < Math.abs(indent); i++) {
		  robot.mouseWheel(dir);
		  robot.delay(500);
	  }
	  
	  robot.delay(EVENT_DELAY/2);
	 
  }

  /**
   * This method is used to press a button given the integer ID of a KeyEvent
   * @param robot Robot object that interacts with the UI
   * @param key Integer ID of a KeyEvent
   */
  public static void keyPress (Robot robot, int key) {
	  
	  robot.delay(EVENT_DELAY/2);  
	  robot.keyPress(key);
	  robot.keyRelease(key);
	  robot.delay(EVENT_DELAY/2);
	  
  }

  /**
   * This method is used to paste a string 
   * @param robot Robot object that interacts with the UI
   * @param sText String to be pasted
   */
  public static void typeIn (Robot robot, String sText) {
	  
	  StringSelection ssText = new StringSelection(sText);
	  Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
	  clip.setContents(ssText, ssText);
	  
	  robot.delay(EVENT_DELAY/2);
	  robot.keyPress(KeyEvent.VK_CONTROL);
	  robot.keyPress(KeyEvent.VK_A);
	  robot.keyRelease(KeyEvent.VK_CONTROL);
	  robot.keyRelease(KeyEvent.VK_A);
	  robot.keyPress(KeyEvent.VK_BACK_SPACE);
	  robot.keyRelease(KeyEvent.VK_BACK_SPACE);
	  robot.keyPress(KeyEvent.VK_CONTROL);
	  robot.keyPress(KeyEvent.VK_V);
	  robot.keyRelease(KeyEvent.VK_CONTROL);
	  robot.keyRelease(KeyEvent.VK_V);
	  robot.keyPress(KeyEvent.VK_ENTER);
	  robot.delay(EVENT_DELAY/2);
	  
  }

  /**
   * This method is used to Alt-tab
   * @param robot Robot object that interacts with the UI
   */
  public static void altTab (Robot robot) {
	  
	  robot.keyPress(KeyEvent.VK_ALT);
	  robot.keyPress(KeyEvent.VK_TAB);
	  robot.keyRelease(KeyEvent.VK_TAB);
	  robot.keyRelease(KeyEvent.VK_ALT);
	  robot.delay(EVENT_DELAY/2);
  }
  

  /**
   * This method is used to delay the script until the given color is no longer on the coordinates
   * @param robot Robot object that interacts with the UI
   * @param cord The integer array that contains the coordinates in (x,y) format
   * @param color The color that is expected to disappear 
   */
  public static void checkLoad (Robot robot, int[] cord, Color color) {
	  
	  while (robot.getPixelColor(cord[0], cord[1]).equals(color)) {
		  robot.delay(1000);
	  }
	  
	  robot.delay(2000);
  }
  
  /**
   * This method is used to delay the script until the given color is detected on the x plane on the given y coordinate
   * @param robot Robot object that interacts with the UI
   * @param y The integer that contains the y coordinate
   * @param color The color that is expected to appear 
   */
  public static void checkX (Robot robot, int y, Color color) {
	  
	  Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
	  int x = 0;
	  
	  while (!checkColor(robot.getPixelColor(x, y), color, 1)) {
		  robot.delay(1000);
		  x += 25;
		  x = x % size.width;
	  }
	  
	  robot.delay(2000);
	  
  }

  /**
   * This method is used to delay the script until color1 or color2 is detected on the x plane on the given y coordinate
   * @param robot Robot object that interacts with the UI
   * @param y The integer that contains the y coordinate
   * @param color1 The color that is expected to appear 
   * @param color2 The color that is expected to appear 
   * @return true - color1 detected 
   * 		 false - color2 detected
   */
  public static boolean checkX (Robot robot, int y, Color color1, Color color2) {
	  
	  Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
	  int x = 0;
	  
	  while (!checkColor(robot.getPixelColor(x, y), color1, 1)) {
		  
		  if (checkColor(robot.getPixelColor(x, y), color2, 1)) {
			  return false;
		  }
		  
		  robot.delay(1000);
		  x += 25;
		  x = x % size.width;
	  }
	  
	  robot.delay(2000);
	  
	  return true;
  }

  /**
   * This method is used to delay the script until the given color is detected on the y plane on the given x coordinate
   * @param robot Robot object that interacts with the UI
   * @param x The integer that contains the x coordinate
   * @param color The color that is expected to appear 
   */
  public static void checkY (Robot robot, int x, Color color) {
	  
	  Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
	  int y = 0;
	  
	  while (!robot.getPixelColor(x, y).equals(color)) {
		  robot.delay(1000);
		  y += 25;
		  y = y % size.height;
	  }
	  
	  robot.delay(2000);
	  
  }
  
  /**
   * This method is used to return the string stored in the user's clip board
   * @return String stored in the user's clip board
   */
  public static String returnClip() throws UnsupportedFlavorException, IOException {
	  
	  Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
	  return (String)clip.getData(DataFlavor.stringFlavor);
	  
  }
  
  
  /**
   * This method is used to generate a random string(a-z) with a given length
   * @param length Length of string
   * @return Random string
   */
  public static String genRandString (int length) {
	  
	 int leftLimit = 97; // letter 'a'
	 int rightLimit = 122; // letter 'z'
	 Random random = new Random();

	 String generatedString = random.ints(leftLimit, rightLimit + 1)
	   .limit(length)
	   .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
	   .toString();

	 return generatedString;
  }
  
  /**
   * This method is used to generate a random integer with a given length
   * @param length Number of digits in integer
   * @return Random integer
   */
  public static String genRandNum (int length) {
	  
	  return "" + (int)(Math.random()*(Math.pow(10, length)));
	  
  }
  
  /**
   * This method is used to attach a int[x][2] to another int[x][2]
   * @param cords Array to be added to
   * @param addCord Array to add
   * @return Combined array
   */
  public static int[][] addToCords (int[][] cords, int[][] addCord) {
	  
	  int[][] arr = new int[cords.length + addCord.length][2];
	  
	  for (int i = 0; i < cords.length; i++) {
		  arr[i] = cords[i]; 
	  }
	  
	  for (int i = cords.length; i < cords.length + addCord.length; i++) {
		  arr[i] = addCord[i - cords.length];
	  }
	  
	  return arr;
	  
  }
  
  /**
   * This method is used to attach a int[2] to a int[x][2]
   * @param cords Array to be added to
   * @param addCord Array to add
   * @return Combined array
   */
  public static int[][] addToCords (int[][] cords, int[] addCord) {
	  
	  int[][] arr = new int[cords.length + 1][2];
	  
	  for (int i = 0; i < cords.length; i++) {
		  arr[i] = cords[i]; 
	  }
	  
	  arr[cords.length] = addCord;
	  
	  return arr;
	  
  }
  
  /**
   * This method is used to be able to detect a color with a given variance. (Fuzzy logic)
   * @param Ocolor The color to be tested against
   * @param color The color that is varied
   * @param variance The accuracy of the detection, each integer increment is a +/- 1 point to the R/G/B value
   * @return true - Ocolor matches color when varied
   * 		 false - Ocolor does not match color when varied
   */
  public static boolean checkColor (Color Ocolor, Color color, int variance) {
	  
	  Color check;
	  
	  for (int i = -variance; i < variance; i++) {
		  
		  if (!(color.getRed() + i > 255 || color.getGreen() + i > 255 || color.getBlue() + i > 255 || color.getRed() + i < 0 || color.getGreen() + i < 0 || color.getBlue() + i < 0)) {
		  
			  if (i == 0) {
				  
				  if (Ocolor.equals(color))
					  return true;
				  
			  } else {
				  
				  check = new Color(color.getRed() + i,color.getGreen(),color.getBlue());
				  if (Ocolor.equals(check))
					  return true;
				  
				  check = new Color(color.getRed(),color.getGreen() + i,color.getBlue());
				  if (Ocolor.equals(check))
					  return true;
				  			  
				  check = new Color(color.getRed(),color.getGreen(),color.getBlue() + i);
				  if (Ocolor.equals(check))
					  return true;
				  			  
				  check = new Color(color.getRed() + i,color.getGreen() + i,color.getBlue());
				  if (Ocolor.equals(check))
					  return true;
				  			  
				  check = new Color(color.getRed() + i,color.getGreen(),color.getBlue() + i);
				  if (Ocolor.equals(check))
					  return true;
				  			  
				  check = new Color(color.getRed(),color.getGreen() + i,color.getBlue() + i);
				  if (Ocolor.equals(check))
					  return true;
				  			  
				  check = new Color(color.getRed() + i,color.getGreen() + i,color.getBlue() + i);
				  if (Ocolor.equals(check))			  
					  return true;
				  			  
			  }
			  
		  }
		  
	  }
	  
	  return false;
	  
  }
  
 
  public static XY[] convertXYClass (String str) {
	  str = str.replaceAll("X", "\"X\"");
	  str = str.replaceAll("Y", "\"Y\"");
	  XY[] list = new XY[] {};
	  Gson gson = new Gson ();
	  list = gson.fromJson(str, list.getClass());
	  return list;
  }
  
  class XY {
	  public double X = 0;
	  public double Y = 0;
  }
  
  
  /**
   * This method is used to set up a connection with the AWS S3 servers and create a bucket
   * @param s3Client The client object
   * @param bucketName Name of bucket to be created in S3
   * @param region The server that is used
   */
  public static void Setup(S3Client s3Client, String bucketName, Region region) {
    try {
      s3Client.createBucket(CreateBucketRequest
          .builder()
          .bucket(bucketName)
          .createBucketConfiguration(
              CreateBucketConfiguration.builder()
                  .locationConstraint(region.id())
                  .build())
          .build());
      System.out.println("Creating bucket: " + bucketName);
      s3Client.waiter().waitUntilBucketExists(HeadBucketRequest.builder()
          .bucket(bucketName)
          .build());
      System.out.println(bucketName +" is ready.");
      System.out.printf("%n");
    } catch (S3Exception e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
  }

  /**
   * This method is used to disconnect with the AWS S3 servers and delete a bucket
   * @param s3Client The client object
   * @param bucketName Name of bucket to be deleted in S3
   * @param region The server that is used
   */
  public static void cleanUp(S3Client s3Client, String bucketName, String keyName) {
    System.out.println("Cleaning up...");
    try {
      System.out.println("Deleting object: " + keyName);
      DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder().bucket(bucketName).key(keyName).build();
      s3Client.deleteObject(deleteObjectRequest);
      System.out.println(keyName +" has been deleted.");
      System.out.println("Deleting bucket: " + bucketName);
      DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucketName).build();
      s3Client.deleteBucket(deleteBucketRequest);
      System.out.println(bucketName +" has been deleted.");
      System.out.printf("%n");
    } catch (S3Exception e) {
      System.err.println(e.awsErrorDetails().errorMessage());
      System.exit(1);
    }
    System.out.println("Cleanup complete");
    System.out.printf("%n");
  }
}