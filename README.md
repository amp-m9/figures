# Figures
![bann err](https://github.com/amp-m9/figures/assets/31145121/5cdcdaef-5a5b-4f1a-9551-c7adbde888e7)

Figure drawing class application written in Java using JavaFX for the UI. Will use an 
image folder the user points to as image source. Still in early alpha.

## Install/use
You can simply download the app for your platform from the 
[releases page](https://github.com/amp-m9/figures/releases) if available, alternatively
you can [build it yourself](https://github.com/amp-m9/figures#build-with-maven-and-warp-packer).

Once acquired you can also get some **free reference photos (some NSFW)** from [reference.pictures/free/](https://reference.pictures/free/) 
to use with the app.

## Screenshots
### Start screen
![image](https://github.com/amp-m9/figures/assets/31145121/6b49f14c-43f7-4334-bd95-36b195b72c1c)
### Session
![image](https://github.com/amp-m9/figures/assets/31145121/02d5719a-e720-43ff-8c5a-f496bdbc2d71)
### Session: grid on 
![image](https://github.com/amp-m9/figures/assets/31145121/58c7c663-2180-41bd-aaac-c591c24b3460)
### Session: grayscale mode
![image](https://github.com/amp-m9/figures/assets/31145121/7555c2bb-8428-49b0-9d60-d1dfa60c5968)
### Pause screen
![image](https://github.com/amp-m9/figures/assets/31145121/bafebd12-72b0-452f-9069-8dadde5d1889)
### Break screen
![image](https://github.com/amp-m9/figures/assets/31145121/31d24a29-0c2c-433d-9107-6dc8a16d7241)


## Build with maven and warp-packer.
### prerequisites
- maven
- JDK 17
- [warp-packer](https://github.com/dgiagio/warp/releases)

Get [warp-packer](https://github.com/dgiagio/warp/releases) and store it in the root folder of the project.

### Linux/MacOS
Then run the following, replacing `[PLATFORM]` with one of the following options:  
- `linux-x64`
- `macos-x64`**(untested)**
```shell
mvn clean javafx:jlink
mkdir release
./warp-packer --arch [PLATFORM] -i ./target/figures/ --exec bin/figures --output ./release/figures
```

### Windows
Windows install requires a bit more work.

- Run `mvn clean javafx:jlink`
- modify target\figures\bin\figures.bat.
    - replace `"%DIR%\java"` at the start of the last line with `start "" "%DIR%\javaw"` **making sure it says `javaw` and not `java`**
    - add ` && exit 0` to the end of the last line.
    - last line should now read as follows/similar
      ```shell
      start "" "%DIR%\javaw" %JLINK_VM_OPTIONS% -m xyz.andrick.figures/xyz.andrick.figures.FiguresApplication %* && exit 0
      ```
- run the following
  ```shell
  mkdir release
  warp-packer --arch windows-x64 -i ./target/figures/ --exec bin/figures.bat --output ./release/figures.exe
  ```
now find figures.exe in the releases foler of the project :) 
