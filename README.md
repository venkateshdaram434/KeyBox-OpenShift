KeyBox-OpenShift
======
A web-based SSH console for applications in an OpenShift domain. Connect and share terminal commands on multiple gears simultaneously.

Prerequisites
-------------
RHC Client tool
https://www.openshift.com/developers/rhc-client-tools-install

Browser with Web Socket support
http://caniuse.com/websockets

Install [FreeOTP](https://fedorahosted.org/freeotp) to enable two-factor authentication with Android or iOS

| FreeOTP       | Link                                                                                 |
|:------------- |:------------------------------------------------------------------------------------:|
| Android       | [Google Play](https://play.google.com/store/apps/details?id=org.fedorahosted.freeotp)|
| iOS           | [iTunes](https://itunes.apple.com/us/app/freeotp/id872559395)                        |

Install and Run with OpenShift Online
------
Install with RHC

    rhc app create keybox jbossews-2.0 --from-code git://github.com/skavanagh/KeyBox-OpenShift.git --gear-size medium

Open browser to

    https://keybox-<namespace>.rhcloud.com

Members of the domain can login with their OpenShift account

KeyBox will generate an SSH key pair and associate the public key with a user account on every login (Login to "openshift.com", under "My Account" -> "Settings").

    KeyBox-Generated-keybox-<namespace>.rhcloud.com


Alternate Installation with WildFly
------
Alternate Installation using the [WildFly Community Cartridge](https://github.com/openshift-cartridges/openshift-wildfly-cartridge)

    rhc app create keybox https://cartreflect-claytondev.rhcloud.com/reflect?github=openshift-cartridges/openshift-wildfly-cartridge --from-code git://github.com/skavanagh/KeyBox-OpenShift.git --gear-size medium

Screenshots
-----------

![Login](http://sshkeybox.com/img/screenshots/openshift/login.png)

![Two-Factor Authentication](http://sshkeybox.com/img/screenshots/openshift/two-factor.png)

![Select Servers 1](http://sshkeybox.com/img/screenshots/openshift/server_list1.png)

![Select Servers 2](http://sshkeybox.com/img/screenshots/openshift/server_list2.png)

![Terminals](http://sshkeybox.com/img/screenshots/openshift/terms1.png)

![More Terminals](http://sshkeybox.com/img/screenshots/openshift/terms2.png)


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


