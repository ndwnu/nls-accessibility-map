echo "Preparing files so backend api can start"

function createSymlink() {
  if [[ -e active ]]; then
    unlink active
  fi
  ln -s empty active
}

# Go to working directory if set as first argument
workingDir=$@;
if [[ -n "$workingDir" ]]; then
  cd $workingDir
fi
projectRoot=$(pwd);


mkdir -p .tmp/trafficSigns/empty
cd .tmp/trafficSigns
echo "[]" > empty/trafficSigns.json
createSymlink
echo "Setup empty traffic sign cache"
cd $projectRoot


mkdir -p .tmp/network/empty/graphHopper/latest
cd .tmp/network/empty/graphHopper/latest
cp -R ../../../../../scripts/graphhopper-empty/* .
cd ../../../
createSymlink
cd $projectRoot

mkdir -p .tmp/network/empty/nwb/
cd .tmp/network/empty/nwb/
echo "{\"nwbVersionId\": 1, \"accessibilityNwbRoadSections\": []}\"" > roadSections.json
cd ../../
createSymlink
cd $projectRoot
echo "Setup empty network cache with graph hopper and nwb road sections"


echo "Done"
