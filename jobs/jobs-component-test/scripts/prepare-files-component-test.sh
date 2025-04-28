cd ../../.tmp/trafficsigns
echo "[]" > test
ln -s test active

cd ../graphhopper
mkdir accessibility_latest
cd accessibility_latest
echo "{\"nwbVersion\":20240701}" > accessibility_meta_data.json