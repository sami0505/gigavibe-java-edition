# Gigavibe

Discord bot written in java using JDA and lavaplayer for audio functionality. Has youtube music, custom (user defined)
dj roles and some other cool features for you to play with.

## Requirements

> > [JDK 16](https://www.oracle.com/java/technologies/javase/jdk16-archive-downloads.html)
>
>> [YT-DLP ON WINDOWS](https://github.com/yt-dlp/yt-dlp/releases)
>
>> [FFMPEG & FFPROBE ON WINDOWS](https://www.gyan.dev/ffmpeg/builds/ffmpeg-git-essentials.7z)
>
>> [GUIDE FOR FFMPEG & FFPROBE ON LINUX](https://www.tecmint.com/install-ffmpeg-in-linux/)
>
>> Keep these files named as "ffprobe", "yt-dlp" and "ffmpeg".
> > The latest version of ffmpeg and yt-dlp are recommended, be sure to download the correct version for your correct
> > system.

## Usage

> > Make sure to define a token and change any parameters you wish to change (such as the prefix) within the .env file.
> > If there is no .env file, try run the bot once, it will create the files for you.
>
>> Run the bot using the latest included .jar file in releases or compile the jar yourself from source.

## Installation

> > JDK 8 can be acquired
> > from [The Oracle Archive](https://www.oracle.com/java/technologies/javase/jdk8-archive-downloads.html) or from other
> > trusted sources.
>
>> Here is how to install the latest version of yt-dlp
> > sudo wget https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp -O /usr/local/bin/yt-dlp
> > sudo chmod a+rx /usr/local/bin/yt-dlp
>
>> If you wish to install the requirements, place the files from the download links in the modules folder, if this
> > folder doesnt exist, make it where the .jar file is BUT MAKE SURE THAT ffprobe is called "ffprobe", yt-dlp is called "
> > yt-dlp" and ffmpeg is called "ffmpeg".

## **DISCLAIMERs:**

> > If you are on linux, be sure to use the correct commands to install the requirements for your distribution, multiple
> > distributions are supported, but I cannot guarantee stability as ive only tested with ubuntu.
>
>> Be sure that your ffmpeg version is **>=4.3.1**, this is to ensure that videodl and audiodl work correctly and as
> > intended, you can get this from the snap store or elsewhere.
