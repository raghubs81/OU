@echo off
adb shell su -c "cp /data/data/com.maga.ou/databases/OUDB /storage/sdcard1/Share"
adb pull /storage/sdcard1/Share/OUDB
