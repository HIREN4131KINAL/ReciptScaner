rm -f build.xml
rm -f proguard-project.txt
rm -f proguard.cfg
rm -f project.properties
rm -rf bin/
rm -rf gen/

mkdir -p src/main/java/
mv src/co src/main/java/co
mv src/com src/main/java/com
mv src/wb src/main/java/wb
mv res src/main/res
mv AndroidManifest.xml src/main/AndroidManifest.xml

