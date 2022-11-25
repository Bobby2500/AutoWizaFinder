@echo off
cls

echo Welcome to AutoWizaFinder V0.2 (By Elsa Digital)
echo ------------------------------------------------
echo This program will automatically search for all LinkedIn profiles listed in LinkedInURL.txt
echo Please ensure your device is set up properly as adviced in ReadMe.txt

FOR /f "eol=S tokens=2 delims=:" %%G IN (Credentials.txt) DO (

	SET ID=%%G

)

FOR /f "eol=A tokens=2 delims=:" %%G IN (Credentials.txt) DO (

	SET PASS=%%G

)

IF [%ID%]==[] (

	echo Please set your AWS Account ID in Credentials.txt
	pause
	GOTO end

)

IF [%PASS%]==[] (

	echo Please set your AWS Secret Key in Credentials.txt
	pause
	GOTO end

)


SET  AWS_ACCESS_KEY_ID=%ID%
SET  AWS_SECRET_ACCESS_KEY=%PASS%
pause

java -jar AutoWizaFinder_V0.2.jar

pause

:end
