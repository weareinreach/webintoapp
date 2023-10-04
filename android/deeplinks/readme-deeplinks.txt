-----------------------------------------------------
Android Deep Links:
-----------------------------------------------------

- 	The system extracted the fingerprints from the keystore of the 
	App and created the '.well-known/assetlinks.json'.

-	in order to use the deep links technic correctly you will 
	need to copy the '.well-known' folder with all its content 
	(the assetlinks.json file) to the root of the hostname.

-	After uploading the '.well-known/assetlinks.json' to the root of 
	the hostname, you can test an existing statement file with the 
	Tester tool:
	https://developers.google.com/digital-asset-links/tools/generator

-	All the returned data from the 'keytool -list' command saved to the 
	'keytool-data.txt' file.

-	You can read more about the 'Verify Android App Links' at:
	https://developer.android.com/training/app-links/verify-site-associations

Please note, in order to display the '.well-known' directory under 
the finder of the MacOS press the following keys together: 
cmd + shift + full-stop (period). That is: ⌘ + ⇧ + .

-------------------------------------
WebIntoApp.com Team.
https://www.webintoapp.com
-------------------------------------
