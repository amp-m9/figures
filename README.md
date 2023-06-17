# figures
Figure drawing class application written in Java using JavaFX for the UI. Will use an 
image folder the user points to as image source. Still in early alpha.
## App demo.
https://github.com/amp-m9/figures/assets/31145121/2f309cd6-640f-4eee-9e9a-049b4be70971

## Build with maven and warp-packer.
### prerequisites
- maven
- JDK 17
- [warp-packer](https://github.com/dgiagio/warp/releases)

Get [warp-packer](https://github.com/dgiagio/warp/releases) and store it in the root folder of the project.

### Linux/MacOS
Then run the following, replacing `PLATFORM` with one of the following options:  
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
  warp-packer --arch windows-x64 -i ./target/figures/ --exec bin/figures.bat --output ./release/figures.exe
  ```
now find figures.exe in the releases foler of the project :) 
