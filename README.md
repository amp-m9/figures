# figures
Figure drawing class application written in Java using JavaFX for the UI. Will use an 
image folder the user points to as image source. Still in early alpha.
## App demo.
https://github.com/amp-m9/figures/assets/31145121/2f309cd6-640f-4eee-9e9a-049b4be70971

## Build with maven and warp-packer.
get [warp-packer](https://github.com/dgiagio/warp) and store it in the root folder of the project.
- [Linux](https://github.com/dgiagio/warp#macos:~:text=sh%20%0AHello%2C%20world.-,Download%20warp%2Dpacker,directory%20in%20your%20PATH%2C%20you%20only%20need%20to%20download%20it%20once.,-%24%20wget%20%2DO%20warp)
- [MacOS](https://github.com/dgiagio/warp#macos:~:text=dgiagio%24%20chmod%20%2Bx%20launch-,Download%20warp%2Dpacker,directory%20in%20your%20PATH%2C%20you%20only%20need%20to%20download%20it%20once.,-Diegos%2DiMac%3Amyapp%20dgiagio)
- [Windows](https://github.com/dgiagio/warp#windows:~:text=B%20%25ERRORLEVEL%25-,Download%20warp%2Dpacker,directory%20in%20your%20PATH%2C%20you%20only%20need%20to%20download%20it%20once.,-PS%20C%3A%5CUsers)

Then run the following, replacing `PLATFORM` with one of the following options:  
- `linux-x64`
- `windows-x64`
- `macos-x64`

```shell
mvn clean javafx:jlink
mkdir release
./warp-packer --arch [PLATFORM] -i ./target/figures/ --exec bin/figures --output ./release/figures
```