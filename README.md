SimpleProximityApp
==================

This is a very simple Application that uses ProximityAPI to configure an interface, and launch mint on top of this.

Demo requirements
-----------------

1. Configure an OPEN access point with SSID "ProximityNetwork"
2. Use two Android devices with regular Wifi support.

Demo known issues
-----------------

Current implementation suffers several issues:

1. ProximityService() reference used in ProximityManager is returned in an asynchronous manner, asynchronous callbacks are handled on the main thread. It's not possible to block using waitForService method, doing this will end up in a deadlock.
2. enableNetwork(), tries to set the provided network but there's no guarantee that this network will be used (for instance if it doesn't exists)
3. Sometimes the enableNetwork seems to not respond or it takes a very long time to establish connection with the AP.

Future work
-----------

Improve logic and try to provide a less complex or simulate a synchronous call mechanism.

Demo instructions
-----------------

1. Check that wifi AP is working
2. Launch the app on both devices 
3. Press Manager to bind the ProximityManager with the ProximityService on both devices. Here we can see the first issue, if we try to get ProximityNetwork before the asynchronous callback happens it's possible that we end up with a NullPointerException. Please wait a second before moving to next step.
4. Press Network to obtain the ProximityNetwork instance, even if our configure() method is synchronous we don't have a guarantee that the connection has been established. This is the second issue observed. So we need to wait until the connection with ProximityNetwork AP is established. We need to Wait until the Wifi signal is back on the notification bar. If after a few seconds the signal measurement it's not back, press again the Network button.
5. Once we got the wifi network established we can press the Mint button, this will launch Mint and join the public channel. When Mint is fully started devices will be able to see each other and exchange messages using Mint.

