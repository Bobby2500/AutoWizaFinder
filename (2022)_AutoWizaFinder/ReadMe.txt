Introduction
____________

AutoWizaFinder is a Java application that automates the process of finding emails from LinkedIn profile urls through Wiza's free trial. AutoWizafinder will randomly generate information and create new Wiza accounts, allowing for unlimited free contacts from Wiza. It utilizes TunnelBear VPN to change the user's IP and clears Wiza's cookies everytime before account creation.

AutoWizaFinder refers to your screen display on where to click and type, so it is IMPORTANT that the set-up is complete.



Set-up
______

You must have these installed:

Chrome - https://www.google.com/chrome

Wiza's Chrome extention - https://chrome.google.com/webstore/detail/wiza-find-contact-informa/pjmlkdacmaejhkdcflncbpcpidkggoio

Tunnel Bear - https://www.tunnelbear.com/

JRE(Java runtime environment) - https://java.com/en/download/manual.jsp


Configurations
______________

Ensure your Chrome theme is set to default(White & grey) or "Warm grey"
[Open new tab -> click "Customize Chrome"(bottom left) -> Select "Color and theme" -> Set Theme -> click "done"]

Ensure Chrome's developer tools is on the application tab
[Press F12 on any tab -> click "application" tab -> Expand "Cookies"]

Ensure Chrome's home button is enabled
[Click the three verticle dots(Top right) -> click "Settings" -> go to the "Appearance" tab -> toggle on "Show home button"]

Ensure your windows scaling is at 100%

Ensure to Log in to your Linked In account



Instructions
____________

Step 1 : Make sure everything is set up as stated above

Step 2 : Enter your AWS credentials in between the colons in Credentials.txt

Step 3 : Launch both Chrome and Tunnel Bear (Both in full screen)

Step 4 : Navigate to Wiza's "Create Account" page

Step 5 : Double click on run.bat

Step 6 : After you have pressed any button and the cmd window shows "Program start", by default you have 10 seconds to alt + tab to chrome.

Step 7 : Alt + tab so that Tunnel Bear would be the first application it switches to. Return back to Wiza's "Create Account" page. Then wait.

Step 8 : When the developer tools pop up on the right, expand cookies and click on wiza. Then wait.

Step 9 : When the program has alt + tab to Tunnel bear make sure to expand the server list on the top left, then scroll up until you see "fastest". Expand "USA" so that "Salt Lake City" is visible.

Step 10 : At this point everything should be automated. Simply leave the computer alone until it has completed all Linked In searches.


**** If this is your second time running, You would only need to do step 1 - 7. You can make the program run faster by reducing "Input Lag" in settings.txt