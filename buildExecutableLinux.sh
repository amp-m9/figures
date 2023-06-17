#! /bin/bash
mvn clean javafx:jlink
rm -r release
mkdir release
./warp-packer --arch linux-x64 -i ./target/figures/ --exec bin/figures --output ./release/figures_linux_x64