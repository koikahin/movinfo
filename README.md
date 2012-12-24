For the truly impatient
-----------------------
Obtain movinfo.jar from [above]. Install Oracle JDK 7+ (from [here]). Make sure you install JDK, not the JRE, and that the version is at least 7. Open a command prompt or terminal, ```cd``` to the directory you downloaded movinfo.jar to. And do: 
```bash
java -jar movinfo.jar -dir "<movies-dir>" csv
```
where ```<movies-dir>``` is a directory having multiple movie files/directories. A movinfo.csv file will be generated within ```<movies-dir>``` directory having (currently) imdb information obtained through 'The OMDb API by Brian Fritz' (http://www.omdbapi.com). Thanks for that Mr. Fritz!

Example usages:
```bash
java -jar movinfo.jar -dir "K:\Movies" csv
java -jar movinfo.jar -dir "/media/Media1/All Movies/new" csv
```

About
=====
Some of us have lots of movies - spanning multiple partitions across several internal/external hard drives, perhaps so much, that it may get hard to decide what to watch. That's where websites like imdb.com and rottentomatoes.com come in to help us. Still, it is a bit of an overkill to open the website and manually run a list of probable movie candidatese through these websites everytime one wishes to watch a movie! 

This software wishes to address this problem. In the current (first) version, it parses the list of movies present in a given directory, fetches the movie information from www.omdbapi.com, and caches it in a csv file (hence the ```csv``` argument). We can extend this logic later to printout one file per movie, or append the movie's details to the filename itself, or even automatically organize movies based on genre/rating etc! 

Here is more information about the currently supported command line arguments:
```
[sri@sed Downloads]$ java -jar movinfo.jar -h
Usage: java -jar movinfo.jar [options] [command] [command options]
  Options:
    -c, --clean
       Clean the DB file and start fresh
       Default: false
    -d, --dir
       The movies directory to work within (Default: <current dir>)
       Default: /home/sri/Downloads
    -f, --file
       Filename (or approximate pattern) to include. Wildcards/regex are not
       supported
    -h, --help
       Displays this help
       Default: false
    -n, --noget, -nosync
       Don't get missing movie information from the internet
       Default: false
    -v, --verbose
       Provide verbose output (for debugging)
       Default: false
  Commands:
    csv      Provides movie information as a CSV file
      Usage: csv [options]
        Options:
          -d, --delimiter
             Specify the delimiter to use in the csv file
             Default: ,
          -f, --file
             Specify the name of the file to write to. Warning: the file
             specified will be overwritten.
             Default: movinfo.csv

```

*More details/Notes*

* The software is written in several loosely coupled components. The primary logic revolves around 'sync'ing or 'get'ing information of all movies present within a directory, into a db file. This file is by default called movinfo.db. Once you run it within a directory, you'll see this file getting created. The information is currently obtained, as mentioned above, from omdbapi.com, but can (and will likely) be extended to other providers like rottentomatoes.com as well. 
* The information 'sync'ed can now be used by various 'plugins'. The only plugin present now is the 'csv' plugin that spits out the information in an excel-sheet friendly csv format. However, as mentioned above, several others plugins can be implemented. 
* Multiple files similar in name to 'movinfo.db', if present in the target directory, are automatically merged. Therefore, if you wish to merge two movie directories, all you need to do is to ensure you rename one of the movinfo.db files (and not overwrite) into something like movinfo[1].db or 'Copy of movinfo.db' or movinfo (2).db, and run this program again to merge the two dbs into one. 
* <more details later>



DISCLAIMER:
  1. The software provided here has no endorsement or affiliation with IMDb.com, rottentomatoes.com, omdbapi.com or any other website. 
  2. This software is being provided as is, with a hope that it will be useful to you. There is no warranty, either expressed or implied. Using this software may burn your CPU, crash your harddrive or hurt you physically and anything in between. Use it at your own risk. 



  [above]: https://github.com/koikahin/movinfo/blob/master/movinfo.jar?raw=true
  [here]: http://www.oracle.com/technetwork/java/javase/downloads/index.html
