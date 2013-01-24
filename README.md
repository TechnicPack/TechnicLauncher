Technic Launcher
===================

## What is The Technic Launcher?
The Technic Launcher is a modified version of the Spoutcraft Launcher, which has been modified for the distribution of Technic mod packs.  It features automated minecraft.jar version patching, automated updates, and more!  
The launcher in it's current form is maintained by [Olloth](https://github.com/Olloth)

[![Technic][Logo]][Homepage]  
[Homepage] | [Forums] | [Twitter] | [Facebook] | [Steam]

## The License
The Technic Launcher is licensed under the same license as the Spoutcraft Launcher it is derived from. The [GNU Lesser General Public License Version 3][License], but with a provision that files are released under the MIT license 180 days after they are published. Please see the `LICENSE.txt` file for details.

Copyright (c) 2012, Technic <<http://www.technicpack.net/>>  
Upstream Copyright (c) 2011-2012, Spout LLC <<http://www.spout.org/>>

## Getting the Source
The latest and greatest source can be found here on [GitHub][Source].  
Download the latest builds from our [build server][Builds]. [![Build Status](http://build.technicpack.net/job/TechnicLauncher/badge/icon)](http://build.technicpack.net/job/TechnicLauncher/)

## Compiling the Source
Technic Launcher uses Maven to handle its dependencies.

* Install [Maven 2 or 3](http://maven.apache.org/download.html)
* Checkout this repo and run: `mvn clean package`
* To compile an `exe` on a non-Windows platform, add: `-P package-win` to the previous goals.

## Contributing to the Project
Track and submit issues and bugs on our [GitHub issues page][Issues].  

## Code and Pull Request Formatting
* Generally follow the Oracle coding standards.
* No spaces, only tabs for indentation.
* No trailing whitespaces on new lines.
* 200 column limit for readability.
* Pull requests must compile, work, and be formatted properly.
* Sign-off on ALL your commits - this indicates you agree to the terms of our license.
* No merges should be included in pull requests unless the pull request's purpose is a merge.
* Number of commits in a pull request should be kept to *one commit* and all additional commits must be *squashed*.
* You may have more than one commit in a pull request if the commits are separate changes, otherwise squash them.
* For clarification, follow the Spout pull request guidelines [here](http://spout.in/prguide).

**Please follow the above conventions if you want your pull request(s) accepted.**

[Logo]: http://i.imgur.com/H23l53N.png
[Homepage]: http://www.technicpack.net
[Forums]: http://forums.technicpack.net
[License]: http://cdn.spout.org/license/spoutv1.txt
[Source]: https://github.com/TechnicPack/Spoutcraft-Launcher/tree/dev
[Builds]: http://build.technicpack.net/job/TechnicLauncher/
[Issues]: https://github.com/TechnicPack/Spoutcraft-Launcher/issues
[Twitter]: https://twitter.com/TechnicPack
[Facebook]: https://www.facebook.com/TechnicPack
[Steam]: http://steamcommunity.com/groups/technic-pack
