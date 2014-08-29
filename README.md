KeyBox-OpenShift
======
A web-based SSH console for OpenShift

Prerequisites
-------------
RHC Client tool
https://www.openshift.com/developers/rhc-client-tools-install

Browser with Web Socket support
http://caniuse.com/websockets

**Note: In Safari if using a self-signed certificate you must import the certificate into your Keychain.
Select 'Show Certificate' -> 'Always Trust' when prompted in Safari


Install and Run on OpenShift
------
Install with RHC

    rhc app create keybox jbossews-2.0 --from-code git://github.com/skavanagh/KeyBox-OpenShift.git

Open browser to

    https://keybox-<namespace>.rhcloud.com

Members of the domain can login with their OpenShift account

KeyBox will generate an SSH key pair and associate the public key with the user's account for every user.

Public Key Name (under "My Account" -> "Settings")

    KeyBox-Generated-keybox-<namespace>.rhcloud.com


Acknowledgments
------
Special thanks goes to these amazing projects which makes this (and other great projects) possible.

+ [JSch](http://www.jcraft.com/jsch) Java Secure Channel - by [ymnk](https://github.com/ymnk)
+ [term.js](https://github.com/chjj/term.js) A terminal written in javascript - by [chjj](https://github.com/chjj)


Author
------
**Sean Kavanagh**

+ sean.p.kavanagh6@gmail.com
+ https://twitter.com/spkavanagh6

(Follow me on twitter for release updates, but mostly nonsense)


